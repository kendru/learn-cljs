.PHONY: build
build: clean compile

.PHONY: clean
clean:
	rm -f notes

.PHONY: compile
compile:
	go build -o notes main.go

.PHONY: test
test:
	go test ./...
