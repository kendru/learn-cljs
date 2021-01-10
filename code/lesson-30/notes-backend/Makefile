.PHONY: build clean compile test dev run
build: clean compile

clean:
	rm -rf notes db-data

compile: notes
	
test:
	go test ./...

run:
	go run main.go --config config.dev.toml

dev: 
	go run github.com/codegangsta/gin --immediate main.go --config config.dev.toml

notes:
	go build -o notes main.go
