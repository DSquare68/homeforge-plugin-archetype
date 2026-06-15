-- ============================================================
-- V1__init.sql
-- Initial schema setup for my_plugin_schema.
--
-- Flyway runs this script inside the schema created by TemplateHubPlugin.
-- Rename / extend as needed.
-- ============================================================

-- Example table – replace or delete as needed.
CREATE TABLE IF NOT EXISTS my_plugin_schema.example_items
(
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL,          -- references hub_schema.users.id (logical FK)
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index lookups by user
CREATE INDEX IF NOT EXISTS idx_example_items_user_id
    ON my_plugin_schema.example_items (user_id);
