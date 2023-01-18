# chat-backend

chat backend service

## Overview

this will serve as the backend for the chat application
which will store users, conversations, rooms, and messages

## Setup

make sure to install [leiningnen](https://leiningen.org/)

make sure to use version [node@12](https://nodejs.org/en/download/releases/)

```shell
npm install
npm update db-migrate db-migrate-pg
npm run build
npm run start:dev
npm run migrations:up
```

## License

Copyright © 2020 Andrew Meredith

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
