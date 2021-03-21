# BEGIN go-builder
FROM golang:1.15-buster AS go-builder
WORKDIR /build

COPY notes-backend/go.mod notes-backend/go.sum ./
RUN CGO_ENABLED=0 go mod download

COPY notes-backend ./
# Creates /build/notes
RUN make notes-static
# END go-builder

# BEGIN cljs-builder
FROM clojure:openjdk-17-tools-deps-1.10.2.790-slim-buster AS cljs-builder
WORKDIR /build

COPY notes/deps.edn ./
RUN clj -A:fig -P

COPY notes/dev.cljs.edn notes/figwheel-main.edn ./
COPY notes/src ./src
COPY notes/resources ./resources
RUN clj -A:fig -A:min
RUN mkdir resources/public/js && \
    mv target/public/cljs-out/dev-main.js resources/public/js/app.js && \
    sed 's#cljs-out/dev-main.js#/js/app.js#' resources/public/index.html > /tmp/index.html && \
    mv /tmp/index.html resources/public/index.html
# END cljs-builder

FROM scratch

WORKDIR "/srv"

COPY --chown=0:0 --from=go-builder /build/notes-static /srv/notes
COPY --from=cljs-builder /build/resources/public /srv/static

ENTRYPOINT [ "./notes" ]
