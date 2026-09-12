CREATE DATABASE IF NOT EXISTS app
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE app;

-- CREATE TABLE IF NOT EXISTS permission (
--     id BINARY(16) NOT NULL,
--     created_at DATETIME(6),
--     updated_at DATETIME(6),
--     code VARCHAR(80) NOT NULL,
--     description VARCHAR(200),
--     PRIMARY KEY (id),
--     UNIQUE KEY uk_permission_code (code)
-- );
--
-- CREATE TABLE IF NOT EXISTS adminRole (
--     id BINARY(16) NOT NULL,
--     created_at DATETIME(6),
--     updated_at DATETIME(6),
--     name VARCHAR(60) NOT NULL,
--     description VARCHAR(200),
--     PRIMARY KEY (id),
--     UNIQUE KEY uk_role_name (name)
-- );
--
-- CREATE TABLE IF NOT EXISTS admin (
--     id BINARY(16) NOT NULL,
--     created_at DATETIME(6),
--     updated_at DATETIME(6),
--     email VARCHAR(255) NOT NULL,
--     password VARCHAR(255) NOT NULL,
--     status VARCHAR(255) NOT NULL,
--     enabled BIT(1),
--     PRIMARY KEY (id),
--     UNIQUE KEY uk_admin_email (email)
-- );
--
-- CREATE TABLE IF NOT EXISTS role_permissions (
--     role_id BINARY(16) NOT NULL,
--     permission_id BINARY(16) NOT NULL,
--     CONSTRAINT uk_role_permissions UNIQUE (role_id, permission_id)
-- );
--
-- CREATE TABLE IF NOT EXISTS admin_roles (
--     admin_id BINARY(16) NOT NULL,
--     role_id BINARY(16) NOT NULL,
--     CONSTRAINT uk_user_roles UNIQUE (admin_id, role_id)
-- );
