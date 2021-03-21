#!/bin/bash

version=${NOTES_VERSION:-$(date '+%Y%m%d')}

docker build \
    -t "kendru/cljs-notes:${version}" \
    .

docker push "kendru/cljs-notes:${version}"
