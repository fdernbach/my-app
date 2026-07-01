-- Schema PostgreSQL — my-app
-- À exécuter une seule fois sur la base myappdb

CREATE TABLE IF NOT EXISTS users (
    id           UUID         NOT NULL,
    user_name    VARCHAR(255) NOT NULL,
    last_name    VARCHAR(255) NOT NULL,
    first_name   VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    birth_date   DATE,
    -- AddressEmbeddable
    street_number  VARCHAR(255),
    street_name    VARCHAR(255),
    additional_info VARCHAR(255),
    postal_code    VARCHAR(255),
    city           VARCHAR(255),
    country        VARCHAR(255),
    -- AuditDataEmbeddable
    created_at   TIMESTAMPTZ,
    created_by   VARCHAR(255),
    updated_at   TIMESTAMPTZ,
    updated_by   VARCHAR(255),
    -- Optimistic locking
    version      BIGINT,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_user_name UNIQUE (user_name),
    CONSTRAINT uq_users_email     UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS courses (
    id            BIGSERIAL    NOT NULL,
    title         VARCHAR(256) NOT NULL,
    author        VARCHAR(255) NOT NULL,
    document_json JSONB,
    -- AuditDataEmbeddable
    created_at    TIMESTAMPTZ,
    created_by    VARCHAR(255),
    updated_at    TIMESTAMPTZ,
    updated_by    VARCHAR(255),

    CONSTRAINT pk_courses PRIMARY KEY (id)
);
