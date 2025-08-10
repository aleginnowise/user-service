CREATE TABLE IF NOT EXISTS card_info(
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         uuid         NOT NULL,
    number          varchar(16)  NOT NULL UNIQUE,
    holder          varchar(255) NOT NULL,
    expiration_date DATE   NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users (id)
);