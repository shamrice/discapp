/*

Database creation script.

MySQL creation script comes as-is and untested. It is provided simply for the sake of those who may
want it. Please use the postgres creation script with a postgres database.

*/
CREATE DATABASE IF NOT EXISTS discapp;

USE discapp;

DROP TABLE IF EXISTS prologue;
DROP TABLE IF EXISTS epilogue;
DROP TABLE IF EXISTS thread_body;
DROP TABLE IF EXISTS thread;
DROP TABLE IF EXISTS configuration;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS owner;

commit;

CREATE TABLE owner (
    id int(20) NOT NULL AUTO_INCREMENT,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    phone varchar(50),
    email varchar(255) UNIQUE NOT NULL,
    enabled BOOL DEFAULT false,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id)
);


CREATE TABLE discapp_user (
  id int(20) NOT NULL AUTO_INCREMENT,
  username varchar(50) UNIQUE NOT NULL,
  password varchar(255) NOT NULL,
  email varchar(255) UNIQUE NOT NULL,
  show_email bool NOT NULL DEFAULT FALSE,
  owner_id int(20),
  enabled bool NOT NULL DEFAULT TRUE,
  is_admin bool NOT NULL DEFAULT FALSE,
  is_user_account NOT NULL DEFAULT TRUE,
  create_dt TIMESTAMP DEFAULT NOW(),
  mod_dt TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (id),
  FOREIGN KEY (owner_id) REFERENCES owner(id)
);


CREATE TABLE application (
    id int(20) NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    owner_id int(20) NOT NULL,
    enabled bool NOT NULL DEFAULT TRUE,
    deleted bool NOT NULL DEFAULT FALSE,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (owner_id) REFERENCES owner(id)
);

commit;


CREATE TABLE configuration (
    id int(20) NOT NULL AUTO_INCREMENT,
    application_id int(20) NOT NULL,
    name varchar(255) NOT NULL,
    value varchar(255) NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE thread (
    id int(255) NOT NULL AUTO_INCREMENT,
    application_id int(20) NOT NULL,
    submitter varchar(50) NOT NULL,
    email varchar(60),
    ip_address varchar(64),
    user_agent varchar(150),
    subject varchar(65) NOT NULL,
    show_email bool NOT NULL default false,
    deleted bool NOT NULL default false,
    parent_id int(255) NOT NULL DEFAULT 0,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE thread_body (
    id int(255) NOT NULL AUTO_INCREMENT,
    application_id int(20) NOT NULL,
    thread_id int(255) NOT NULL,
    body varchar(32768) NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id),
    FOREIGN KEY (thread_id) REFERENCES thread(id)
);

CREATE TABLE prologue (
    id int(255) NOT NULL AUTO_INCREMENT,
    application_id int(20) UNIQUE NOT NULL,
    text varchar(16384) NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE epilogue (
    id int(255) NOT NULL AUTO_INCREMENT,
    application_id int(20) UNIQUE NOT NULL,
    text varchar(16384) NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE reported_abuse (
    id int(255) NOT NULL AUTO_INCREMENT,
    application_id int(255) NOT NULL,
    thread_id int(255) NOT NULL,
    ip_address varchar(64),
    reported_by int(255) NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id),
    FOREIGN KEY (thread_id) REFERENCES thread(id),
    FOREIGN KEY (reported_by) REFERENCES discapp_user(id)
);

CREATE TABLE stats (
  id int(255) NOT NULL AUTO_INCREMENT,
  application_id int(255) NOT NULL,
  stat_date varchar(64) NOT NULL,
  unique_ips INT(255) NOT NULL,
  page_views int(255) NOT NULL,
  create_dt TIMESTAMP DEFAULT NOW(),
  mod_dt TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (id),
  FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE stats_unique_ips (
  id int(255) NOT NULL AUTO_INCREMENT,
  stats_id int(255) NOT NULL,
  ip_address varchar(64) NOT NULL,
  create_dt TIMESTAMP DEFAULT NOW(),
  mod_dt TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (id),
  FOREIGN KEY (stats_id) REFERENCES stats(id)
);

CREATE TABLE password_reset (
  id int(255) NOT NULL AUTO_INCREMENT,
  email varchar(255) UNIQUE NOT NULL,
  key varchar(64) NOT NULL,
  code INT(20) NOT NULL,
  is_redeemed boolean NOT NULL DEFAULT false,
  create_dt TIMESTAMP DEFAULT NOW(),
  exp_dt TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);



commit;


