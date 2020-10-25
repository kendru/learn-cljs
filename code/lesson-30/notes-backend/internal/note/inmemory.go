package note

import (
	"time"
)

type inMemoryRepo struct {
	notes  []Note
	lastID uint64
}

func NewInMemoryRepo() *inMemoryRepo {
	return &inMemoryRepo{}
}

func (r *inMemoryRepo) FindByID(id uint64) (*Note, error) {
	for _, note := range r.notes {
		if note.ID == id {
			return &note, nil
		}
	}

	return nil, nil
}

func (r *inMemoryRepo) FindAll() ([]*Note, error) {
	notes := make([]*Note, len(r.notes))
	for i, note := range r.notes {
		n := note
		notes[i] = &n
	}

	return notes, nil
}

func (r *inMemoryRepo) Create(note *Note) error {
	r.lastID++
	note.ID = r.lastID
	now := time.Now()
	note.CreatedAt = now
	note.UpdatedAt = now
	r.notes = append(r.notes, *note)
	return nil
}

func (r *inMemoryRepo) Update(id uint64, update *Note) error {
	for i, note := range r.notes {
		if note.ID == id {
			newNote := note
			if update.Title != "" {
				newNote.Title = update.Title
			}
			if update.Body != "" {
				newNote.Body = update.Body
			}

			newNote.UpdatedAt = time.Now()
			r.notes[i] = newNote
			break
		}
	}

	return nil
}

func (r *inMemoryRepo) Delete(id uint64) error {
	notes := make([]Note, len(r.notes)-1)
	for _, note := range r.notes {
		if note.ID != id {
			notes = append(notes, note)
		}
	}

	return nil
}
