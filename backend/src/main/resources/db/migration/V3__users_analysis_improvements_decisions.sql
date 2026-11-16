CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE projects ADD COLUMN owner_id UUID REFERENCES users(id) ON DELETE RESTRICT;
CREATE INDEX idx_projects_owner_id ON projects(owner_id);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    owner_id UUID REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO categories (id, name, description) VALUES
    (gen_random_uuid(), 'Desempenho', 'Lentidão, consumo de recursos ou tempo de resposta.'),
    (gen_random_uuid(), 'Autenticação', 'Login, cadastro, sessão ou recuperação de acesso.'),
    (gen_random_uuid(), 'Interface', 'Elementos visuais, legibilidade ou apresentação.'),
    (gen_random_uuid(), 'Navegação', 'Fluxos, busca, organização ou localização de recursos.'),
    (gen_random_uuid(), 'Estabilidade', 'Falhas, travamentos, erros ou indisponibilidade.'),
    (gen_random_uuid(), 'Funcionalidades', 'Comportamento ou ausência de recursos do aplicativo.'),
    (gen_random_uuid(), 'Outros', 'Feedback sem correspondência clara com as demais categorias.');

CREATE TABLE analysis_runs (
    id UUID PRIMARY KEY,
    requested_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    project_id UUID REFERENCES projects(id) ON DELETE RESTRICT,
    application_id UUID REFERENCES applications(id) ON DELETE RESTRICT,
    method_version VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_analysis_scope CHECK (project_id IS NOT NULL OR application_id IS NOT NULL)
);

CREATE TABLE feedback_analyses (
    id UUID PRIMARY KEY,
    analysis_run_id UUID NOT NULL REFERENCES analysis_runs(id) ON DELETE CASCADE,
    feedback_id UUID NOT NULL REFERENCES feedbacks(id) ON DELETE RESTRICT,
    sentiment VARCHAR(20) NOT NULL CHECK (sentiment IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE')),
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    classification_source VARCHAR(20) NOT NULL CHECK (classification_source IN ('AUTOMATIC', 'MANUAL')),
    confidence NUMERIC(5,4),
    original_sentiment VARCHAR(20) CHECK (original_sentiment IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE')),
    original_category_id UUID REFERENCES categories(id) ON DELETE RESTRICT,
    reviewed_by UUID REFERENCES users(id) ON DELETE RESTRICT,
    reviewed_at TIMESTAMPTZ,
    CONSTRAINT uq_analysis_feedback UNIQUE (analysis_run_id, feedback_id)
);

CREATE TABLE issue_groups (
    id UUID PRIMARY KEY,
    analysis_run_id UUID NOT NULL REFERENCES analysis_runs(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    title VARCHAR(180) NOT NULL,
    feedback_count INTEGER NOT NULL CHECK (feedback_count > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issue_group_items (
    id UUID PRIMARY KEY,
    issue_group_id UUID NOT NULL REFERENCES issue_groups(id) ON DELETE CASCADE,
    feedback_analysis_id UUID NOT NULL UNIQUE REFERENCES feedback_analyses(id) ON DELETE CASCADE,
    CONSTRAINT uq_issue_group_item UNIQUE (issue_group_id, feedback_analysis_id)
);

CREATE TABLE improvements (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE RESTRICT,
    issue_group_id UUID REFERENCES issue_groups(id) ON DELETE SET NULL,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    impact SMALLINT NOT NULL CHECK (impact BETWEEN 1 AND 5),
    urgency SMALLINT NOT NULL CHECK (urgency BETWEEN 1 AND 5),
    frequency SMALLINT NOT NULL CHECK (frequency BETWEEN 1 AND 5),
    effort SMALLINT NOT NULL CHECK (effort BETWEEN 1 AND 5),
    assessment_origin VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROPOSED', 'IN_PROGRESS', 'DONE', 'CANCELLED')),
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_improvements_application ON improvements(application_id);

CREATE TABLE decision_runs (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    impact_weight NUMERIC(6,5) NOT NULL CHECK (impact_weight >= 0),
    urgency_weight NUMERIC(6,5) NOT NULL CHECK (urgency_weight >= 0),
    frequency_weight NUMERIC(6,5) NOT NULL CHECK (frequency_weight >= 0),
    effort_weight NUMERIC(6,5) NOT NULL CHECK (effort_weight >= 0),
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_decision_weights_sum CHECK (impact_weight + urgency_weight + frequency_weight + effort_weight = 1.00000)
);

CREATE TABLE decision_scores (
    id UUID PRIMARY KEY,
    decision_run_id UUID NOT NULL REFERENCES decision_runs(id) ON DELETE CASCADE,
    improvement_id UUID NOT NULL REFERENCES improvements(id) ON DELETE RESTRICT,
    improvement_title VARCHAR(160) NOT NULL,
    impact_value SMALLINT NOT NULL,
    urgency_value SMALLINT NOT NULL,
    frequency_value SMALLINT NOT NULL,
    effort_value SMALLINT NOT NULL,
    normalized_impact NUMERIC(10,8) NOT NULL,
    normalized_urgency NUMERIC(10,8) NOT NULL,
    normalized_frequency NUMERIC(10,8) NOT NULL,
    normalized_effort NUMERIC(10,8) NOT NULL,
    final_score NUMERIC(12,8) NOT NULL,
    ranking INTEGER NOT NULL CHECK (ranking > 0),
    CONSTRAINT uq_decision_improvement UNIQUE (decision_run_id, improvement_id)
);
