package transport

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"net/http"
	"strconv"
	"time"

	"learn-cljs.com/notes/internal/note"

	"github.com/go-chi/chi"
	"github.com/go-chi/chi/middleware"
	"github.com/go-chi/render"
)

type HTTPServer struct {
	srv    http.Server
	config Config

	notes note.Service
}

type Config struct {
	Addr          string
	StaticFileDir string
}

func NewHTTPServer(c Config, notes note.Service) *HTTPServer {
	var s = &HTTPServer{
		config: c,

		notes: notes,
	}

	s.srv = http.Server{
		Addr:    c.Addr,
		Handler: s.newRouter(c.StaticFileDir),
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
	)

	r.Route("/notes", func(r chi.Router) {
		r.Post("/", s.handleCreateNote)
		r.Get("/", s.handleListNotes)

		r.Route("/{noteID}", func(r chi.Router) {
			r.Use(s.noteCtx) // Add note to context based on noteID route param
			r.Get("/", s.getNote)
			r.Put("/", s.updateNote)
			r.Delete("/", s.deleteNote)
		})
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
	if err := s.notes.Create(n); err != nil {
		render.Render(w, r, errInvalidRequest(err))
		return
	}
	w.WriteHeader(http.StatusCreated)
}

func (s *HTTPServer) handleListNotes(w http.ResponseWriter, r *http.Request) {
	notes, err := s.notes.FindAll()
	if err != nil {
		render.Render(w, r, errServerError(err))
		return
	}
	if err := json.NewEncoder(w).Encode(notes); err != nil {
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

			note, err = s.notes.FindByID(id)

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

	if err := s.notes.Update(id, n); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (s *HTTPServer) deleteNote(w http.ResponseWriter, r *http.Request) {
	id := r.Context().Value("note").(*note.Note).ID
	if err := s.notes.Delete(id); err != nil {
		render.Render(w, r, errServerError(err))
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (s *HTTPServer) Serve() {
	log.Printf("Server listening on %s", s.config.Addr)
	log.Fatal(s.srv.ListenAndServe())
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
