# Account schema

# --- !Ups

CREATE TABLE Account (
    id SERIAL,
    accountName varchar(100) NOT NULL,
    accountKey varchar(40) NOT NULL,
    isActive boolean NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE Account;