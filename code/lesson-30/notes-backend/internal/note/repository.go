package note

import "fmt"

type Read interface {
	FindNoteByID(id uint64) (*Note, error)
	FindAllNotes() ([]*Note, error)

	FindTagByID(id uint64) (*Tag, error)
	FindAllTags() ([]*Tag, error)
}

type Mutate interface {
	CreateNote(*Note) error
	UpdateNote(id uint64, note *Note) error
	DeleteNote(id uint64) error

	CreateTag(*Tag) error
	DeleteTag(id uint64) error

	TagNote(noteID, tagID uint64) error
	UntagNote(noteID, tagID uint64) error
}

type Transaction interface {
	Read
	Mutate
}

type Repository interface {
	Transaction(tenantID string) Transaction
	Close() error
}

func NewRepository(c RepositoryConfig) (Repository, error) {
	switch c.Type {
	case "memory":
		return NewInMemoryRepo(), nil
	case "badgerdb":
		return NewBadgerRepo(c)
	default:
		return nil, fmt.Errorf("Invalid repository type: %q", c.Type)
	}
}

type RepositoryConfig struct {
	Type string

	BadgerDir string `mapstructure:"badger-dir"`
}
