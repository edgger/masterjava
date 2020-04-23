DROP TABLE IF EXISTS user_group;
DROP TABLE IF EXISTS users;
DROP SEQUENCE IF EXISTS user_seq;
DROP TYPE IF EXISTS user_flag;
DROP TABLE IF EXISTS groups;
DROP SEQUENCE IF EXISTS group_seq;
DROP TYPE IF EXISTS group_status;
DROP TABLE IF EXISTS projects;
DROP SEQUENCE IF EXISTS project_seq;
DROP TABLE IF EXISTS cities;

CREATE TABLE cities
(
  id   VARCHAR(255) PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE SEQUENCE project_seq START 100000;

CREATE TABLE projects
(
  id          INTEGER PRIMARY KEY DEFAULT nextval('project_seq'),
  name        TEXT NOT NULL,
  description TEXT NOT NULL
);

CREATE UNIQUE INDEX project_name_idx ON projects (name);

CREATE TYPE user_flag AS ENUM ('active', 'deleted', 'superuser');

CREATE SEQUENCE user_seq START 100000;

CREATE TABLE users
(
  id        INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
  full_name TEXT      NOT NULL,
  email     TEXT      NOT NULL,
  city_id   VARCHAR(255) REFERENCES cities (id) ON UPDATE CASCADE,
  flag      user_flag NOT NULL
);

CREATE UNIQUE INDEX user_email_idx ON users (email);

CREATE TYPE group_status AS ENUM ('REGISTERING', 'CURRENT', 'FINISHED');

CREATE SEQUENCE group_seq START 100000;

CREATE TABLE groups
(
  id         INTEGER PRIMARY KEY DEFAULT nextval('group_seq'),
  name       TEXT         NOT NULL,
  project_id INTEGER REFERENCES projects (id) ON UPDATE CASCADE ON DELETE CASCADE,
  status     group_status NOT NULL
);

CREATE TABLE user_group
(
  user_id  INTEGER REFERENCES users (id) ON UPDATE CASCADE,
  group_id INTEGER REFERENCES groups (id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT user_group_pkey PRIMARY KEY (user_id, group_id)
);