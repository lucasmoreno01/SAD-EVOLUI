ALTER TABLE feedbacks
    ADD COLUMN created_by UUID REFERENCES users(id) ON DELETE RESTRICT,
    ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'MANUAL' CHECK (source IN ('MANUAL', 'EXTERNAL'));

CREATE INDEX idx_feedbacks_created_by ON feedbacks(created_by);
