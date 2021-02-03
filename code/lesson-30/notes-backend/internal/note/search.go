package note

type SearchIndex interface {
	IndexNote(tenantID string, note *Note) error
	RemoveNote(tenantID string, id uint64) error
	Search(tenantID, query string) ([]uint64, error)
}

type SearchIndexConfig struct {
	BleveIndexPath string `mapstructure:"bleve-path"`
}
