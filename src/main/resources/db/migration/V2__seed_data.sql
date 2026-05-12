-- Seed data. Password for every user: "senha123" (BCrypt hashed below).

INSERT INTO usuario (id, nome, email, senha_hash) VALUES
    (1, 'Joao Silva',     'joao@email.com',   '$2a$10$fy1UbQcOh5tYVPpfzhX5ceRqLpA1OGa7hsalIwmD2oiNXrnlbSu66'),
    (2, 'Maria Souza',    'maria@email.com',  '$2a$10$RKTd2MqFzY2rbDaDbJ.e6O3Epq1mq4rSUp2RG4mL71Z2jIQN2cAhy'),
    (3, 'Pedro Oliveira', 'pedro@email.com',  '$2a$10$IYqkeWtINn2J2KoqJOmbdei2eQHujloxIPGZttWNc3XHfdMANvkxG'),
    (4, 'Ana Costa',      'ana@email.com',    '$2a$10$QWJ9cfgoy1BIvqGLNM4PLOr6uSbTQiipM7XcssvQH1gYxYidGCHgu'),
    (5, 'Carlos Pereira', 'carlos@email.com', '$2a$10$FjEC7AwH5wj86mqw0OsOqeg96ae1nE7dWWQU1EwGEiPInDjZvz8uW');

SELECT setval('usuario_id_seq', (SELECT MAX(id) FROM usuario));

INSERT INTO conta (id, numero, saldo, usuario_id) VALUES
    (1, 'C00001',  1500.00, 1),
    (2, 'C00002',  9999.99, 2),
    (3, 'C00003',     0.00, 3),
    (4, 'C00004', 25000.00, 4),
    (5, 'C00005',   100.00, 5);

SELECT setval('conta_id_seq', (SELECT MAX(id) FROM conta));
SELECT setval('conta_numero_seq', 5);

INSERT INTO investimento (usuario_id, valor, ultima_att) VALUES
    (2,  500.00, now() - interval '5 minutes'),
    (4, 1500.00, now() - interval '2 minutes');

INSERT INTO transacao (conta_destino, tipo, valor, data) VALUES
    (1, 'deposito', 1500.00, now() - interval '7 days'),
    (2, 'deposito',  500.00, now() - interval '5 days'),
    (4, 'deposito', 5000.00, now() - interval '6 days'),
    (5, 'deposito',  100.00, now() - interval '1 day');

INSERT INTO transacao (conta_origem, tipo, valor, data) VALUES
    (1, 'saque',         100.00, now() - interval '3 days'),
    (4, 'saque',         200.00, now() - interval '2 days'),
    (2, 'investimento',  500.00, now() - interval '5 minutes'),
    (4, 'investimento', 1500.00, now() - interval '2 minutes');

INSERT INTO transacao (conta_origem, conta_destino, tipo, valor, data) VALUES
    (4, 1, 'transferencia',  50.00, now() - interval '4 days'),
    (4, 5, 'transferencia', 100.00, now() - interval '1 day');
