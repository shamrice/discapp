/*

Database creation script.

*/
CREATE DATABASE discapp;

\c discapp

DROP TABLE IF EXISTS discapp_user;
DROP TABLE IF EXISTS prologue;
DROP TABLE IF EXISTS epilogue;
DROP TABLE IF EXISTS thread_body;
DROP TABLE IF EXISTS thread;
DROP TABLE IF EXISTS thread_activity;
DROP TABLE IF EXISTS thread_post_code
DROP TABLE IF EXISTS configuration;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS owner;
DROP TABLE IF EXISTS site_update_log;
DROP TABLE IF EXISTS user_read_thread;


CREATE TABLE owner (
    id serial NOT NULL,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    email varchar UNIQUE NOT NULL,
    enabled BOOL NOT NULL DEFAULT false,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id)
);

CREATE TABLE discapp_user (
    id serial NOT NULL,
    username varchar UNIQUE NOT NULL,
    password varchar(255) NOT NULL,
    email varchar UNIQUE NOT NULL,
    show_email bool NOT NULL DEFAULT FALSE,
    owner_id int,
    enabled bool NOT NULL DEFAULT TRUE,
    is_admin bool NOT NULL DEFAULT FALSE,
    is_user_account NOT NULL DEFAULT TRUE,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    last_login_date TIMESTAMP DEFAULT NOW(),
    locked_until_date TIMESTAMP DEFAULT NULL,
    password_fail_count INT DEFAULT 0,
    last_password_fail_date TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (owner_id) REFERENCES owner(id)
);

CREATE TABLE application (
    id serial NOT NULL,
    name varchar(255) NOT NULL,
    owner_id int NOT NULL,
    enabled bool NOT NULL DEFAULT TRUE,
    deleted bool NOT NULL DEFAULT FALSE,
    searchable bool NOT NULL DEFAULT TRUE,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (owner_id) REFERENCES owner(id)
);

CREATE TABLE configuration (
    id serial NOT NULL,
    application_id int NOT NULL,
    name varchar(255) NOT NULL,
    value varchar NOT NULL,
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
    is_approved bool NOT NULL default true,
    create_dt TIMESTAMP NOT NULL DEFAULT NOW(),
    mod_dt TIMESTAMP NOT NULL DEFAULT NOW(),
    is_admin_post bool NOT NULL default false,
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

CREATE TABLE thread_activity (
    id serial NOT NULL,
    application_id int NOT NULL,
    thread_id int NOT NULL,
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
    is_deleted boolean NOT NULL DEFAULT false,
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
  application_id int NULL,
  key varchar(64) NOT NULL,
  code INT NOT NULL,
  is_redeemed boolean NOT NULL DEFAULT false,
  create_dt TIMESTAMP DEFAULT NOW(),
  exp_dt TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE import_data (
  id serial NOT NULL,
  application_id int UNIQUE NOT NULL,
  import_name varchar(100) NOT NULL,
  import_data bytea NOT NULL,
  create_dt TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (id)
);

CREATE TABLE application_permission (
  id serial NOT NULL,
  application_id int UNIQUE NOT NULL,
  display_ip_address bool NOT NULL DEFAULT TRUE,
  block_bad_words bool NOT NULL DEFAULT FALSE,
  block_search_engines bool NOT NULL DEFAULT FALSE,
  block_anonymous_posting bool NOT NULL DEFAULT FALSE,
  allow_html_permissions varchar(10) NOT NULL,
  unregistered_user_permissions varchar(10) NOT NULL,
  registered_user_permissions varchar(10) NOT NULL,
  create_dt TIMESTAMP DEFAULT NOW(),
  mod_dt TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (id),
  FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE user_permission (
  id serial NOT NULL,
  application_id int NOT NULL,
  discapp_user_id int NOT NULL,
  user_permissions varchar(10) NOT NULL,
  is_active bool NOT NULL DEFAULT TRUE,
  create_dt TIMESTAMP DEFAULT NOW(),
  mod_dt TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (id),
  FOREIGN KEY (application_id) REFERENCES application(id),
  FOREIGN KEY (discapp_user_id) REFERENCES discapp_user(id)
);

CREATE TABLE application_ip_block (
    id serial NOT NULL,
    application_id int NOT NULL,
    ip_address_prefix varchar(64) NOT NULL,
    reason varchar NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);

CREATE TABLE site_update_log (
    id serial NOT NULL,
    subject varchar NOT NULL,
    message varchar NOT NULL,
    enabled boolean NOT NULL DEFAULT TRUE,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id)
);

CREATE TABLE application_subscription (
    id serial NOT NULL,
    application_id INT NOT NULL,
    subscriber_email VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    last_send_dt TIMESTAMP,
    confirmation_code INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);


CREATE TABLE user_read_thread (
    id serial NOT NULL,
    application_id INT NOT NULL,
    discapp_user_id INT NOT NULL,
    read_threads VARCHAR,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id),
    FOREIGN KEY (discapp_user_id) REFERENCES discapp_user(id)
);

CREATE TABLE user_configuration (
    id serial NOT NULL,
    discapp_user_id int NOT NULL,
    name varchar(255) NOT NULL,
    value varchar NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (discapp_user_id) REFERENCES discapp_user(id)
);


CREATE TABLE persistent_logins (
    username varchar(64) not null,
    series varchar(64) primary key,
    token varchar(64) not null,
    last_used timestamp not null
);

CREATE TABLE thread_post_code (
    post_code VARCHAR(255) NOT NULL,
    application_id INT NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (post_code)
);


CREATE TABLE user_registration (
  id serial NOT NULL,
  email varchar(255) UNIQUE NOT NULL,
  key varchar(64) NOT NULL,
  is_redeemed boolean NOT NULL DEFAULT false,
  create_dt TIMESTAMP DEFAULT NOW(),
  redeem_dt TIMESTAMP NULL,
  PRIMARY KEY (id)
);

CREATE TABLE application_report_code (
    id serial NOT NULL,
    application_id INT NOT NULL,
    email varchar(255) NOT NULL,
    code varchar NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);


CREATE TABLE application_favicon (
    id serial NOT NULL,
    application_id INT unique NOT NULL,
    file_name varchar not null,
    favicon_data bytea NOT NULL,
    create_dt TIMESTAMP DEFAULT NOW(),
    mod_dt TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application(id)
);



