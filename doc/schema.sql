-- =============================================================
-- DB: td (MySQL 8)
-- 생성 순서: 의존 관계 없는 테이블 먼저
-- =============================================================

-- -------------------------------------------------------------
-- 1. 시장 지표 (market_index)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS market_index (
    base_dt      CHAR(8)        NOT NULL COMMENT '기준일자 (YYYYMMDD)',
    kospi_close  DECIMAL(10, 2) NOT NULL COMMENT '코스피 종가 지수',
    kospi_vs     DECIMAL(10, 2) NOT NULL COMMENT '코스피 전일 대비 등락',
    kosdaq_close DECIMAL(10, 2) NOT NULL COMMENT '코스닥 종가 지수',
    kosdaq_vs    DECIMAL(10, 2) NOT NULL COMMENT '코스닥 전일 대비 등락',
    usd_krw_rate DECIMAL(10, 2) NOT NULL COMMENT '달러-원화 환율',
    usd_krw_vs   DECIMAL(10, 2) NOT NULL COMMENT '환율 전일 대비 등락',
    PRIMARY KEY (base_dt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='시장 전체 지표 (코스피/코스닥/환율)';

-- -------------------------------------------------------------
-- 2. 회사 (company) — DART API
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS company (
    corp_code  CHAR(8)      NOT NULL COMMENT 'DART 고유번호 (8자리)',
    stock_code CHAR(6)      NOT NULL COMMENT '종목코드 (6자리)',
    isinCd     VARCHAR(12)      NULL COMMENT 'ISIN 코드 (12자리) — stock_price 적재 후 역참조 저장',
    corp_name  VARCHAR(100) NOT NULL COMMENT '법인명',
    corp_cls   CHAR(1)      NOT NULL COMMENT '법인구분 (Y:유가 K:코스닥 N:코넥스 E:기타)',
    PRIMARY KEY (corp_code),
    UNIQUE KEY uq_company_stock_code (stock_code),
    UNIQUE KEY uq_company_isincd (isinCd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DART 기준 상장 법인 목록';

-- -------------------------------------------------------------
-- 3. 유저 (user)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    user_id    BIGINT NOT NULL AUTO_INCREMENT COMMENT '유저 고유 ID',
    -- 인증 방식 확정 후 컬럼 추가 예정
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='서비스 회원';

-- -------------------------------------------------------------
-- 4. 재무제표 (financial_statement) — DART fnlttMultiAcnt API
-- 적재 순서 2a단계
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financial_statement (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    bsns_year           CHAR(4)      NOT NULL COMMENT '사업연도',
    stock_code          CHAR(6)      NOT NULL COMMENT '종목코드',
    reprt_code          CHAR(5)      NOT NULL COMMENT '보고서코드 (11011/11012/11013/11014)',
    account_nm          VARCHAR(100) NOT NULL COMMENT '계정명',
    fs_div              VARCHAR(5)   NOT NULL COMMENT '개별/연결구분 (OFS/CFS)',
    fs_nm               VARCHAR(50)      NULL COMMENT '개별/연결명',
    sj_div              VARCHAR(5)   NOT NULL COMMENT '재무제표구분 (BS/IS)',
    sj_nm               VARCHAR(50)      NULL COMMENT '재무제표명',
    thstrm_nm           VARCHAR(50)      NULL COMMENT '당기명',
    thstrm_dt           VARCHAR(30)      NULL COMMENT '당기일자',
    thstrm_amount       BIGINT           NULL COMMENT '당기금액',
    thstrm_add_amount   BIGINT           NULL COMMENT '당기누적금액',
    frmtrm_nm           VARCHAR(50)      NULL COMMENT '전기명',
    frmtrm_dt           VARCHAR(30)      NULL COMMENT '전기일자',
    frmtrm_amount       BIGINT           NULL COMMENT '전기금액',
    frmtrm_add_amount   BIGINT           NULL COMMENT '전기누적금액',
    bfefrmtrm_nm        VARCHAR(50)      NULL COMMENT '전전기명 (사업보고서만)',
    bfefrmtrm_dt        VARCHAR(30)      NULL COMMENT '전전기일자',
    bfefrmtrm_amount    BIGINT           NULL COMMENT '전전기금액',
    ord                 INT              NULL COMMENT '계정과목 정렬순서',
    currency            VARCHAR(10)      NULL COMMENT '통화단위',
    PRIMARY KEY (id),
    KEY idx_fs_stock (stock_code),
    CONSTRAINT fk_fs_company FOREIGN KEY (stock_code) REFERENCES company (stock_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DART 재무제표 원본 데이터';

-- -------------------------------------------------------------
-- 5. 배당 정보 (dividend_info) — 금융위원회 GetStocDiviInfoService_V2 API (JSON)
-- 적재 순서 4단계
-- 전체 페이지 수집 후 isinCd == company.isinCd 로 매칭
-- dividend_yield 계산 시 stckGenrDvdnAmt ÷ 현재 주가 × 100 으로 사용
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS dividend_info (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    isinCd            VARCHAR(12)    NOT NULL COMMENT 'ISIN 코드 (12자리) — company.isinCd 참조',
    basDt             CHAR(8)        NOT NULL COMMENT '기준일자 (YYYYMMDD) — 데이터 갱신 기준일',
    crno              VARCHAR(13)        NULL COMMENT '법인등록번호 (13자리)',
    stckIssuCmpyNm    VARCHAR(200)       NULL COMMENT '주식발행회사명',
    dvdnBasDt         CHAR(8)            NULL COMMENT '배당기준일자 (YYYYMMDD)',
    cashDvdnPayDt     CHAR(8)            NULL COMMENT '현금배당지급일자 (YYYYMMDD)',
    stckHndvDt        CHAR(8)            NULL COMMENT '주식교부일자 (YYYYMMDD)',
    isinCdNm          VARCHAR(200)       NULL COMMENT 'ISIN 코드명',
    stckDvdnRcd       VARCHAR(2)         NULL COMMENT '주식배당사유코드',
    stckDvdnRcdNm     VARCHAR(100)       NULL COMMENT '주식배당사유코드명',
    trsnmDptyDcd      VARCHAR(2)         NULL COMMENT '명의개서대리인구분코드',
    trsnmDptyDcdNm    VARCHAR(100)       NULL COMMENT '명의개서대리인구분코드명',
    scrsItmsKcd       VARCHAR(4)         NULL COMMENT '유가증권종목종류코드',
    scrsItmsKcdNm     VARCHAR(100)       NULL COMMENT '유가증권종목종류코드명',
    stckGenrDvdnAmt   DECIMAL(22, 3)     NULL COMMENT '주식일반배당금액 — 1주당 현금 배당 금액',
    stckGrdnDvdnAmt   DECIMAL(22, 3)     NULL COMMENT '주식차등배당금액',
    stckGenrCashDvdnRt DECIMAL(26, 10)   NULL COMMENT '주식일반현금배당률',
    stckGenrDvdnRt    DECIMAL(26, 10)    NULL COMMENT '주식일반배당률',
    cashGrdnDvdnRt    DECIMAL(26, 10)    NULL COMMENT '현금차등배당률',
    stckGrdnDvdnRt    DECIMAL(26, 10)    NULL COMMENT '주식차등배당률',
    stckParPrc        DECIMAL(22, 3)     NULL COMMENT '주식액면가',
    stckStacMd        VARCHAR(4)         NULL COMMENT '주식결산월일 (ex. 12)',
    PRIMARY KEY (id),
    KEY idx_div_isincd (isinCd),
    CONSTRAINT fk_div_company FOREIGN KEY (isinCd) REFERENCES company (isinCd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='금융위원회 주식배당정보 — API 응답 전량 저장';

-- -------------------------------------------------------------
-- 6. 주식시세 (stock_price) — 금융위원회 공공데이터
-- 적재 순서 3단계
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_price (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    basDt      CHAR(8)        NOT NULL COMMENT '기준일자 (YYYYMMDD)',
    srtnCd     CHAR(6)        NOT NULL COMMENT '단축종목코드',
    isinCd     VARCHAR(12)        NULL COMMENT 'ISIN 코드',
    itmsNm     VARCHAR(100)       NULL COMMENT '종목명',
    mrktCtg    VARCHAR(10)        NULL COMMENT '시장구분 (KOSPI/KOSDAQ/KONEX)',
    clpr       BIGINT             NULL COMMENT '종가',
    vs         BIGINT             NULL COMMENT '전일 대비 등락',
    fltRt      DECIMAL(8, 2)      NULL COMMENT '등락률',
    mkp        BIGINT             NULL COMMENT '시가',
    hipr       BIGINT             NULL COMMENT '고가',
    lopr       BIGINT             NULL COMMENT '저가',
    trqu       BIGINT             NULL COMMENT '거래량',
    trPrc      BIGINT             NULL COMMENT '거래대금',
    lstgStCnt  BIGINT             NULL COMMENT '상장주식수',
    mrktTotAmt BIGINT             NULL COMMENT '시가총액',
    PRIMARY KEY (id),
    KEY idx_sp_bas_srtn (basDt, srtnCd),
    KEY idx_sp_srtncd (srtnCd),
    CONSTRAINT fk_sp_company FOREIGN KEY (srtnCd) REFERENCES company (stock_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='금융위원회 주식 일별 시세';

-- -------------------------------------------------------------
-- 7. 지표 계산 (stock_indicator)
-- 적재 순서 5단계
-- calc_year(현재연도) 기준 1행 UPSERT
-- 예: 2026년 계산 → bsns_year=2025 재무 + 2026 최근 영업일 주가
-- dividend_yield = dividend_info.stckGenrDvdnAmt(직전 배당기준일) ÷ stock_price.clpr(현재) × 100
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_indicator (
    stock_code        CHAR(6)        NOT NULL COMMENT '종목코드',
    calc_year         CHAR(4)        NOT NULL COMMENT '계산 기준연도 (현재연도, e.g. 2026)',
    bsns_year         CHAR(4)        NOT NULL COMMENT '사용된 재무제표 사업연도 (calc_year - 1)',
    reprt_code        CHAR(5)        NOT NULL COMMENT '사용된 보고서 코드',
    base_dt           CHAR(8)        NOT NULL COMMENT '사용된 주가 기준일자 (stock_price.basDt)',
    eps               DECIMAL(15, 2)     NULL COMMENT '주당순이익 = 당기순이익 / 상장주식수',
    bps               DECIMAL(15, 2)     NULL COMMENT '주당순자산 = 자본총계 / 상장주식수',
    per               DECIMAL(10, 2)     NULL COMMENT '주가수익비율 = 현재가 / EPS',
    pbr               DECIMAL(10, 2)     NULL COMMENT '주가순자산비율 = 현재가 / BPS',
    roe               DECIMAL(10, 2)     NULL COMMENT '자기자본이익률 = 당기순이익 / 자본총계 × 100',
    operating_margin  DECIMAL(10, 2)     NULL COMMENT '영업이익률 = 영업이익 / 매출액 × 100',
    debt_ratio        DECIMAL(10, 2)     NULL COMMENT '부채비율 = 부채총계 / 자본총계 × 100',
    dividend_yield    DECIMAL(10, 2)     NULL COMMENT '배당수익률 = 전년도 주당 현금배당금 / 현재 주가 × 100',
    calculated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계산일시',
    PRIMARY KEY (stock_code, calc_year),
    CONSTRAINT fk_si_company FOREIGN KEY (stock_code) REFERENCES company (stock_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='재무(전년도) + 주가(현재) 기반 지표 계산 결과 — 연도별 1행';

-- -------------------------------------------------------------
-- 8. 유저 즐겨찾기 (user_favorite)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_favorite (
    user_id    BIGINT   NOT NULL COMMENT '유저 ID',
    stock_code CHAR(6)  NOT NULL COMMENT '즐겨찾기 종목코드',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    PRIMARY KEY (user_id, stock_code),
    CONSTRAINT fk_uf_user    FOREIGN KEY (user_id)    REFERENCES `user` (user_id),
    CONSTRAINT fk_uf_company FOREIGN KEY (stock_code) REFERENCES company (stock_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='유저별 즐겨찾기 종목';

-- -------------------------------------------------------------
-- 9. 시가총액 상위 100 스냅샷 (top100)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS top100 (
    base_dt    CHAR(8)          NOT NULL COMMENT '기준일자 (YYYYMMDD)',
    `rank`     TINYINT UNSIGNED NOT NULL COMMENT '순위 (1~100)',
    corp_code  CHAR(8)          NOT NULL COMMENT 'DART 고유번호',
    stock_code CHAR(6)          NOT NULL COMMENT '종목코드',
    bsns_year  CHAR(4)          NOT NULL COMMENT '지표 계산에 사용된 사업연도',
    PRIMARY KEY (base_dt, `rank`),
    KEY idx_top100_stock (stock_code),
    CONSTRAINT fk_top100_company FOREIGN KEY (corp_code) REFERENCES company (corp_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='시가총액 기준 상위 100 종목 일별 스냅샷';

-- -------------------------------------------------------------
-- 10. 커뮤니티 게시글 (investment_journal)
-- 종목별 투자 의견을 공유하는 커뮤니티 게시판 — 댓글(comment) 연결
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS investment_journal (
    journal_id BIGINT       NOT NULL AUTO_INCREMENT COMMENT '게시글 고유 ID',
    user_id    BIGINT       NOT NULL COMMENT '작성자 유저 ID',
    title      VARCHAR(255) NOT NULL COMMENT '게시글 제목',
    content    TEXT             NULL COMMENT '게시글 본문',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일시',
    PRIMARY KEY (journal_id),
    KEY idx_ij_user  (user_id),
    KEY idx_ij_stock (stock_code),
    CONSTRAINT fk_ij_user    FOREIGN KEY (user_id)    REFERENCES `user` (user_id),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='종목별 투자 의견 커뮤니티 게시글';

-- -------------------------------------------------------------
-- 11. 댓글 (comment)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `comment` (
    comment_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '댓글 고유 ID',
    journal_id BIGINT   NOT NULL COMMENT '대상 일지 ID',
    user_id    BIGINT   NOT NULL COMMENT '작성자 유저 ID',
    content    TEXT     NOT NULL COMMENT '댓글 내용',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    PRIMARY KEY (comment_id),
    KEY idx_comment_journal (journal_id),
    KEY idx_comment_user    (user_id),
    CONSTRAINT fk_comment_journal FOREIGN KEY (journal_id) REFERENCES investment_journal (journal_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user    FOREIGN KEY (user_id)    REFERENCES `user` (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='투자일지 댓글';
