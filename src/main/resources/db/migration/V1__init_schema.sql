CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL UNIQUE,
    senha_hash  VARCHAR(72)  NOT NULL,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE SEQUENCE conta_numero_seq START 1;

CREATE TABLE conta (
    id          BIGSERIAL PRIMARY KEY,
    numero      VARCHAR(20)    NOT NULL UNIQUE,
    saldo       NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (saldo >= 0),
    usuario_id  BIGINT         NOT NULL UNIQUE REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE transacao (
    id              BIGSERIAL PRIMARY KEY,
    conta_origem    BIGINT         REFERENCES conta (id),
    conta_destino   BIGINT         REFERENCES conta (id),
    tipo            VARCHAR(20)    NOT NULL,
    valor           NUMERIC(15, 2) NOT NULL CHECK (valor > 0),
    data            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT transacao_tipo_check CHECK (tipo IN ('deposito', 'saque', 'transferencia', 'investimento', 'resgate'))
);

CREATE INDEX idx_transacao_origem  ON transacao (conta_origem, data DESC);
CREATE INDEX idx_transacao_destino ON transacao (conta_destino, data DESC);

CREATE TABLE investimento (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT         NOT NULL UNIQUE REFERENCES usuario (id) ON DELETE CASCADE,
    valor       NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (valor >= 0),
    ultima_att  TIMESTAMPTZ    NOT NULL DEFAULT now()
);
