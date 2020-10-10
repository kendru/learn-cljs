package app

import (
	"encoding/json"

	"learn-cljs.com/estimates/internal/session"
)

type MessageType string

const (
	StartSession   MessageType = "start-session"
	SessionStarted             = "session-started"
	ListSessions               = "list-sessions"
	SessionList                = "session-list"
	Error                      = "error"
)

type Message struct {
	Type    MessageType     `json:"type"`
	Payload json.RawMessage `json:"payload,omitempty"`
}

// Request types

type StartSessionPayload struct {
	Name string `json:"name"`
}

// Response types

func newSessionStartedMessage(id int, accessKey string) (*Message, error) {
	payload, err := json.Marshal(&SessionStartedPayload{
		ID:        id,
		AccessKey: accessKey,
	})
	if err != nil {
		return nil, err
	}

	return &Message{
		Type:    SessionStarted,
		Payload: payload,
	}, nil
}

type SessionStartedPayload struct {
	ID        int    `json:"id"`
	AccessKey string `json:"accessKey"`
}

func newSessionListMessage(sessions []*session.Session) (*Message, error) {
	payload, err := json.Marshal(&SessionListPayload{
		Sessions: sessions,
	})
	if err != nil {
		return nil, err
	}

	return &Message{
		Type:    SessionList,
		Payload: payload,
	}, nil
}

type SessionListPayload struct {
	Sessions []*session.Session `json:"sessions"`
}

func newErrorMessage(message string) (*Message, error) {
	payload, err := json.Marshal(&ErrorPayload{
		Error: message,
	})
	if err != nil {
		return nil, err
	}

	return &Message{
		Type:    Error,
		Payload: payload,
	}, nil
}

type ErrorPayload struct {
	Error string `json:"error"`
}
