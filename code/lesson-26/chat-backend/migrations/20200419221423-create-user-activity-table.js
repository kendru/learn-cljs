'use strict';

var dbm;
var type;
var seed;

/**
  * We receive the dbmigrate dependency from dbmigrate initially.
  * This enables us to not have to rely on NODE_PATH.
  */
exports.setup = function(options, seedLink) {
  dbm = options.dbmigrate;
  type = dbm.dataType;
  seed = seedLink;
};

exports.up = function(db) {
  return db.runSql(`
  CREATE TABLE IF NOT EXISTS "user_activity" (
    username TEXT NOT NULL,
    ts timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (username)
  );
  `);
};

exports.down = function(db) {
  return db.runSql(`
  DROP TABLE IF EXISTS "user_activity";
  `);
};

exports._meta = {
  "version": 1
};
