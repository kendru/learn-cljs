package transport

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"net"
	"net/http"
	"strconv"
	"time"

	"learn-cljs.com/notes/internal/note"

	"github.com/go-chi/chi"
	"github.com/go-chi/chi/middleware"
	"github.com/go-chi/cors"
	"github.com/go-chi/render"
)

type HTTPServer struct {
	http.Server
	config Config

	notes *note.Service
}

type Config struct {
	Context       context.Context
	Addr          string
	StaticFileDir string
	NoteService   *note.Service
}

func NewHTTPServer(c Config) *HTTPServer {
	var s = &HTTPServer{
		config: c,
		notes:  c.NoteService,
	}

	s.Server = http.Server{
		Addr:    c.Addr,
		Handler: s.newRouter(c.StaticFileDir),
		BaseContext: func(l net.Listener) context.Context {
			return c.Context
		},
	}

	return s
}

func (s *HTTPServer) newRouter(staticFileDir string) chi.Router {
	r := chi.NewRouter()

	r.Use(
		middleware.RequestID,
		middleware.RealIP,
		middleware.Logger,
		middleware.Recoverer,
		middleware.StripSlashes,
		middleware.Timeout(20*time.Second),
		cors.Handler(cors.Options{
			AllowedOrigins:   []string{"*"},
			AllowedMethods:   []string{"GET", "POST", "PUT", "DELETE"},
			AllowedHeaders:   []string{"Accept", "Content-Type"},
			ExposedHeaders:   []string{"Location"},
			AllowCredentials: false,
			MaxAge:           300,
		}),
	)

	r.Route("/notes", func(r chi.Router) {
		r.Post("/", s.handleCreateNote)
		r.Get("/", s.handleListNotes)

		r.Route("/{noteID}", func(r chi.Router) {
			r.Use(s.noteCtx) // Add note to context based on noteID route param
			r.Get("/", s.getNote)
			r.Put("/", s.updateNote)
			r.Delete("/", s.deleteNote)

			r.Route("/tags/{tagID}", func(r chi.Router) {
				r.Use(s.tagCtx)
				r.Put("/", s.tagNote)
				r.Delete("/", s.untagNote)
			})
		})
	})

	r.Route("/tags", func(r chi.Router) {
		r.Post("/", s.handleCreateTag)
		r.Get("/", s.handleListTags)
	})

	// Since all files are relative to the root path, we do not need to worry about
	// stripping a prefix.
	fs := http.FileServer(http.Dir(staticFileDir))
	r.Get("/*", fs.ServeHTTP)

	return r
}

func (s *HTTPServer) handleCreateNote(w http.ResponseWriter, r *http.Request) {
	n := &note.Note{}
	if err := json.NewDecoder(r.Body).Decode(n); err != nil {
		render.Render(w, r, errInvalidRequest(err))
		return
	}
	if err := s.notes.Transaction("TODO: Get Tenant").CreateNote(n); err != nil {
		render.Render(w, r, errInvalidRequest(err))
		return
	}

	w.Header().Add("Location", fmt.Sprintf("/notes/%d", n.ID))
	if err := json.NewEncoder(w).Encode(n); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}
}

func (s *HTTPServer) handleListNotes(w http.ResponseWriter, r *http.Request) {
	notes, err := s.notes.Transaction("TODO: Get Tenant").FindAllNotes()
	if err != nil {
		render.Render(w, r, errServerError(err))
		return
	}
	if err := json.NewEncoder(w).Encode(notes); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}
}

func (s *HTTPServer) handleCreateTag(w http.ResponseWriter, r *http.Request) {
	t := &note.Tag{}
	if err := json.NewDecoder(r.Body).Decode(t); err != nil {
		render.Render(w, r, errInvalidRequest(err))
		return
	}
	if err := s.notes.Transaction("TODO: Get Tenant").CreateTag(t); err != nil {
		render.Render(w, r, errInvalidRequest(err))
		return
	}

	w.Header().Add("Location", fmt.Sprintf("/tags/%d", t.ID))
	if err := json.NewEncoder(w).Encode(t); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}
}

func (s *HTTPServer) handleListTags(w http.ResponseWriter, r *http.Request) {
	tags, err := s.notes.Transaction("TODO: Get Tenant").FindAllTags()
	if err != nil {
		render.Render(w, r, errServerError(err))
		return
	}
	if err := json.NewEncoder(w).Encode(tags); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}
}

func (s *HTTPServer) noteCtx(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		var note *note.Note

		if noteID := chi.URLParam(r, "noteID"); noteID != "" {
			id, err := strconv.ParseUint(noteID, 10, 64)
			if err != nil {
				render.Render(w, r, errInvalidRequest(
					fmt.Errorf("error parsing noteID: %w", err),
				))
				return
			}

			note, err = s.notes.Transaction("TODO: Get Tenant").FindNoteByID(id)

			if err != nil {
				render.Render(w, r, errServerError(
					fmt.Errorf("error loading note: %w", err),
				))
				return
			}

			if note == nil {
				render.Render(w, r, errNotFound)
				return
			}
		} else {
			render.Render(w, r, errInvalidRequest(errors.New("noteID must not be empty")))
			return
		}

		ctx := context.WithValue(r.Context(), "note", note)
		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func (s *HTTPServer) getNote(w http.ResponseWriter, r *http.Request) {
	note := r.Context().Value("note").(*note.Note)
	if err := json.NewEncoder(w).Encode(note); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}
}

func (s *HTTPServer) updateNote(w http.ResponseWriter, r *http.Request) {
	id := r.Context().Value("note").(*note.Note).ID
	n := &note.Note{}
	if err := json.NewDecoder(r.Body).Decode(n); err != nil {
		render.Render(w, r, errInvalidRequest(err))
		return
	}

	if err := s.notes.Transaction("TODO: Get Tenant").UpdateNote(id, n); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (s *HTTPServer) tagCtx(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {

		tagID := chi.URLParam(r, "tagID")
		if tagID == "" {
			render.Render(w, r, errInvalidRequest(errors.New("tagID must not be empty")))
			return
		}

		id, err := strconv.ParseUint(tagID, 10, 64)
		if err != nil {
			render.Render(w, r, errInvalidRequest(
				fmt.Errorf("error parsing tagID: %w", err),
			))
			return
		}

		tag, err := s.notes.Transaction("TODO: Get Tenant").FindTagByID(id)

		if err != nil {
			render.Render(w, r, errServerError(
				fmt.Errorf("error loading tag: %w", err),
			))
			return
		}

		if tag == nil {
			render.Render(w, r, errNotFound)
			return
		}

		ctx := context.WithValue(r.Context(), "tagID", id)
		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func (s *HTTPServer) tagNote(w http.ResponseWriter, r *http.Request) {
	noteID := r.Context().Value("note").(*note.Note).ID
	tagID := r.Context().Value("tagID").(uint64)

	if err := s.notes.Transaction("TODO: Get Tenant").TagNote(noteID, tagID); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (s *HTTPServer) untagNote(w http.ResponseWriter, r *http.Request) {
	noteID := r.Context().Value("note").(*note.Note).ID
	tagID := r.Context().Value("tagID").(uint64)

	if err := s.notes.Transaction("TODO: Get Tenant").UntagNote(noteID, tagID); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (s *HTTPServer) deleteNote(w http.ResponseWriter, r *http.Request) {
	id := r.Context().Value("note").(*note.Note).ID
	if err := s.notes.Transaction("TODO: Get Tenant").DeleteNote(id); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (s *HTTPServer) Serve() error {
	log.Printf("Server listening on %s", s.config.Addr)
	return s.ListenAndServe()
}

// The ErrResponse struct implements render so that chi can generate an HTTP
// response for specific error types

type ErrResponse struct {
	Err            error `json:"-"`
	HTTPStatusCode int   `json:"-"`

	StatusText string `json:"status"`
	ErrorText  string `json:"error,omitempty"`
}

func (e *ErrResponse) Render(w http.ResponseWriter, r *http.Request) error {
	render.Status(r, e.HTTPStatusCode)
	return nil
}

func errInvalidRequest(err error) render.Renderer {
	return &ErrResponse{
		Err:            err,
		HTTPStatusCode: 400,
		StatusText:     "Invalid request.",
		ErrorText:      err.Error(),
	}
}

func errServerError(err error) render.Renderer {
	return &ErrResponse{
		Err:            err,
		HTTPStatusCode: 500,
		StatusText:     "Error processing request.",
		ErrorText:      err.Error(),
	}
}

var errNotFound = &ErrResponse{HTTPStatusCode: 404, StatusText: "Resource not found."}
