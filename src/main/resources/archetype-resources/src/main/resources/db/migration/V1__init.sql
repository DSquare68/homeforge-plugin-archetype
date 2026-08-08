-- ============================================================
-- V1__init.sql
-- Initial schema setup for ${pluginSchema}.
--
-- Flyway runs this script inside the schema created by HubPluginImpl.
-- Rename / extend as needed.
-- ============================================================

-- Example table - replace or delete as needed.
CREATE TABLE IF NOT EXISTS ${pluginSchema}.example_items
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
    ON ${pluginSchema}.example_items (user_id);
