-- V1__init_schema.sql
-- Initial schema for store-api
-- Column names follow Spring JPA default naming (camelCase -> snake_case)

CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(255) NOT NULL,
    role       VARCHAR(50),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE TABLE products
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description TEXT,
    price       NUMERIC(19, 2) NOT NULL,
    stock       INTEGER        NOT NULL DEFAULT 0,
    category    VARCHAR(255),
    created_at  TIMESTAMP
);

CREATE TABLE orders
(
    id               BIGSERIAL PRIMARY KEY,
    order_code       VARCHAR(255)   NOT NULL UNIQUE,
    user_id          BIGINT REFERENCES users (id),
    total_amount     NUMERIC(19, 2) NOT NULL,
    status           VARCHAR(50),
    payment_method   VARCHAR(50),
    shipping_address VARCHAR(255),
    note             VARCHAR(255),
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP
);

CREATE TABLE order_items
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT REFERENCES orders (id),
    product_id BIGINT REFERENCES products (id),
    quantity   INTEGER        NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL
);
