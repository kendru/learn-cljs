package note

import (
	"path"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestBleveSearchIndex(t *testing.T) {
	dir := t.TempDir()
	file := path.Join(dir, "index.bleve")
	idx, err := NewBleveSearchindex(SearchIndexConfig{
		BleveIndexPath: file,
	})
	assert.NoError(t, err)

	err = idx.IndexNote("tenant1", &Note{
		ID:      123,
		Title:   "Taco Tuesday",
		Content: "Every day is a great day to eat tacos!",
	})
	assert.NoError(t, err)

	err = idx.IndexNote("tenant1", &Note{
		ID:      234,
		Title:   "Todos",
		Content: "1. Buy tacos\n2. Eat tacos\n",
		Tags: []*Tag{
			&Tag{ID: 1, Name: "list"},
		},
	})
	assert.NoError(t, err)

	err = idx.IndexNote("tenant1", &Note{
		ID:      345,
		Title:   "Misc",
		Content: "This should be done by tuesday at the latest",
	})
	assert.NoError(t, err)

	notes, err := idx.Search("tenant1", "tuesday")
	assert.NoError(t, err)
	assert.Len(t, notes, 2)
	assert.Contains(t, notes, uint64(123))
	assert.Contains(t, notes, uint64(345))

	notes, err = idx.Search("tenant1", "Tacos")
	assert.NoError(t, err)
	assert.Len(t, notes, 2)
	assert.Contains(t, notes, uint64(123))
	assert.Contains(t, notes, uint64(234))

	notes, err = idx.Search("other-tenant", "Tacos")
	assert.NoError(t, err)
	assert.Empty(t, notes, "should not get notes for another tenant")

	err = idx.RemoveNote("tenant1", 123)
	assert.NoError(t, err)

	notes, err = idx.Search("tenant1", "tacos")
	assert.NoError(t, err)
	assert.Equal(t, []uint64{234}, notes, "should not return deleted notes")

	notes, err = idx.Search("tenant1", "List")
	assert.NoError(t, err)
	assert.Len(t, notes, 1)
	assert.Contains(t, notes, uint64(234), "should match tags")
}
