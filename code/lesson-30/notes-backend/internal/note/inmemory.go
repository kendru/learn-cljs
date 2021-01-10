package note

import (
	"fmt"
	"time"
)

type inMemoryRepo struct {
	notes  []Note
	tags   []Tag
	links  []link
	lastID uint64
}

type link struct {
	noteID, tagID uint64
}

func NewInMemoryRepo() *inMemoryRepo {
	return &inMemoryRepo{}
}

func (r *inMemoryRepo) Transaction(tenantID string) Transaction {
	// Ignore tenantID - inMemoryRepo itself implements transaction
	return r
}

func (r *inMemoryRepo) FindNoteByID(id uint64) (*Note, error) {
	for _, note := range r.notes {
		if note.ID == id {
			return r.withTags(note), nil
		}
	}

	return nil, nil
}

func (r *inMemoryRepo) FindAllNotes() ([]*Note, error) {
	notes := make([]*Note, len(r.notes))
	for i, note := range r.notes {
		n := note
		notes[i] = r.withTags(n)
	}

	return notes, nil
}

func (r *inMemoryRepo) FindTagByID(id uint64) (*Tag, error) {
	for _, tag := range r.tags {
		if tag.ID == id {
			return &tag, nil
		}
	}

	return nil, nil
}

func (r *inMemoryRepo) FindAllTags() ([]*Tag, error) {
	tags := make([]*Tag, len(r.tags))
	for i, tag := range r.tags {
		t := tag
		tags[i] = &t
	}

	return tags, nil
}

func (r *inMemoryRepo) CreateNote(note *Note) error {
	r.lastID++
	note.ID = r.lastID
	now := time.Now()
	note.CreatedAt = now
	note.UpdatedAt = now
	r.notes = append(r.notes, *note)
	return nil
}

func (r *inMemoryRepo) UpdateNote(id uint64, update *Note) error {
	for i, note := range r.notes {
		if note.ID == id {
			newNote := note
			if update.Title != "" {
				newNote.Title = update.Title
			}
			if update.Content != "" {
				newNote.Content = update.Content
			}

			newNote.UpdatedAt = time.Now()
			r.notes[i] = newNote
			break
		}
	}

	return nil
}

func (r *inMemoryRepo) DeleteNote(id uint64) error {
	notes := make([]Note, len(r.notes)-1)
	for _, note := range r.notes {
		if note.ID != id {
			notes = append(notes, note)
		}
	}

	return nil
}

func (r *inMemoryRepo) CreateTag(tag *Tag) error {
	r.lastID++
	tag.ID = r.lastID
	now := time.Now()
	tag.CreatedAt = now
	r.tags = append(r.tags, *tag)
	return nil
}

func (r *inMemoryRepo) DeleteTag(id uint64) error {
	tags := make([]Tag, len(r.tags)-1)
	for _, tag := range r.tags {
		if tag.ID != id {
			tags = append(tags, tag)
		}
	}

	return nil
}

func (r *inMemoryRepo) TagNote(noteID, tagID uint64) error {
	for _, link := range r.links {
		if link.noteID == noteID && link.tagID == tagID {
			return nil
		}
	}
	r.links = append(r.links, link{
		noteID: noteID,
		tagID:  tagID,
	})

	return nil
}

func (r *inMemoryRepo) UntagNote(noteID, tagID uint64) error {
	var newLinks []link
	for _, link := range r.links {
		if link.noteID == noteID && link.tagID == tagID {
			continue
		}
		newLinks = append(newLinks, link)
	}
	r.links = newLinks

	return nil
}

func (r *inMemoryRepo) Close() error {
	fmt.Println("Closing in-memory repo (TODO: Remove this noop log)")
	return nil
}

func (r *inMemoryRepo) withTags(n Note) *Note {
	for _, link := range r.links {
		if link.noteID == n.ID {
			tag, _ := r.FindTagByID(link.tagID)
			n.Tags = append(n.Tags, tag)
		}
	}

	return &n
}
