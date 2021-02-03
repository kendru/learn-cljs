'use strict';

var dbm;
var type;
var seed;

/**
  * We receive the dbmigrate dependency from dbmigrate initially.
  * This enables us to not have to rely on NODE_PATH.
  */
exports.setup = function (options, seedLink) {
  dbm = options.dbmigrate;
  type = dbm.dataType;
  seed = seedLink;
};

exports.up = function (db) {
  return db.runSql(`
    CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
    BEGIN
        NEW.updated_at = now();
        RETURN NEW;
    END;
    $$ language 'plpgsql';

    CREATE TABLE IF NOT EXISTS "user" (
      username TEXT NOT NULL,
      first_name TEXT,
      last_name TEXT,
      password_digest TEXT NOT NULL,
      created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (username)
    );
    CREATE TRIGGER update_user_updated_at BEFORE UPDATE ON "user" FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

    CREATE TABLE IF NOT EXISTS "room" (
      id SERIAL,
      name TEXT,
      created_by TEXT NOT NULL REFERENCES "user" (username),
      created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id)
    );
    CREATE TRIGGER update_room_updated_at BEFORE UPDATE ON "room" FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

    CREATE TABLE IF NOT EXISTS "room_message" (
      id SERIAL,
      content TEXT,
      room_id INT NOT NULL REFERENCES "room" (id) ON DELETE CASCADE,
      sender TEXT NOT NULL REFERENCES "user" (username) ON DELETE CASCADE,
      "timestamp" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id)
    );
    CREATE INDEX IF NOT EXISTS "room_message_by_room_feed" ON "room_message" ( room_id ASC, "timestamp" desc );

    CREATE TABLE IF NOT EXISTS "private_message" (
      content TEXT,
      sender TEXT NOT NULL REFERENCES "user" (username) ON DELETE CASCADE,
      recipient TEXT NOT NULL REFERENCES "user" (username) ON DELETE CASCADE,
      "timestamp" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
    CREATE INDEX IF NOT EXISTS "private_message_by_sender_feed" ON "private_message" ( sender ASC, "timestamp" desc );
    CREATE INDEX IF NOT EXISTS "private_message_by_recipient_feed" ON "private_message" ( recipient ASC, "timestamp" desc );
    `);
};

exports.down = function (db) {
  return db.runSql(`
  DROP TABLE IF EXISTS "private_message";
  DROP TABLE IF EXISTS "room_message";
  DROP TABLE IF EXISTS "room";
  DROP TABLE IF EXISTS "user";
  DROP FUNCTION IF EXISTS "update_updated_at_column";
  `);
};

exports._meta = {
  "version": 1
};
