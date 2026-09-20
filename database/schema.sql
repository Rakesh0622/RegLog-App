-- ============================================================
-- RegLog database schema (MySQL 8.x)
-- Run these statements manually, OR simply start the backend:
-- Hibernate's ddl-auto=update creates the tables automatically.
-- ============================================================

CREATE DATABASE IF NOT EXISTS reglog
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE reglog;

CREATE TABLE IF NOT EXISTS users (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email    VARCHAR(150) NOT NULL UNIQUE,
    phone    VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS jwt_tokens (
    tid   BIGINT AUTO_INCREMENT PRIMARY KEY,
    uid   BIGINT      NOT NULL,
    token TEXT        NOT NULL,
    cat   TIMESTAMP   NOT NULL COMMENT 'created-at',
    eat   TIMESTAMP   NOT NULL COMMENT 'expiry-at',
    CONSTRAINT fk_jwt_tokens_user FOREIGN KEY (uid) REFERENCES users (id)
);