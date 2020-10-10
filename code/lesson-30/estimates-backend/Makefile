.PHONY: build
build: clean compile

.PHONY: clean
clean:
	rm -f estimates

.PHONY: compile
compile:
	go build -o estimates main.go

.PHONY: test
test:
	go test ./...
