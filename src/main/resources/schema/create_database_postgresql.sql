/*

Database creation script.

*/
CREATE DATABASE IF NOT EXISTS discapp;

USE discapp;

DROP TABLE IF EXISTS discapp_user;
DROP TABLE IF EXISTS prologue;
DROP TABLE IF EXISTS epilogue;
DROP TABLE IF EXISTS thread_body;
DROP TABLE IF EXISTS thread;
DROP TABLE IF EXISTS configuration;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS owner;

commit;

CREATE TABLE owner (
    id serial NOT NULL,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    phone varchar(50),
    email varchar(255) UNIQUE NOT NULL,
    enabled BOOL NOT NULL DEFAULT false,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id)
);

CREATE TABLE discapp_user (
    id serial NOT NULL,
    username varchar(50) UNIQUE NOT NULL,
    password varchar(255) NOT NULL,
    email varchar(255) UNIQUE NOT NULL,
    show_email bool NOT NULL DEFAULT FALSE,
    owner_id int,
    enabled bool NOT NULL DEFAULT TRUE,
    is_admin bool NOT NULL DEFAULT FALSE,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (owner_id) REFERENCES owner(id)
);

CREATE TABLE application (
    id serial NOT NULL,
    name varchar(255) NOT NULL,
    owner_id int NOT NULL,
    enabled bool NOT NULL DEFAULT TRUE,
    deleted bool NOT NULL DEFAULT FALSE,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (owner_id) REFERENCES owner(id)
);

CREATE TABLE configuration (
    id serial NOT NULL,
    application_id int NOT NULL,
    name varchar(255) NOT NULL,
    value varchar(255) NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE thread (
    id serial NOT NULL,
    application_id int NOT NULL,
    submitter varchar(50) NOT NULL,
    email varchar(60),
    ip_address varchar(64),
    user_agent varchar(150),
    subject varchar(65) NOT NULL,
    deleted bool NOT NULL default false,
    show_email bool NOT NULL default false,
    parent_id int NOT NULL DEFAULT 0,
    discapp_user_id int DEFAULT NULL,
    create_dt TIMESTAMP NOT NULL DEFAULT NOW(),
    mod_dt TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE thread_body (
    id serial NOT NULL,
    application_id int NOT NULL,
    thread_id int NOT NULL,
    body varchar(32768) NOT NULL,
    create_dt TIMESTAMP NOT NULL DEFAULT NOW(),
    mod_dt TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id),
    FOREIGN KEY (thread_id) REFERENCES thread(id)
);

CREATE TABLE prologue (
    id serial NOT NULL,
    application_id int UNIQUE NOT NULL,
    text varchar(16384) NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE epilogue (
    id serial NOT NULL,
    application_id int UNIQUE NOT NULL,
    text varchar(16384) NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE reported_abuse (
    id serial NOT NULL,
    application_id int NOT NULL,
    thread_id int NOT NULL,
    ip_address varchar(64),
    reported_by int NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id),
    FOREIGN KEY (thread_id) REFERENCES thread(id),
    FOREIGN KEY (reported_by) REFERENCES discapp_user(id)
);

CREATE TABLE stats (
  id serial NOT NULL,
  application_id int NOT NULL,
  stat_date varchar(64) NOT NULL,
  unique_ips INT NOT NULL,
  page_views int NOT NULL,
  create_dt TIMESTAMP DEFAULT NOW(),
  mod_dt TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (id),
  FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE stats_unique_ips (
  id serial NOT NULL,
  stats_id int NOT NULL,
  ip_address varchar(64) NOT NULL,
  create_dt TIMESTAMP DEFAULT NOW(),
  mod_dt TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (id),
  FOREIGN KEY (stats_id) REFERENCES stats(id)
);

CREATE TABLE password_reset (
  id serial NOT NULL,
  email varchar(255) UNIQUE NOT NULL,
  key varchar(64) NOT NULL,
  code INT NOT NULL,
  is_redeemed boolean NOT NULL DEFAULT false,
  create_dt TIMESTAMP DEFAULT NOW(),
  exp_dt TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

commit;


