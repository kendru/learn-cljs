package session

import (
	"crypto/rand"
	"encoding/base64"
)

func randomBytes(n int) ([]byte, error) {
	b := make([]byte, n)
	if _, err := rand.Read(b); err != nil {
		return nil, err
	}

	return b, nil
}

func getRandomKey() (string, error) {
	b, err := randomBytes(12)
	return base64.URLEncoding.EncodeToString(b), err
}
