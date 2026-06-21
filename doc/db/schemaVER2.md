# Schema Design Document (VER2)

> `doc/데이터베이스ver1.drawio` ERD를 기반으로 작성된 스키마 설계 근거 문서입니다.  
> 이전 버전: [schema.md](./schema.md) (유지)  
> Draw.io 다이어그램의 **상세 테이블 정의**를 기준으로 반영했으며, 다이어그램에 포함된 Mermaid ER 스냅샷과 일부 차이가 있습니다.

---

## VER2 변경 요약 (schema.md 대비)

| 구분 | VER1 (schema.md) | VER2 (draw.io) |
|---|---|---|
| **market_index** | 일별 1행, KOSPI/KOSDAQ 컬럼 고정 | **지수별 다행** 구조 (`idxNm`으로 KOSPI/KOSDAQ 등 구분) |
| **exchange** | `id` + `base_dt` UK, USD/KRW 전용 | **`curUnit` PK**, 통화 단위별 다행 (`dealBasR`, `changeRate` 등) |
| **stock_price** | `id` PK, 일별 시세 이력 | **`srtn_cd` PK,FK** — 종목당 최신 1건 (1:1) |
| **dividend_info** | `id` PK + `corp_code` UK | **`corp_code` PK,FK** — 서rogate key 제거 |
| **stock_indicator** | `id` PK, `(stock_code, calc_year)` UK | **`stock_code` PK,FK** — 종목당 최신 지표 1건, `eps`/`bps` 추가 |
| **top100** | `id` PK, `base_dt` + `stock_code` | **`(base_dt, stock_code)` 복합 PK**, `id`/타임스탬프 제거 |
| **user** | `id` BIGINT PK, `email` UK | **`id(API.email)` VARCHAR PK** — 이메일을 PK로 사용 |
| **user_favorite** | `id` PK, `(user_id, stock_code)` UK | **`(user_id, stock_code)` 복합 PK**, `user_id` → `user.id(email)` |
| **investment_journal / comment** | `user_id` BIGINT FK | **`user_id` VARCHAR FK** → `user.id(email)` |

---

## MarketIndex (`market_index`)

### 설계 근거
금융위원회 **지수시세정보 API** 응답 필드에 맞춘 구조입니다. VER1처럼 KOSPI/KOSDAQ을 한 행의 컬럼으로 두지 않고, **지수명(`idxNm`)별로 행을 분리**하여 KOSPI·KOSDAQ·기타 지수를 동일 스키마로 적재합니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | 전역 고유 식별자 |
| basDd | DATE | NOT NULL | 기준일자 (API: `basDd`) |
| idxNm | VARCHAR(50) | NOT NULL | 지수명 (예: KOSPI, KOSDAQ) |
| flucRt | DECIMAL(10,4) | NULL | 등락률 (API: `flucRt`) |
| opnprcIdx | DECIMAL(15,4) | NULL | 시가 지수 |
| clsprcIdx | DECIMAL(15,4) | NULL | 종가 지수 |
| cmpprevddIdx | DECIMAL(15,4) | NULL | 전일 대비 지수 |
| mktcap | BIGINT | NULL | 상장시가총액 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| (없음) | — | 독립 집계 테이블 |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| uq_market_index_date_name | (basDd, idxNm) | UNIQUE | 동일 일자·동일 지수 중복 적재 방지 |
| idx_market_index_basDd | basDd | BTREE | 최신 지수 일괄 조회 |

---

## Exchange (`exchange`)

### 설계 근거
한국수출입은행 **환율 API** 응답 구조를 반영합니다. VER1의 USD/KRW 전용 컬럼 대신 **`curUnit`(통화 코드)을 PK**로 두고, USD·JPY 등 통화별로 행을 저장합니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| curUnit | VARCHAR(10) | PK | 통화 단위 (예: USD, JPY) |
| baseDate | DATE | NOT NULL | 기준일자 (API: `baseDate`) |
| country | VARCHAR(50) | NULL | 국가명 |
| dealBasR | DECIMAL(15,4) | NULL | 매매 기준율 |
| changeRate | DECIMAL(10,4) | NULL | 등락률 |
| changeAmount | DECIMAL(15,4) | NULL | 등락폭 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| (없음) | — | 독립 집계 테이블 |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| idx_exchange_baseDate | baseDate | BTREE | 일별 환율 조회 |

> **구현 참고:** `curUnit` 단독 PK이면 통화당 1건만 저장됩니다. 일별 이력이 필요하면 `(curUnit, baseDate)` 복합 PK 또는 UNIQUE로 조정하세요.

---

## Company (`company`)

### 설계 근거
DART list.json API로 수집한 전체 상장 법인 목록. `stock_code`가 KRX 주식시세 API의 `srtnCd`와 동일하여 주가·지표 데이터 매칭의 핵심 키입니다. VER1과 동일한 허브 테이블 역할을 유지합니다.

다이어그램 주석(향후 확장 후보): 시장구분, 상장일 — 현재 테이블 컬럼에는 미포함.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| stock_code | VARCHAR(6) | PK | 종목코드 6자리. KRX `srtnCd`와 동일 |
| corp_code | VARCHAR(8) | UNIQUE, NOT NULL | DART 고유번호. 재무·배당 API 호출 기준 |
| corp_name | VARCHAR(100) | NOT NULL | 종목명. 검색·목록·상세 표시 |
| corp_cls | CHAR(1) | NULL | 법인구분(Y/K/N/E). 시장 필터 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 적재일시 |
| updated_at | DATETIME | NOT NULL, DEFAULT NOW() | 수정일시 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| financial_statement | 1:N | 연도·보고서별 재무 데이터 |
| stock_price | 1:1 | `stock_code = srtn_cd` |
| dividend_info | 1:1 | `corp_code` 기준 |
| stock_indicator | 1:1 | `stock_code` 기준, 최신 지표 1건 |
| top100 | 1:N | 날짜별 랭킹 스냅샷 |
| user_favorite | 1:N | 다수 유저 즐겨찾기 |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| uq_company_corp_code | corp_code | UNIQUE | DART API 중복 방지 |
| idx_company_corp_name | corp_name | BTREE | 종목명 LIKE 검색 |

---

## FinancialStatement (`financial_statement`)

### 설계 근거
DART fnlttMultiAcnt API 응답을 **핵심 집계 컬럼** 위주로 저장합니다. VER1의 세부 재무 항목(유동자산, 자본금 등)은 제거하고, 손익·재무상태 핵심 수치만 유지합니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | 전역 고유 식별자 |
| bsns_year | CHAR(4) | NOT NULL | 사업연도 |
| stock_code | VARCHAR(6) | NOT NULL, FK→company | 종목코드 |
| reprt_code | VARCHAR(5) | NOT NULL | 보고서 코드 (11011: 사업보고서) |
| fs_div | VARCHAR(5) | NOT NULL | OFS/CFS 구분 |
| revenue | BIGINT | NULL | 매출액 |
| operating_income | BIGINT | NULL | 영업이익 |
| net_income | BIGINT | NULL | 당기순이익 |
| total_assets | BIGINT | NULL | 자산총계 |
| total_liabilities | BIGINT | NULL | 부채총계 |
| total_equity | BIGINT | NULL | 자본총계 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| company | N:1 | 종목별 다연도 재무 데이터 |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| idx_fs_lookup | (stock_code, bsns_year, reprt_code) | BTREE | 5년 재무 추이 조회 |

---

## StockPrice (`stock_price`)

### 설계 근거
KRX 주식시세정보 API 기반 **종목별 최신 시세** 1건을 저장합니다. VER1의 일별 이력 구조에서 **1:1 스냅샷**으로 단순화했습니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| srtn_cd | VARCHAR(6) | PK, FK→company | 단축종목코드 = `stock_code` |
| bas_dt | DATE | NOT NULL | 기준일자 |
| clpr | BIGINT | NULL | 종가. PER/PBR 계산 입력 |
| lstg_st_cnt | BIGINT | NULL | 상장주식수. EPS 계산 |
| mrkt_tot_amt | BIGINT | NULL | 시가총액 |
| mkp | BIGINT | NULL | 시가 |
| flt_rt | DECIMAL(10,4) | NULL | 등락률 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 적재일시 |
| updated_at | DATETIME | NOT NULL, DEFAULT NOW() | 수정일시 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| company | 1:1 | `company.stock_code = stock_price.srtn_cd` |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| (PK) | srtn_cd | PRIMARY | 종목별 1건 |

---

## DividendInfo (`dividend_info`)

### 설계 근거
DART 배당 정보. VER1의 surrogate `id`를 제거하고 **`corp_code`를 PK**로 사용합니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| corp_code | VARCHAR(8) | PK, FK→company | DART 고유번호 |
| dividend_kind | VARCHAR(6) | NULL | 보통주/우선주 |
| dividend_amount | BIGINT | NULL | 주당배당금 |
| stlm_dt | DATETIME | NULL | 배당 기준일 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| company | 1:1 | `company.corp_code` 기준 |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| (PK) | corp_code | PRIMARY | 종목별 1건 |

---

## StockIndicator (`stock_indicator`)

### 설계 근거
재무제표 + 주가 기반 **최신 파생 지표** 1건을 종목별로 저장합니다. VER1의 연도별 이력(`calc_year`) 대신 **현재 시점 스냅샷** 구조입니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| stock_code | VARCHAR(6) | PK, FK→company | 종목코드 |
| per | DECIMAL(10,2) | NULL | 주가수익비율 |
| pbr | DECIMAL(10,4) | NULL | 주가순자산비율 |
| roe | DECIMAL(10,4) | NULL | 자기자본이익률 |
| debt_ratio | DECIMAL(10,4) | NULL | 부채비율 |
| dividend_yield | DECIMAL(10,4) | NULL | 배당수익률 |
| eps | DECIMAL(15,2) | NULL | 주당순이익 |
| bps | DECIMAL(15,2) | NULL | 주당순자산 |
| calculated_at | DATETIME | NULL | 지표 계산 시점 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| company | 1:1 | 종목당 최신 지표 1건 |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| idx_indicator_per | per | BTREE | PER 정렬·필터 |
| idx_indicator_pbr | pbr | BTREE | PBR 정렬·필터 |
| idx_indicator_roe | roe | BTREE | ROE 정렬·필터 |
| idx_indicator_debt | debt_ratio | BTREE | 부채비율 필터 |

---

## Top100 (`top100`)

### 설계 근거
가치주 종합점수 기반 **날짜별 랭킹** 스냅샷. VER1의 surrogate `id`와 타임스탬프를 제거하고 **`(base_dt, stock_code)` 복합 PK**로 정의합니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| base_dt | DATE | NOT NULL, PK | 기준일자 |
| corp_code | VARCHAR(8) | NOT NULL, FK→company | DART 고유번호 |
| stock_code | VARCHAR(6) | NOT NULL, PK, FK→company | 종목코드 |
| score | INT | NULL | 가치주 종합점수 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| company | N:1 | 날짜별 다수 종목 랭킹 |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| idx_top100_base_dt_score | (base_dt, score) | BTREE | 일별 점수 순 조회 |

---

## User (`user`)

### 설계 근거
서비스 회원 테이블. VER2에서는 **이메일을 PK(`id`)** 로 사용합니다. 다이어그램 표기 `id(API.email)`은 로그인 API의 이메일 필드를 PK로 쓴다는 의미입니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| id | VARCHAR(255) | PK | 로그인 이메일 (API.email) |
| password | VARCHAR(255) | NOT NULL | BCrypt 해시 |
| nickname | VARCHAR(50) | NOT NULL | 표시명 |
| role | ENUM('USER','ADMIN') | NOT NULL, DEFAULT 'USER' | 권한 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 가입일시 |
| updated_at | DATETIME | NOT NULL, DEFAULT NOW() | 수정일시 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| user_favorite | 1:N | 즐겨찾기 |
| investment_journal | 1:N | 투자일지 |
| comment | 1:N | 댓글 |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| (PK) | id | PRIMARY | 이메일 로그인 |

---

## UserFavorite (`user_favorite`)

### 설계 근거
마이페이지 즐겨찾기. VER2에서는 surrogate `id`를 제거하고 **`(user_id, stock_code)` 복합 PK**입니다. `user_id`는 `user.id`(이메일)를 참조합니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| user_id | VARCHAR(255) | PK, FK→user | 즐겨찾기 등록 유저 (이메일) |
| stock_code | VARCHAR(6) | PK, FK→company | 즐겨찾기 종목 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 등록일시 |
| updated_at | DATETIME | NOT NULL, DEFAULT NOW() | 수정일시 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| user | N:1 | `user_id` → `user.id` |
| company | N:1 | `stock_code` → `company.stock_code` |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| (PK) | (user_id, stock_code) | PRIMARY | 중복 즐겨찾기 방지 |

---

## InvestmentJournal (`investment_journal`)

### 설계 근거
커뮤니티 투자일지 게시글. `user_id` FK 타입이 BIGINT → **VARCHAR(255)** 로 변경되어 `user.id`(이메일)를 참조합니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | 게시글 ID |
| user_id | VARCHAR(255) | NOT NULL, FK→user | 작성자 (이메일) |
| title | VARCHAR(200) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 본문 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 작성일시 |
| updated_at | DATETIME | NOT NULL, DEFAULT NOW() | 수정일시 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| user | N:1 | 작성자 |
| comment | 1:N | 댓글 |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| idx_journal_user_id | user_id | BTREE | 본인 게시글 조회 |

---

## Comment (`comment`)

### 설계 근거
투자일지 댓글. `user_id` FK가 **VARCHAR(255)** 로 변경되어 `user.id`(이메일)를 참조합니다.

### 컬럼 상세

| 컬럼명 | 타입 | 제약조건 | 설계 근거 |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | 댓글 ID |
| journal_id | BIGINT | NOT NULL, FK→investment_journal | 게시글 |
| user_id | VARCHAR(255) | NOT NULL, FK→user | 작성자 (이메일) |
| content | TEXT | NOT NULL | 댓글 내용 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 작성일시 |
| updated_at | DATETIME | NOT NULL, DEFAULT NOW() | 수정일시 |

### 관계

| 대상 테이블 | 관계 유형 | 근거 |
|---|---|---|
| investment_journal | N:1 | 소속 게시글 |
| user | N:1 | 작성자 |

### 인덱스

| 인덱스명 | 컬럼 | 타입 | 근거 |
|---|---|---|---|
| idx_comment_journal_id | journal_id | BTREE | 게시글별 댓글 목록 |

---

## 전체 ERD 요약 (VER2)

```
market_index  (독립, 지수별 다행)
exchange      (독립, 통화별 다행)

company ──< financial_statement
company ──  stock_price           (stock_code = srtn_cd, 1:1)
company ──  dividend_info         (corp_code, 1:1)
company ──  stock_indicator       (stock_code, 1:1)
company ──< top100                (base_dt + stock_code 복합 PK)
company ──< user_favorite

user(id=email) ──< user_favorite >── company
user(id=email) ──< investment_journal
user(id=email) ──< comment

investment_journal ──< comment
```

---

## API 필드명 ↔ DB 컬럼 매핑 (참고)

| API | draw.io 컬럼 | 권장 DB 컬럼 |
|---|---|---|
| 지수시세 `basDd` | basDd | bas_dd |
| 지수시세 `idxNm` | idxNm | idx_nm |
| 지수시세 `flucRt` | flucRt | fluc_rt |
| 환율 `curUnit` | curUnit | cur_unit |
| 환율 `dealBasR` | dealBasR | deal_bas_r |
| 주가 `srtnCd` | srtn_cd | srtn_cd |
| 주가 `clpr` | clpr | clpr |
| 로그인 `email` | id(API.email) | id (PK) |

> draw.io에는 camelCase(API 원본)로 표기되어 있습니다. 실제 DDL 작성 시 snake_case 통일을 권장합니다.

---

## 다이어그램 메모 (향후 검토)

Draw.io 캔버스에 아래 항목이 **주석**으로만 존재하며, VER2 테이블 컬럼에는 아직 반영되지 않았습니다.

- **company**: 시장구분, 상장일
- **financial_statement**: period_nm, period_dt, currency 등 세부 항목
- **exchange**: `curUnit` PK + `baseDate` 조합에 대한 일별 이력 PK 정의 재검토

---

*출처: [doc/데이터베이스ver1.drawio](../데이터베이스ver1.drawio)*
