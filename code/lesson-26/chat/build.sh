#!/bin/bash

rm -rf target/*
clj -A fig:min
docker build -t kendru/cljs-chat-ui .
