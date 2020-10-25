package session

import (
	"sync"
)

func NewSessionManager() *SessionManager {
	return &SessionManager{
		sessions: make(map[int]*Session),
		nextId:   1,
	}
}

type SessionManager struct {
	mu       sync.Mutex
	sessions map[int]*Session
	nextId   int
}

func (s *SessionManager) StartSession(name string) (*Session, error) {
	accessKey, err := getRandomKey()
	if err != nil {
		return nil, err
	}

	s.mu.Lock()
	id := s.nextId
	s.nextId++
	session := &Session{
		ID:        id,
		Name:      name,
		AccessKey: accessKey,
	}
	s.sessions[id] = session
	s.mu.Unlock()

	return session, nil
}

func (s *SessionManager) ListSessions() ([]*Session, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	i := 0
	sessions := make([]*Session, len(s.sessions))
	for _, session := range s.sessions {
		sessions[i] = session
		i++
	}

	return sessions, nil
}

type Session struct {
	ID        int      `json:"id"`
	Name      string   `json:"name"`
	AccessKey string   `json:"-"`
	Members   []Member `json:"members,omitempty"`
}

type Member struct {
	ID   int
	Name string
}
