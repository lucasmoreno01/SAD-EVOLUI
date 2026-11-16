ALTER TABLE improvements RENAME COLUMN frequency TO legacy_frequency;

ALTER TABLE decision_scores
    ALTER COLUMN frequency_value TYPE INTEGER USING frequency_value::INTEGER;

ALTER TABLE decision_runs
    ADD COLUMN frequency_basis VARCHAR(32) NOT NULL DEFAULT 'MANUAL_SCALE';
