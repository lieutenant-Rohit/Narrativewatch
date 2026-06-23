CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    account_id VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    avg_interval_sec DOUBLE PRECISION DEFAULT 0,
    post_count INTEGER DEFAULT 0,
    follower_count INTEGER DEFAULT 0,
    topic_history TEXT DEFAULT '',
    behavioral_score DOUBLE PRECISION DEFAULT 0.0,
    last_seen TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE post (
    id BIGSERIAL PRIMARY KEY,
    account_id VARCHAR(100) NOT NULL REFERENCES account(account_id),
    text TEXT NOT NULL,
    posted_at TIMESTAMP NOT NULL,
    topic_bucket VARCHAR(50) DEFAULT 'general',
    semantic_score DOUBLE PRECISION DEFAULT 0.0
);

CREATE INDEX idx_post_account_id ON post(account_id);
CREATE INDEX idx_post_posted_at ON post(posted_at);
CREATE INDEX idx_post_topic_bucket ON post(topic_bucket);

CREATE TABLE graph_edge (
    id BIGSERIAL PRIMARY KEY,
    from_account VARCHAR(100) NOT NULL REFERENCES account(account_id),
    to_account VARCHAR(100) NOT NULL REFERENCES account(account_id),
    connected_at TIMESTAMP NOT NULL,
    UNIQUE(from_account, to_account)
);

CREATE INDEX idx_graph_edge_from ON graph_edge(from_account);
CREATE INDEX idx_graph_edge_to ON graph_edge(to_account);

CREATE TABLE temporal_bucket (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(50) NOT NULL,
    window_start TIMESTAMP NOT NULL,
    post_count INTEGER DEFAULT 0,
    suspicious_count INTEGER DEFAULT 0,
    UNIQUE(topic, window_start)
);

CREATE INDEX idx_temporal_window ON temporal_bucket(window_start);

CREATE TABLE alert (
    id BIGSERIAL PRIMARY KEY,
    fired_at TIMESTAMP NOT NULL DEFAULT NOW(),
    narrative TEXT NOT NULL,
    account_count INTEGER NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    signals_fired VARCHAR(100) NOT NULL
);

CREATE TABLE alert_account (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL REFERENCES alert(id),
    account_id VARCHAR(100) NOT NULL REFERENCES account(account_id),
    UNIQUE(alert_id, account_id)
);

CREATE INDEX idx_alert_account_alert ON alert_account(alert_id);