package note

import (
	"bytes"
	"encoding/binary"
	"fmt"
	"sync"
	"time"

	badger "github.com/dgraph-io/badger/v2"
)

// Add data is stored in a single db file.

const (
	noteIDSeq = "noteIDs"
	tagIDSeq  = "tagIDs"
)

func NewBadgerRepo(c RepositoryConfig) (*badgerRepo, error) {
	dir := c.BadgerDir
	db, err := badger.Open(badger.DefaultOptions(dir))
	if err != nil {
		return nil, fmt.Errorf("error opening BadgerDB at %q: %w", dir, err)
	}

	repo := &badgerRepo{db: db}

	if repo.noteIDs, err = db.GetSequence([]byte(noteIDSeq), 100); err != nil {
		return nil, fmt.Errorf("error acquiring note id seq: %w", err)
	}
	// Ensure that 0 can never be used for an ID
	if _, err = repo.noteIDs.Next(); err != nil {
		return nil, fmt.Errorf("error advancing note ID seq: %w", err)
	}

	if repo.tagIDs, err = db.GetSequence([]byte(tagIDSeq), 100); err != nil {
		return nil, fmt.Errorf("error acquiring tag id Seq: %w", err)
	}
	if _, err = repo.tagIDs.Next(); err != nil {
		return nil, fmt.Errorf("error advancing tag ID seq: %w", err)
	}

	return repo, nil
}

type badgerRepo struct {
	db      *badger.DB
	noteIDs *badger.Sequence
	tagIDs  *badger.Sequence
}

func (r *badgerRepo) Close() error {
	fmt.Println("Closing BadgerDB database")
	if err := r.noteIDs.Release(); err != nil {
		return err
	}
	if err := r.tagIDs.Release(); err != nil {
		return err
	}
	return r.db.Close()
}

func (r *badgerRepo) Transaction(tenantID string) Transaction {
	return &badgerTransaction{
		badgerRepo: r,
		tenantID:   tenantID,
	}
}

type badgerTransaction struct {
	*badgerRepo
	tenantID string
}

func (tx *badgerTransaction) FindNoteByID(id uint64) (note *Note, err error) {
	key := tx.noteKey(id)
	err = tx.db.View(func(txn *badger.Txn) error {
		item, err := txn.Get(key.Bytes())
		switch err {
		case nil:
			return item.Value(func(bs []byte) error {
				note = new(Note)
				return note.Unmarshal(bs)
			})
		case badger.ErrKeyNotFound:
			return nil
		default:
			return err
		}
	})
	if note != nil {
		err = tx.withTags(note)
	}
	return
}

func (tx *badgerTransaction) FindAllNotes() ([]*Note, error) {
	notes := make([]*Note, 0)
	err := tx.db.View(func(txn *badger.Txn) error {
		opts := badger.DefaultIteratorOptions
		opts.PrefetchSize = 10
		it := txn.NewIterator(opts)
		defer it.Close()

		prefix := tx.noteKey(0).Bytes()
		for it.Seek(prefix); it.ValidForPrefix(prefix); it.Next() {
			item := it.Item()
			err := item.Value(func(bs []byte) error {
				note := new(Note)
				if err := note.Unmarshal(bs); err != nil {
					return fmt.Errorf("error decoding as note: %w", err)
				}
				if err := tx.withTags(note); err != nil {
					return err
				}
				notes = append(notes, note)
				return nil
			})
			if err != nil {
				return err
			}
		}
		return nil
	})

	return notes, err
}

func (tx *badgerTransaction) FindTagByID(id uint64) (tag *Tag, err error) {
	key := tx.tagKey(id)
	err = tx.db.View(func(txn *badger.Txn) error {
		item, err := txn.Get(key.Bytes())
		switch err {
		case nil:
			return item.Value(func(bs []byte) error {
				tag = new(Tag)
				return tag.Unmarshal(bs)
			})
		case badger.ErrKeyNotFound:
			return nil
		default:
			return err
		}
	})
	return
}

func (tx *badgerTransaction) FindAllTags() ([]*Tag, error) {
	tags := make([]*Tag, 0)
	err := tx.db.View(func(txn *badger.Txn) error {
		opts := badger.DefaultIteratorOptions
		opts.PrefetchSize = 10
		it := txn.NewIterator(opts)
		defer it.Close()

		prefix := tx.tagKey(0).Bytes()
		for it.Seek(prefix); it.ValidForPrefix(prefix); it.Next() {
			item := it.Item()
			err := item.Value(func(bs []byte) error {
				tag := new(Tag)
				if err := tag.Unmarshal(bs); err != nil {
					return err
				}
				tags = append(tags, tag)
				return nil
			})
			if err != nil {
				return err
			}
		}
		return nil
	})

	return tags, err
}

func (tx *badgerTransaction) CreateNote(note *Note) error {
	id, err := tx.noteIDs.Next()
	if err != nil {
		return err
	}

	key := tx.noteKey(id)
	now := time.Now()
	note.ID = id
	note.CreatedAt = now
	note.UpdatedAt = now
	return tx.db.Update(func(txn *badger.Txn) error {
		return txn.Set(key.Bytes(), note.MustMarshal())
	})
}

func (tx *badgerTransaction) UpdateNote(id uint64, note *Note) error {
	key := tx.noteKey(id)
	note.UpdatedAt = time.Now()
	return tx.db.Update(func(txn *badger.Txn) error {
		return txn.Set(key.Bytes(), note.MustMarshal())
	})
}

func (tx *badgerTransaction) DeleteNote(id uint64) error {
	key := tx.noteKey(id)
	// TODO: Schedule deletion of note/tag associations for this note
	return tx.db.Update(func(txn *badger.Txn) error {
		return txn.Delete(key.Bytes())
	})
}

func (tx *badgerTransaction) CreateTag(tag *Tag) error {
	id, err := tx.tagIDs.Next()
	if err != nil {
		return err
	}

	key := tx.tagKey(id)
	tag.ID = id
	tag.CreatedAt = time.Now()
	return tx.db.Update(func(txn *badger.Txn) error {
		return txn.Set(key.Bytes(), tag.MustMarshal())
	})
}

func (tx *badgerTransaction) DeleteTag(id uint64) error {
	key := tx.tagKey(id)
	// TODO: Schedule deletion of tag/note associations for this tag
	return tx.db.Update(func(txn *badger.Txn) error {
		return txn.Delete(key.Bytes())
	})
}

func (tx *badgerTransaction) TagNote(noteID, tagID uint64) error {
	// TODO: Validate existence of note and tag
	noteIDBytes := tx.noteKey(noteID).entityKey
	tagIDBytes := tx.tagKey(tagID).entityKey
	return tx.db.Update(func(txn *badger.Txn) error {
		noteToTagKey := tx.noteTagKey(noteID, tagID)
		if err := txn.Set(noteToTagKey.Bytes(), tagIDBytes); err != nil {
			// If there is an error here, we will end up in an inconsistent state
			// because we could not delete the reverse reference.
			return err
		}

		tagToNoteKey := tx.tagNoteKey(tagID, noteID)
		if err := txn.Set(tagToNoteKey.Bytes(), noteIDBytes); err != nil {
			return err
		}

		return nil
	})
}

func (tx *badgerTransaction) UntagNote(noteID, tagID uint64) error {
	return tx.db.Update(func(txn *badger.Txn) error {
		noteToTagKey := tx.noteTagKey(noteID, tagID)
		if err := txn.Delete(noteToTagKey.Bytes()); err != nil {
			return err
		}

		tagToNoteKey := tx.tagNoteKey(tagID, noteID)
		if err := txn.Delete(tagToNoteKey.Bytes()); err != nil {
			return err
		}

		return nil
	})
}

func (tx *badgerTransaction) withTags(note *Note) error {
	var tagIDs []uint64
	err := tx.db.View(func(txn *badger.Txn) error {
		opts := badger.DefaultIteratorOptions
		opts.PrefetchSize = 10
		it := txn.NewIterator(opts)
		defer it.Close()

		prefix := tx.noteTagKey(note.ID, 0).Bytes()
		for it.Seek(prefix); it.ValidForPrefix(prefix); it.Next() {
			item := it.Item()
			err := item.Value(func(bs []byte) error {
				tagID := binary.BigEndian.Uint64(bs)
				tagIDs = append(tagIDs, tagID)
				return nil
			})
			if err != nil {
				return err
			}
		}
		return nil
	})
	if err != nil {
		return err
	}

	tagCount := len(tagIDs)
	if tagCount == 0 {
		return nil
	}

	var mu sync.Mutex
	var wg sync.WaitGroup
	wg.Add(tagCount)
	note.Tags = make([]*Tag, tagCount)
	for i, tagID := range tagIDs {
		i := i
		tagID := tagID
		go func() {
			var findTagErr error
			if note.Tags[i], findTagErr = tx.FindTagByID(tagID); findTagErr != nil {
				mu.Lock()
				err = findTagErr
				mu.Unlock()
			}
			wg.Done()
		}()
	}
	wg.Wait()

	return err
}

func (tx *badgerTransaction) noteKey(id uint64) badgerKey {
	key := badgerKey{
		tenantID:   tx.tenantID,
		entityType: "n",
	}
	if id > 0 {
		entityID := make([]byte, 8)
		binary.BigEndian.PutUint64(entityID, id)
		key.entityKey = entityID
	}

	return key
}

func (tx *badgerTransaction) tagKey(id uint64) badgerKey {
	key := badgerKey{
		tenantID:   tx.tenantID,
		entityType: "t",
	}
	if id > 0 {
		entityID := make([]byte, 8)
		binary.BigEndian.PutUint64(entityID, id)
		key.entityKey = entityID
	}

	return key
}

func (tx *badgerTransaction) noteTagKey(noteID, tagID uint64) badgerKey {
	return tx.assocKey("nt", noteID, tagID)
}

func (tx *badgerTransaction) tagNoteKey(tagID, noteID uint64) badgerKey {
	return tx.assocKey("tn", tagID, noteID)
}

func (tx *badgerTransaction) assocKey(joinType string, idA, idB uint64) badgerKey {
	var idBuf bytes.Buffer
	idBuf.Grow(16)

	entityID := make([]byte, 8)
	binary.BigEndian.PutUint64(entityID, idA)
	_, _ = idBuf.Write(entityID)
	if idB > 0 {
		binary.BigEndian.PutUint64(entityID, idB)
		_, _ = idBuf.Write(entityID)
	}

	return badgerKey{
		tenantID:   tx.tenantID,
		entityType: joinType,
		entityKey:  idBuf.Bytes(),
	}
}

type badgerKey struct {
	tenantID, entityType string
	entityKey            []byte
}

const KEY_SEP byte = 0

func (k badgerKey) Bytes() []byte {
	var buf bytes.Buffer

	buf.Grow(len(k.tenantID) + 1 + len(k.entityType) + 1 + len(k.entityKey))

	buf.WriteString(k.tenantID)
	buf.WriteByte(KEY_SEP)
	buf.WriteString(k.entityType)
	buf.WriteByte(KEY_SEP)
	buf.Write(k.entityKey)

	return buf.Bytes()
}
