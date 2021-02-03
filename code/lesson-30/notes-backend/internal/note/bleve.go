package note

import (
	"errors"
	"fmt"
	"strconv"

	"github.com/blevesearch/bleve"
	"github.com/blevesearch/bleve/analysis/analyzer/keyword"
	"github.com/blevesearch/bleve/analysis/lang/en"
	"github.com/blevesearch/bleve/search/query"
)

func NewBleveSearchindex(c SearchIndexConfig) (*BleveSearchIndex, error) {
	idx, err := bleve.Open(c.BleveIndexPath)
	if err == bleve.ErrorIndexPathDoesNotExist {
		idx, err = initIndex(c)
	}
	if err != nil {
		return nil, err
	}

	return &BleveSearchIndex{
		idx: idx,
	}, nil
}

type BleveSearchIndex struct {
	idx bleve.Index
}

type searchDocument struct {
	TenantID string `json:"tenantId"`
	Note     *Note  `json:"note"`
}

func (i *BleveSearchIndex) IndexNote(tenantID string, note *Note) error {
	return i.idx.Index(stringID(note.ID), searchDocument{
		TenantID: tenantID,
		Note:     note,
	})
}

func (i *BleveSearchIndex) RemoveNote(tenantID string, id uint64) error {
	q := bleve.NewBooleanQuery()
	q.AddMust(
		bleve.NewDocIDQuery([]string{stringID(id)}),
	)
	q.AddMust(tenantTermQuery(tenantID))
	req := bleve.NewSearchRequest(q)
	res, err := i.idx.Search(req)
	if err != nil {
		return fmt.Errorf("error checking for document: %w", err)
	}
	if res.Total != 1 {
		return errors.New("document not found in search index")
	}

	return i.idx.Delete(stringID(id))
}

func (i *BleveSearchIndex) Search(tenantID string, query string) ([]uint64, error) {
	q := bleve.NewBooleanQuery()
	q.AddMust(bleve.NewMatchQuery(query))
	q.AddMust(tenantTermQuery(tenantID))
	req := bleve.NewSearchRequest(q)
	res, err := i.idx.Search(req)
	if err != nil {
		return nil, err
	}

	hits := res.Hits
	ids := make([]uint64, len(hits))
	for i, hit := range hits {
		if ids[i], err = numID(hit.ID); err != nil {
			return nil, err
		}
	}

	return ids, nil
}

func tenantTermQuery(tenantID string) *query.TermQuery {
	tenantTerm := bleve.NewTermQuery(tenantID)
	tenantTerm.SetField("tenantId")

	return tenantTerm
}

func stringID(id uint64) string {
	return strconv.FormatUint(id, 10)
}

func numID(id string) (uint64, error) {
	return strconv.ParseUint(id, 10, 64)
}

func initIndex(c SearchIndexConfig) (bleve.Index, error) {
	textFM := bleve.NewTextFieldMapping()
	textFM.Analyzer = en.AnalyzerName
	textFM.Store = false

	keywordFM := bleve.NewTextFieldMapping()
	keywordFM.Analyzer = keyword.Name
	keywordFM.Store = false

	tagMapping := bleve.NewDocumentMapping()
	tagMapping.AddFieldMappingsAt("name", keywordFM)

	noteMapping := bleve.NewDocumentMapping()
	noteMapping.AddFieldMappingsAt("title", textFM)
	noteMapping.AddFieldMappingsAt("content", textFM)
	noteMapping.AddSubDocumentMapping("tags", tagMapping)

	docMapping := bleve.NewDocumentMapping()
	docMapping.AddFieldMappingsAt("tenantId", keywordFM)
	docMapping.AddSubDocumentMapping("note", noteMapping)

	indexMapping := bleve.NewIndexMapping()
	indexMapping.AddDocumentMapping("doc", docMapping)
	indexMapping.DefaultType = "doc"
	indexMapping.DefaultAnalyzer = "en"
	indexMapping.IndexDynamic = false
	indexMapping.StoreDynamic = false

	return bleve.New(c.BleveIndexPath, indexMapping)
}
