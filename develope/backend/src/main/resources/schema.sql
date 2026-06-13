-- =============================================================
-- Value Stock Discovery Service — DB Schema DDL
-- Segment: infra | TASK-001
-- =============================================================

-- 1. users 테이블 (회원 인증 — Feature 0)
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(100),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='회원 정보';

-- 2. stocks 테이블 (종목 기본정보 + 현재 지표 — Feature 1/2/3)
CREATE TABLE IF NOT EXISTS stocks (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    stock_code          VARCHAR(20)  NOT NULL,
    stock_name          VARCHAR(255) NOT NULL,
    market              VARCHAR(20)              COMMENT 'KOSPI / KOSDAQ',
    sector              VARCHAR(100)             COMMENT '업종',
    listing_date        DATE,
    ceo_name            VARCHAR(100),
    current_price       BIGINT,
    market_cap          BIGINT,
    shares_outstanding  BIGINT                   COMMENT '발행주식수',
    per                 DOUBLE,
    pbr                 DOUBLE,
    roe                 DOUBLE,
    eps                 BIGINT,
    bps                 BIGINT,
    debt_ratio          DOUBLE,
    operating_margin    DOUBLE,
    dart_corp_code      VARCHAR(20)              COMMENT 'DART 기업코드',
    updated_at          DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uq_stocks_stock_code (stock_code),
    INDEX idx_stocks_market (market),
    INDEX idx_stocks_per (per),
    INDEX idx_stocks_pbr (pbr),
    INDEX idx_stocks_roe (roe),
    INDEX idx_stocks_debt_ratio (debt_ratio)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='종목 기본정보 및 현재 가치지표';

-- 3. financials 테이블 (5년치 재무제표 — Feature 2/3)
CREATE TABLE IF NOT EXISTS financials (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    stock_code          VARCHAR(20) NOT NULL,
    year                INT         NOT NULL,
    revenue             BIGINT               COMMENT '매출액',
    operating_profit    BIGINT               COMMENT '영업이익',
    net_income          BIGINT               COMMENT '순이익',
    total_assets        BIGINT               COMMENT '자산',
    total_liabilities   BIGINT               COMMENT '부채',
    total_equity        BIGINT               COMMENT '자본',
    operating_margin    DOUBLE               COMMENT '영업이익률',
    debt_ratio          DOUBLE               COMMENT '부채비율',
    roe                 DOUBLE,
    eps                 BIGINT,
    bps                 BIGINT,
    per                 DOUBLE,
    pbr                 DOUBLE,
    PRIMARY KEY (id),
    UNIQUE KEY uq_financials_code_year (stock_code, year),
    INDEX idx_financials_stock_code (stock_code),
    CONSTRAINT fk_financials_stock_code
        FOREIGN KEY (stock_code) REFERENCES stocks (stock_code)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='종목별 연도별 재무제표 (최근 5년)';
