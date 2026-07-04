-- Core structural DDL for web-reviewer-bot.
-- Applied on startup (spring.sql.init.mode=always). Keep every statement idempotent.
--
-- NOTE: The MVP does not persist assessments (see SPEC.md > Non-goals). These
-- tables scaffold the deferred "saved history / comparison" feature so the
-- schema is ready when persistence is wired into the service layer.

CREATE TABLE IF NOT EXISTS analysis (
    id            BIGSERIAL   PRIMARY KEY,
    url           TEXT        NOT NULL,
    page_title    TEXT,
    overall_score INTEGER     NOT NULL,
    rating        VARCHAR(16) NOT NULL,
    verdict       TEXT,
    summary       TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS analysis_dimension (
    id          BIGSERIAL   PRIMARY KEY,
    analysis_id BIGINT      NOT NULL REFERENCES analysis (id) ON DELETE CASCADE,
    name        VARCHAR(64) NOT NULL,
    score       INTEGER     NOT NULL,
    reasoning   TEXT
);

CREATE INDEX IF NOT EXISTS idx_analysis_created_at ON analysis (created_at);
CREATE INDEX IF NOT EXISTS idx_analysis_dimension_analysis_id ON analysis_dimension (analysis_id);
