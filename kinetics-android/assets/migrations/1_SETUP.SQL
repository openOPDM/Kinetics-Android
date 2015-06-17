--WARNING! All statements execute in 1 common transaction!

CREATE TABLE 'user' (_id INTEGER PRIMARY KEY AUTOINCREMENT, id INTEGER UNIQUE, email TEXT, first_name TEXT, second_name TEXT, uid TEXT, gender INTEGER, birthday INTEGER);

CREATE TABLE 'test_session' (_id INTEGER PRIMARY KEY AUTOINCREMENT, id INTEGER UNIQUE, type TEXT, raw_data TEXT, score REAL, notes TEXT, valid INTEGER, creation NUMERIC, sync INTEGER NOT NULL DEFAULT 0, timestamp NUMERIC);

--TODO: consider reference to metadata to automate deletion
CREATE TABLE 'ext_data' (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT, metaId INTEGER, test_session_id INTEGER, FOREIGN KEY (test_session_id) REFERENCES test_session(_id) ON DELETE CASCADE);

CREATE TABLE 'ext_metadata' (_id INTEGER PRIMARY KEY AUTOINCREMENT, id INTEGER UNIQUE, name TEXT UNIQUE, type TEXT, props INTEGER);

CREATE TABLE 'ext_list_node' (_id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT, ext_metadata_id INTEGER, FOREIGN KEY (ext_metadata_id) REFERENCES ext_metadata(_id) ON DELETE CASCADE);

--query helper views

--TODO: add indexes to speed up querying!

