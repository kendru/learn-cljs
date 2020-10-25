package note

type Read interface {
	FindByID(id uint64) (*Note, error)
	FindAll() ([]*Note, error)
}

type Mutate interface {
	Create(*Note) error
	Update(id uint64, note *Note) error
	Delete(id uint64) error
}

type Repository interface {
	Read
	Mutate
}
