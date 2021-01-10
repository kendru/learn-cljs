package note

import (
	"encoding/json"
	"time"
)

type Note struct {
	ID        uint64    `json:"id"`
	Title     string    `json:"title"`
	Content   string    `json:"content"`
	Tags      []*Tag    `json:"tags"`
	CreatedAt time.Time `json:"createdAt"`
	UpdatedAt time.Time `json:"updatedAt"`
}

type Tag struct {
	ID        uint64    `json:"id"`
	Name      string    `json:"name"`
	CreatedAt time.Time `json:"createdAt"`
}

func (n *Note) MustMarshal() []byte {
	bs, err := json.Marshal(n)
	if err != nil {
		panic(err)
	}
	return bs
}

func (n *Note) Unmarshal(bs []byte) error {
	if n == nil {
		return nil
	}
	return json.Unmarshal(bs, n)
}

func (t *Tag) MustMarshal() []byte {
	bs, err := json.Marshal(t)
	if err != nil {
		panic(err)
	}
	return bs
}

func (t *Tag) Unmarshal(bs []byte) error {
	if t == nil {
		return nil
	}
	return json.Unmarshal(bs, t)
}
