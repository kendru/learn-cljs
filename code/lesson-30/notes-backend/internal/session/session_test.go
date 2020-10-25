package session

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestStartSession(t *testing.T) {
	mgr := NewSessionManager()

	session, err := mgr.StartSession("Test Session")

	assert.NoError(t, err)

	assert.Equal(t, "Test Session", session.Name)
	assert.NotEmpty(t, session.ID)
	assert.NotEmpty(t, session.AccessKey)
}
