-- =====================================================================
-- FrotaControl - banco de dados
-- Projeto Integrador - Etapa 4
--
-- Como usar: abra este arquivo no MySQL Workbench e execute tudo
-- (ícone do raio ou Ctrl+Shift+Enter). O script pode ser executado
-- mais de uma vez: ele recria as tabelas e os dados iniciais.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS frotacontrol
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE frotacontrol;

-- A tabela com chaves estrangeiras é removida primeiro
DROP TABLE IF EXISTS registro_uso;
DROP TABLE IF EXISTS motorista;
DROP TABLE IF EXISTS veiculo;
DROP TABLE IF EXISTS usuario;

-- ---------------------------------------------------------------------
-- Usuários do sistema (tela de login)
-- A senha é guardada como hash SHA-256, nunca em texto puro.
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id      INT          NOT NULL AUTO_INCREMENT,
    nome    VARCHAR(100) NOT NULL,
    login   VARCHAR(40)  NOT NULL,
    senha   CHAR(64)     NOT NULL,
    perfil  ENUM('ADMINISTRADOR', 'OPERADOR') NOT NULL,
    ativo   TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_login (login)
);

-- ---------------------------------------------------------------------
-- Veículos da frota
-- ---------------------------------------------------------------------
CREATE TABLE veiculo (
    id                 INT         NOT NULL AUTO_INCREMENT,
    placa              CHAR(7)     NOT NULL,
    marca              VARCHAR(40) NOT NULL,
    modelo             VARCHAR(40) NOT NULL,
    ano                SMALLINT    NOT NULL,
    quilometragem      INT         NOT NULL DEFAULT 0,
    status             ENUM('DISPONIVEL', 'EM_USO', 'EM_MANUTENCAO') NOT NULL DEFAULT 'DISPONIVEL',
    inicio_manutencao  DATE        NULL,
    ativo              TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_veiculo_placa (placa)
);

-- ---------------------------------------------------------------------
-- Motoristas autorizados a retirar veículos
-- ---------------------------------------------------------------------
CREATE TABLE motorista (
    id            INT          NOT NULL AUTO_INCREMENT,
    nome          VARCHAR(100) NOT NULL,
    cpf           CHAR(11)     NOT NULL,
    cnh           VARCHAR(20)  NOT NULL,
    validade_cnh  DATE         NOT NULL,
    telefone      VARCHAR(20)  NULL,
    ativo         TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_motorista_cpf (cpf)
);

-- ---------------------------------------------------------------------
-- Registros de uso: cada saída e a respectiva devolução
-- data_devolucao e km_devolucao ficam vazios enquanto o veículo está na rua.
-- ---------------------------------------------------------------------
CREATE TABLE registro_uso (
    id              INT          NOT NULL AUTO_INCREMENT,
    veiculo_id      INT          NOT NULL,
    motorista_id    INT          NOT NULL,
    usuario_id      INT          NOT NULL,
    data_saida      DATETIME     NOT NULL,
    data_devolucao  DATETIME     NULL,
    km_saida        INT          NOT NULL,
    km_devolucao    INT          NULL,
    destino         VARCHAR(120) NOT NULL,
    observacao      VARCHAR(255) NULL,
    PRIMARY KEY (id),
    KEY ix_registro_data_saida (data_saida),
    CONSTRAINT fk_registro_veiculo   FOREIGN KEY (veiculo_id)   REFERENCES veiculo (id),
    CONSTRAINT fk_registro_motorista FOREIGN KEY (motorista_id) REFERENCES motorista (id),
    CONSTRAINT fk_registro_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuario (id)
);

-- =====================================================================
-- Dados iniciais para teste
-- As datas são calculadas a partir do dia em que o script é executado,
-- para que o histórico e os avisos façam sentido em qualquer data.
-- =====================================================================

-- Senhas: admin123, jose1234 e paula1234
INSERT INTO usuario (id, nome, login, senha, perfil, ativo) VALUES
    (1, 'Renata Lopes', 'admin', SHA2('admin123', 256),  'ADMINISTRADOR', 1),
    (2, 'José Almeida', 'jose',  SHA2('jose1234', 256),  'OPERADOR',      1),
    (3, 'Paula Nunes',  'paula', SHA2('paula1234', 256), 'OPERADOR',      0);

INSERT INTO veiculo (id, placa, marca, modelo, ano, quilometragem, status, inicio_manutencao, ativo) VALUES
    (1, 'ABC1D23', 'Fiat',       'Strada',  2022, 45200, 'EM_USO',        NULL,                          1),
    (2, 'DEF4567', 'Volkswagen', 'Saveiro', 2020, 81350, 'DISPONIVEL',    NULL,                          1),
    (3, 'GHI8J90', 'Renault',    'Master',  2023, 23780, 'EM_MANUTENCAO', CURDATE() - INTERVAL 6 DAY,    1),
    (4, 'JKL1234', 'Toyota',     'Hilux',   2021, 15000, 'EM_USO',        NULL,                          1),
    (5, 'MNO5P67', 'Chevrolet',  'Onix',    2021, 38910, 'DISPONIVEL',    NULL,                          1),
    (6, 'QRS2T45', 'Fiat',       'Fiorino', 2019, 97440, 'DISPONIVEL',    NULL,                          1),
    (7, 'UVW9X87', 'Honda',      'CG 160',  2022, 12305, 'DISPONIVEL',    NULL,                          1);

-- Bruno: CNH perto de vencer. Carlos: CNH vencida. Eduardo: inativo.
INSERT INTO motorista (id, nome, cpf, cnh, validade_cnh, telefone, ativo) VALUES
    (1, 'Ana Ribeiro',    '11122233344', '01234567890', CURDATE() + INTERVAL 3 YEAR,  '(19) 90000-0001', 1),
    (2, 'Bruno Carvalho', '55566677788', '09876543210', CURDATE() + INTERVAL 28 DAY,  '(19) 90000-0002', 1),
    (3, 'Carlos Menezes', '99988877766', '05556667778', CURDATE() - INTERVAL 2 MONTH, '(19) 90000-0003', 1),
    (4, 'Daniela Prado',  '12345678900', '04443332221', CURDATE() + INTERVAL 2 YEAR,  '(19) 90000-0004', 1),
    (5, 'Eduardo Lima',   '22233344455', '07778889990', CURDATE() + INTERVAL 1 YEAR,  '(19) 90000-0005', 0);

-- Utilizações já encerradas nos últimos dias
INSERT INTO registro_uso
    (veiculo_id, motorista_id, usuario_id, data_saida, data_devolucao, km_saida, km_devolucao, destino, observacao) VALUES
    (4, 5, 2, TIMESTAMP(CURDATE() - INTERVAL 7 DAY, '08:45:00'), TIMESTAMP(CURDATE() - INTERVAL 7 DAY, '17:30:00'), 14813, 15000, 'Entrega em Paulínia',  NULL),
    (3, 1, 1, TIMESTAMP(CURDATE() - INTERVAL 6 DAY, '07:30:00'), TIMESTAMP(CURDATE() - INTERVAL 6 DAY, '09:10:00'), 23746, 23780, 'Levar à oficina',      'Barulho na suspensão dianteira.'),
    (1, 2, 2, TIMESTAMP(CURDATE() - INTERVAL 5 DAY, '10:15:00'), TIMESTAMP(CURDATE() - INTERVAL 5 DAY, '14:50:00'), 45104, 45200, 'Coleta em Americana',  NULL),
    (6, 4, 1, TIMESTAMP(CURDATE() - INTERVAL 4 DAY, '09:00:00'), TIMESTAMP(CURDATE() - INTERVAL 4 DAY, '16:20:00'), 97227, 97440, 'Coleta em Limeira',    NULL),
    (5, 1, 2, TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '08:10:00'), TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '11:40:00'), 38852, 38910, 'Banco e cartório',     NULL),
    (2, 2, 2, TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '13:30:00'), TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '17:05:00'), 81208, 81350, 'Entrega em Campinas',  'Sem avarias.');

-- Veículos que estão na rua neste momento (sem data de devolução)
INSERT INTO registro_uso
    (veiculo_id, motorista_id, usuario_id, data_saida, km_saida, destino) VALUES
    (1, 1, 2, NOW() - INTERVAL 95 MINUTE, 45200, 'Entrega em Jaguariúna'),
    (4, 4, 1, NOW() - INTERVAL 40 MINUTE, 15000, 'Visita em Mogi Mirim');

-- Conferência rápida do que foi criado
SELECT 'usuario' AS tabela, COUNT(*) AS registros FROM usuario
UNION ALL SELECT 'veiculo', COUNT(*) FROM veiculo
UNION ALL SELECT 'motorista', COUNT(*) FROM motorista
UNION ALL SELECT 'registro_uso', COUNT(*) FROM registro_uso;
