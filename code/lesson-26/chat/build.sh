#!/bin/bash

lein do clean, cljsbuild once min
docker build -t kendru/cljs-chat-ui .