package note

func NewService(repo Repository) *Service {
	return &Service{
		Repository: repo,
	}
}

type Service struct {
	Repository
}
