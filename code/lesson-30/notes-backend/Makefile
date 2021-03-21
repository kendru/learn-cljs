.PHONY: build clean compile compile-static test dev run
build: clean compile

clean:
	rm -rf notes db-data

compile: notes

compile-static: notes-static
	
test:
	go test ./...

run:
	go run main.go --config config.dev.toml

dev: 
	go run github.com/codegangsta/gin --immediate main.go --config config.dev.toml

notes:
	go build -o notes main.go

notes-static:
	CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -ldflags="-w -s" -o notes-static main.go 
