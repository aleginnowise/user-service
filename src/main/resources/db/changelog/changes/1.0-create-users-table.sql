CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users(
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name       varchar(255) NOT NULL,
    surname    varchar(255) NOT NULL,
    birth_date DATE         NOT NULL,
    email      varchar(255) NOT NULL UNIQUE
);