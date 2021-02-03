package note

func NewService(repo Repository, idx SearchIndex) *Service {
	return &Service{
		Repository: repo,
		idx:        idx,
	}
}

type Service struct {
	Repository
	idx SearchIndex
}

func (s *Service) SearchNotes(tenantID, query string) ([]*Note, error) {
	ids, err := s.idx.Search(tenantID, query)
	if err != nil {
		return nil, err
	}

	notes := make([]*Note, len(ids))
	tx := s.Repository.Transaction(tenantID)
	for i, id := range ids {
		if notes[i], err = tx.FindNoteByID(id); err != nil {
			return nil, err
		}
	}

	return notes, nil
}
