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
  INSERT INTO "user" (username, first_name, last_name, password_digest)
    VALUES ('system', 'System', 'User', 'no login');
  INSERT INTO "room" (name, created_by)
    VALUES ('Lobby', 'system');
  `);
};

exports.down = function(db) {
  return db.runSql(`
  DELETE FROM "room" where name = 'Lobby';
  DELETE FROM "user" where username = 'system';
  `);
};

exports._meta = {
  "version": 1
};
