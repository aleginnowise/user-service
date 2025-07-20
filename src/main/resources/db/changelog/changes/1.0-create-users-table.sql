CREATE TABLE IF NOT EXISTS users(
    id         BIGSERIAL PRIMARY KEY,
    name       varchar(255) NOT NULL,
    surname    varchar(255) NOT NULL,
    birth_date DATE         NOT NULL,
    email      varchar(255) NOT NULL UNIQUE
);