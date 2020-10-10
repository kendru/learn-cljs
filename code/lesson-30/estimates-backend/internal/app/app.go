package app

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"

	"learn-cljs.com/estimates/internal/session"

	"github.com/gorilla/mux"
	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

type App struct {
	*session.SessionManager

	srv http.Server
	config Config
}

func NewApp(c Config) *App {
	var app = &App{
		SessionManager: session.NewSessionManager(),
		config: c,
	}

	r := mux.NewRouter()

	r.HandleFunc("/ws", app.handleSocketConnect)

	r.PathPrefix("/").Handler(http.FileServer(http.Dir(c.StaticFileDir)))

	app.srv = http.Server{
		Addr:    c.Addr,
		Handler: r,
	}

	return app
}

func (a *App) Serve() {
	log.Printf("Server listening on %s", a.config.Addr)
	log.Fatal(a.srv.ListenAndServe())
}

func (a *App) handleSocketConnect(w http.ResponseWriter, r *http.Request) {
	var err error
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println(err)
		return
	}

	var isClosed bool
	conn.SetCloseHandler(func(code int, text string) error {
		log.Printf("Connection Closed: [%d] %s\n", code, text)
		isClosed = true
		return nil
	})

	for {
		if isClosed {
			log.Println("Not continuing for closed connection")
			return
		}
		msgType, data, err := conn.ReadMessage()
		if err != nil {
			log.Println("Error reading", err)
			continue
		}
		log.Println("Got message", msgType, string(data))

		var req Message
		var reply *Message
		json.Unmarshal(data, &req)
		switch req.Type {
		case StartSession:
			in := new(StartSessionPayload)
			err = json.Unmarshal(req.Payload, in)
			session, err := a.StartSession(in.Name)
			if err != nil {
				break
			}
			reply, err = newSessionStartedMessage(session.ID, session.AccessKey)
		case ListSessions:
			sessions, err := a.ListSessions()
			if err != nil {
				break
			}
			reply, err = newSessionListMessage(sessions)
		default:
			reply, err = newErrorMessage(fmt.Sprintf("Unrecognized message type: %s", req.Type))
		}

		if err != nil {
			reply, err = newErrorMessage(fmt.Sprintf("Error handling request: %s", err))
			if err != nil {
				log.Println("Cannot create error response", err)
				continue
			}
		}

		if reply != nil {
			replayData, err := json.Marshal(&reply)
			if err != nil {
				log.Println("Cannot marshal response", err)
				continue
			}
			if err := conn.WriteMessage(msgType, replayData); err != nil {
				log.Println("Error writing response", err)
				return
			}
		}
	}
}

type Config struct {
	Addr          string
	StaticFileDir string
}
