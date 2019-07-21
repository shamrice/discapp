/*

Database creation script.

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


commit;


CREATE TABLE application (
    id serial NOT NULL,
    name varchar(255) NOT NULL,
    owner_id int NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (owner_id) REFERENCES owner(id)
);

commit;


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
    subject varchar(65) NOT NULL,
    deleted bool NOT NULL default false,
    show_email bool NOT NULL default false,
    parent_id int NOT NULL DEFAULT 0,
    create_dt TIMESTAMP NOT NULL DEFAULT NOW(),
    mod_dt TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE thread_body (
    id serial NOT NULL,
    application_id int NOT NULL,
    thread_id int NOT NULL,
    body varchar(16384) NOT NULL,
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

commit;


