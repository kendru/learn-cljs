FROM node:12

COPY out /srv/out
COPY node_modules /srv/node_modules

WORKDIR /srv

CMD [ "nodejs", "out/main.js" ]