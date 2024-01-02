CREATE DATABASE IF NOT EXISTS legend;
use legend;

CREATE TABLE IF NOT EXISTS legend.groups
(
    name   VARCHAR(20) PRIMARY KEY NOT NULL,
    weight INT                     NOT NULL,
    prefix VARCHAR(20)             NOT NULL
);

CREATE TABLE IF NOT EXISTS legend.group_member
(
    id         BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    uuid       BINARY(16)         NOT NULL,
    name       VARCHAR(20)        NOT NULL,
    expiration DATETIME           NULL,
    FOREIGN KEY (name) REFERENCES legend.groups (name)
);

CREATE TABLE IF NOT EXISTS legend.group_permission
(
    id         BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    permission VARCHAR(50)        NOT NULL,
    name       VARCHAR(20)        NOT NULL,
    FOREIGN KEY (name) REFERENCES legend.groups (name)
);