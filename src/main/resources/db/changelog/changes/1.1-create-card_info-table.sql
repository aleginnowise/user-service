CREATE TABLE IF NOT EXISTS card_info(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    number          varchar(16)  NOT NULL UNIQUE,
    holder          varchar(255) NOT NULL,
    expiration_date varchar(5)   NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users (id)
);