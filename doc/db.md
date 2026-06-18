# Database Design Document

---

# 페이지 구성

| 페이지명 | URL 경로 | 설명 | 인증 필요 |
|---|---|---|---|
| 메인 | `/` | PER·PBR·ROE·부채비율 기준 종목 + 코스피, 환율| 아니오 |
| 종목 목록 | `/list` | PER·PBR·ROE·부채비율 기준 전체 종목 리스트 | 아니오 |
| 종목 랭킹 | `/rank` | 점수를 통한 종목 top 100 | 아니오 |
| 종목 상세 | `/stock/{stockCode}` | 요약카드 + 기업정보/가치지표 + 5년 재무추이 | 아니오 |
| 로그인 | `/login` | 이메일·비밀번호 인증 | 아니오 |
| 회원가입 | `/register` | 이메일·비밀번호·닉네임 입력 | 아니오 |
| 마이페이지 / 즐겨찾기 | `/mypage` | 즐겨찾기 종목 목록 조회 | 예 |
| 투자일지(커뮤니티) | `/community` | 투자일지 목록 | 아니오 |
| 투자일지 작성 | `/community/write` | 투자일지 등록 | 예 |
| 투자일지 상세 | `/community/{journalId}` | 투자일지 본문 + 댓글 | 아니오 |

---

# 필요 기능

## 기능 목록

- [ ] 회원가입 / 로그인 / 로그아웃
- [ ] 종목 목록 페이지 — PER·PBR·ROE·부채비율 기준 정렬·필터
- [ ] 종목 검색 (종목명 또는 종목코드)
- [ ] 가치주 TOP10 / 저PER TOP5 / 고ROE TOP5 탭 전환
- [ ] 종목 상세 페이지 — 요약카드 + 기업정보 + 가치지표
- [ ] 재무제표 5년 추이 차트 (매출·영업이익 / ROE / 부채비율)
- [ ] 재무제표 연도별 테이블 (손익계산서 / 재무상태표 / 투자지표 탭)
- [ ] DART + KRX 데이터 자동 수집 파이프라인
- [ ] 즐겨찾기 추가 / 삭제 / 목록 조회
- [ ] 투자일지(커뮤니티) 게시글 CRUD
- [ ] 댓글 작성 / 삭제
- [ ] 가치주 종합점수 계산 (백분위 환산 합산) — 가중치 추후 확정
- [ ] 배당수익률 표시 — API 확보 후 상세 페이지에 추가

## 기능별 데이터 요구사항

### 종목 목록
- 읽기: `company`, `stock_price`, `stock_indicator`

### 종목 검색
- 읽기: `company` (corp_name, stock_code LIKE 검색)

### 종목 상세
- 읽기: `company`, `stock_price`, `stock_indicator`, `financial_statement`, `dividend_info`

### 즐겨찾기
- 읽기: `user_favorite` JOIN `company`, `stock_indicator`
- 쓰기: `user_favorite` INSERT
- 삭제: `user_favorite` DELETE

### 투자일지
- 읽기: `investment_journal`, `comment`
- 쓰기: `investment_journal` INSERT, `comment` INSERT
- 삭제: `investment_journal` DELETE, `comment` DELETE

### 데이터 수집 파이프라인
- 쓰기: `company`, `financial_statement`, `stock_price`, `dividend_info`, `stock_indicator`, `market_index`, `top100` UPSERT

---

# 전체 엔티티 구성

## 엔티티 목록

| 엔티티명 | 테이블명 | 설명 |
|---|---|---|
| MarketIndex | `market_index` | 코스피·코스닥 지수 날짜별 저장|
| exchange | `exchange` | 환율 날짜별 저장 |
| Company | `company` | Dart list.json api 로 회사, 주식 코드 목록 |
| FinancialStatement | `financial_statement` | DART fnlttMultiAcnt API 응답 원문 저장 |
| StockPrice | `stock_price` | 금융위원회 공공데이터 주식시세 (일별) |
| DividendInfo | `dividend_info` | Dart alotMatter api 배당 정보 |
| StockIndicator | `stock_indicator` | financial_statement + stock_price 기반 파생 지표 |
| Top100 | `top100` | 시가총액 상위 100 스냅샷 |
| User | `user` | 서비스 회원 (일반/관리자) |
| UserFavorite | `user_favorite` | 유저별 즐겨찾기 종목 |
| InvestmentJournal | `investment_journal` | 커뮤니티 투자일지 게시글 |
| Comment | `comment` | 투자일지 댓글 |

## 엔티티 상세

### MarketIndex

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 고유 ID |
| base_dt | DATE | 기준일자 |
| kospi_close | DECIMAL(10,2) | 코스피 종가 지수 |
| kospi_vs | DECIMAL(10,2) | 코스피 전일 대비 등락 |
| kosdaq_close | DECIMAL(10,2) | 코스닥 종가 지수 |
| kosdaq_vs | DECIMAL(10,2) | 코스닥 전일 대비 등락 |
| usd_krw_rate | DECIMAL(10,2) | 달러-원화 환율 |
| usd_krw_vs | DECIMAL(10,2) | 환율 전일 대비 등락 |
| created_at | DATETIME | 적재일시 |
| updated_at | DATETIME | 수정일시 |

### Company

| 컬럼 | 타입 | 설명 |
|---|---|---|
| stock_code | VARCHAR(6) PK | 종목코드(6자리) — 주식시세 매칭 키 (srtnCd) |
| corp_code | VARCHAR(8) UNIQUE | DART 고유번호 — 재무제표 조회 키 |
| corp_name | VARCHAR(100) | 종목명(법인명) |
| corp_cls | CHAR(1) | 법인구분 (Y:유가, K:코스닥, N:코넥스, E:기타) |
| created_at | DATETIME | 적재일시 |
| updated_at | DATETIME | 수정일시 |

### FinancialStatement

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 고유 ID |
| stock_code | VARCHAR(6) | 종목코드 — company.stock_code 참조 |
| bsns_year | CHAR(4) | 사업연도 |
| reprt_code | VARCHAR(10) | 보고서 코드 (11011: 사업보고서 등) |
| account_nm | VARCHAR(100) | 계정명 (예: 자본총계) |
| fs_div | VARCHAR(5) | 개별/연결구분 (OFS/CFS) |
| fs_nm | VARCHAR(50) | 개별/연결명 |
| sj_div | VARCHAR(5) | 재무제표구분 (BS/IS) |
| sj_nm | VARCHAR(50) | 재무제표명 |
| thstrm_nm | VARCHAR(50) | 당기명 |
| thstrm_dt | VARCHAR(30) | 당기일자 |
| thstrm_amount | BIGINT | 당기금액 |
| thstrm_add_amount | BIGINT | 당기누적금액 |
| frmtrm_nm | VARCHAR(50) | 전기명 |
| frmtrm_dt | VARCHAR(30) | 전기일자 |
| frmtrm_amount | BIGINT | 전기금액 |
| frmtrm_add_amount | BIGINT | 전기누적금액 |
| bfefrmtrm_nm | VARCHAR(50) | 전전기명 (사업보고서만) |
| bfefrmtrm_dt | VARCHAR(30) | 전전기일자 (사업보고서만) |
| bfefrmtrm_amount | BIGINT | 전전기금액 (사업보고서만) |
| ord | INT | 계정과목 정렬순서 |
| currency | VARCHAR(10) | 통화단위 |
| created_at | DATETIME | 적재일시 |
| updated_at | DATETIME | 수정일시 |

### StockPrice

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 고유 ID |
| bas_dt | DATE | 기준일자 |
| srtn_cd | VARCHAR(6) | 단축종목코드 — company.stock_code 동일 |
| isin_cd | VARCHAR(12) | ISIN 코드 |
| itms_nm | VARCHAR(100) | 종목명 |
| mrkt_ctg | VARCHAR(10) | 시장구분 (KOSPI/KOSDAQ/KONEX) |
| clpr | BIGINT | 종가 |
| vs | BIGINT | 전일 대비 등락 |
| flt_rt | DECIMAL(10,2) | 등락률 |
| mkp | BIGINT | 시가 |
| hipr | BIGINT | 고가 |
| lopr | BIGINT | 저가 |
| trqu | BIGINT | 거래량 |
| tr_prc | BIGINT | 거래대금 |
| lstg_st_cnt | BIGINT | 상장주식수 |
| mrkt_tot_amt | BIGINT | 시가총액 |
| created_at | DATETIME | 적재일시 |
| updated_at | DATETIME | 수정일시 |

### DividendInfo

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 고유 ID |
| bas_dt | DATE | 기준일자 |
| isin_cd | VARCHAR(12) | ISIN 코드 — company.isin_cd 참조 |
| crno | VARCHAR(13) | 법인등록번호 |
| stck_issu_cmpy_nm | VARCHAR(100) | 주식발행회사명 |
| dvdn_bas_dt | DATE | 배당기준일자 |
| cash_dvdn_pay_dt | DATE | 현금배당지급일자 |
| stck_hndv_dt | DATE | 주식교부일자 |
| isin_cd_nm | VARCHAR(100) | ISIN 코드명 |
| stck_dvdn_rcd | VARCHAR(2) | 주식배당사유코드 |
| stck_dvdn_rcd_nm | VARCHAR(50) | 주식배당사유코드명 |
| trsnm_dpty_dcd | VARCHAR(2) | 명의개서대리인구분코드 |
| trsnm_dpty_dcd_nm | VARCHAR(50) | 명의개서대리인구분코드명 |
| scrs_itms_kcd | VARCHAR(4) | 유가증권종목종류코드 |
| scrs_itms_kcd_nm | VARCHAR(50) | 유가증권종목종류코드명 |
| stck_genr_dvdn_amt | BIGINT | 주식일반배당금액 (1주당 현금 배당금) |
| stck_grdn_dvdn_amt | BIGINT | 주식차등배당금액 |
| stck_genr_cash_dvdn_rt | DECIMAL(10,4) | 주식일반현금배당률 |
| stck_genr_dvdn_rt | DECIMAL(10,4) | 주식일반배당률 |
| cash_grdn_dvdn_rt | DECIMAL(10,4) | 현금차등배당률 |
| stck_grdn_dvdn_rt | DECIMAL(10,4) | 주식차등배당률 |
| stck_par_prc | BIGINT | 주식액면가 |
| stck_stac_md | VARCHAR(4) | 주식결산월일 |
| created_at | DATETIME | 적재일시 |
| updated_at | DATETIME | 수정일시 |

### StockIndicator

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 고유 ID |
| stock_code | VARCHAR(6) | 종목코드 — company.stock_code 참조 |
| calc_year | CHAR(4) | 계산 기준연도 (현재연도, e.g. 2026) |
| bsns_year | CHAR(4) | 사용된 재무제표 사업연도 (calc_year - 1) |
| reprt_code | VARCHAR(10) | 사용된 보고서 코드 |
| base_dt | DATE | 사용된 주가 기준일자 |
| eps | DECIMAL(15,2) | 주당순이익 = 당기순이익 / 상장주식수 |
| bps | DECIMAL(15,2) | 주당순자산 = 자본총계 / 상장주식수 |
| per | DECIMAL(10,2) | 주가수익비율 = 현재가 / EPS |
| pbr | DECIMAL(10,4) | 주가순자산비율 = 현재가 / BPS |
| roe | DECIMAL(10,4) | 자기자본이익률 = 당기순이익 / 자본총계 × 100 |
| operating_margin | DECIMAL(10,4) | 영업이익률 = 영업이익 / 매출액 × 100 |
| debt_ratio | DECIMAL(10,4) | 부채비율 = 부채총계 / 자본총계 × 100 |
| dividend_yield | DECIMAL(10,4) | 배당수익률 = 전년도 주당 현금배당금 / 현재 주가 × 100 |
| calculated_at | DATETIME | 계산일시 |
| created_at | DATETIME | 적재일시 |
| updated_at | DATETIME | 수정일시 |

### Top100

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 고유 ID |
| base_dt | DATE | 기준일자 |
| rank | INT | 순위 (1~100) |
| corp_code | VARCHAR(8) | DART 고유번호 — company.corp_code 참조 |
| stock_code | VARCHAR(6) | 종목코드 — company.stock_code 참조 |
| bsns_year | CHAR(4) | 지표 계산에 사용된 사업연도 |
| created_at | DATETIME | 적재일시 |
| updated_at | DATETIME | 수정일시 |

### User

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 사용자 고유 ID |
| email | VARCHAR(255) UNIQUE | 이메일 |
| password | VARCHAR(255) | 암호화된 비밀번호 (BCrypt) |
| nickname | VARCHAR(50) | 닉네임 |
| role | ENUM('USER', 'ADMIN') DEFAULT 'USER' | 권한 레벨 |
| created_at | DATETIME | 가입일시 |
| updated_at | DATETIME | 수정일시 |

### UserFavorite

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 고유 ID |
| user_id | BIGINT | 유저 ID — user.id 참조 |
| stock_code | VARCHAR(6) | 즐겨찾기 종목코드 — company.stock_code 참조 |
| created_at | DATETIME | 등록일시 |
| updated_at | DATETIME | 수정일시 |

### InvestmentJournal

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 게시글 고유 ID |
| user_id | BIGINT | 작성자 — user.id 참조 |
| title | VARCHAR(200) | 게시글 제목 |
| content | TEXT | 게시글 본문 |
| created_at | DATETIME | 작성일시 |
| updated_at | DATETIME | 최종 수정일시 |

### Comment

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 댓글 고유 ID |
| journal_id | BIGINT | 대상 일지 ID — investment_journal.id 참조 |
| user_id | BIGINT | 작성자 — user.id 참조 |
| content | TEXT | 댓글 내용 |
| created_at | DATETIME | 작성일시 |
| updated_at | DATETIME | 수정일시 |

## 관계도 (ERD 요약)

```
Company 1──* FinancialStatement
Company 1──* StockPrice         (stock_code = srtn_cd)
Company 1──* DividendInfo       
Company 1──* StockIndicator
Company 1──* Top100

User    1──* UserFavorite ──*── Company
User    1──* InvestmentJournal
User    1──* Comment

InvestmentJournal 1──* Comment
```

---

# 외부 API 사용 목록

| API 이름 | 용도 | 연동 엔티티 | 비고 |
|---|---|---|---|
| DART OpenAPI (fnlttMultiAcnt) | 재무제표 원문 수집 | `company`, `financial_statement` | corp_code 기반 호출 |
| 금융위원회 주식시세 API | 현재가·시가총액·상장주식수 수집 | `stock_price` | srtnCd = stock_code 매칭 |
| 금융위원회 GetStocDiviInfoService_V2 | 배당 정보 수집 | `dividend_info` | isinCd 매칭, 전체 페이지 수집 |
| 배당수익률 별도 API |  | `stock_indicator` | 후순위 |

---

# 인증 / 권한 설계

## 인증 방식

- [ ] JWT (토큰 기반)
- [ ] Session (서버 세션)
- [x] 미정 — 개발 착수 전 확정 필요

## 권한 레벨

| 역할 | 권한 범위 |
|---|---|
| USER | 종목 조회, 즐겨찾기 CRUD, 투자일지 CRUD, 댓글 CRUD (본인 것만) |
| ADMIN | USER 권한 전체 + 모든 게시글·댓글 삭제, 데이터 수집 파이프라인 수동 트리거 |

---

# 인덱스 전략

| 테이블 | 인덱스 컬럼 | 인덱스 타입 | 이유 |
|---|---|---|---|
| `company` | `stock_code` | UNIQUE | 외래키 참조, 검색 기준 키 |
| `company` | `corp_code` | UNIQUE | DART API 호출 기준 키 |
| `company` | `corp_name` | INDEX | 종목명 LIKE 검색 |
| `financial_statement` | `(stock_code, bsns_year, reprt_code)` | INDEX | 재무 데이터 조회 기본 조건 |
| `stock_price` | `(srtn_cd, bas_dt)` | INDEX | 종목별 최근 주가 조회 |
| `stock_indicator` | `(stock_code, calc_year)` | UNIQUE | UPSERT 기준, 목록 필터링 |
| `stock_indicator` | `per` | INDEX | PER 정렬·필터 |
| `stock_indicator` | `pbr` | INDEX | PBR 정렬·필터 |
| `stock_indicator` | `roe` | INDEX | ROE 정렬·필터 |
| `stock_indicator` | `debt_ratio` | INDEX | 부채비율 정렬·필터 |
| `dividend_info` | `(isin_cd, dvdn_bas_dt)` | INDEX | 배당 정보 매칭 |
| `user_favorite` | `(user_id, stock_code)` | UNIQUE | 즐겨찾기 중복 방지 |
| `investment_journal` | `user_id` | INDEX | 작성자별 조회 |
| `comment` | `journal_id` | INDEX | 일지별 댓글 목록 조회 |

---

# 데이터 흐름

## 주요 시나리오별 흐름

### 시나리오: 데이터 수집 파이프라인 (스케줄러)

```
1. DART 회사목록 API
   → company 테이블 UPSERT (corp_code, stock_code, corp_name, corp_cls)

2. company.corp_code 기준 DART 재무제표 API 호출
   → financial_statement 테이블 UPSERT

3. 공공데이터 주식시세 API 호출 (srtnCd = company.stock_code)
   → stock_price 테이블 UPSERT
   → stock_price.isin_cd → company.isin_cd 역참조 저장

4. 금융위원회 배당정보 API 전체 수집
   → isin_cd == company.isin_cd 매칭 → dividend_info UPSERT

5. financial_statement + stock_price 데이터 기반 지표 계산
   → stock_indicator UPSERT (calc_year 기준)
   계산 규칙: calc_year(현재연도) = bsns_year(전년도) 재무 + 최근 영업일 주가
```

### 시나리오: 종목 목록 조회

```
클라이언트 요청 (정렬·필터 파라미터)
   → StockIndicator JOIN Company JOIN StockPrice
   → 페이지네이션 적용
   → 응답: 종목명, 현재가, 시가총액, PER, PBR, ROE, 부채비율
```

### 시나리오: 종목 상세 조회

```
클라이언트 요청 (stockCode)
   → Company + StockPrice + StockIndicator + DividendInfo (최신 1건)
   → FinancialStatement (최근 5개 사업연도, 사업보고서 기준)
   → 응답: 요약카드 + 기업정보 + 가치지표 + 5년 추이 데이터
```

---

# 마이그레이션 계획

## 초기 시드 데이터

- [x] 관리자 계정 1건 (role = ADMIN, 비밀번호 BCrypt 해시)

## 스키마 변경 정책

- 마이그레이션 파일 기반 관리 권장 (Flyway 또는 Liquibase)
- 컬럼 추가 시 DEFAULT 또는 NULLABLE로 처리하여 무중단 적용

---

# 보안 고려사항

- [x] 비밀번호 BCrypt 해시 저장 (평문 저장 금지)
- [x] API Key (`dart.api.key`, `krx.api.key`)는 `application.properties`에 관리, Git에는 빈 양식만 커밋
- [x] SQL Injection 방지 — JPA PreparedStatement 사용
- [x] ADMIN 전용 엔드포인트에 권한 검사 적용
- [x] 외부 API 호출 실패 시 graceful fallback 처리
- [ ] Rate Limit 적용 — 외부 API 호출 횟수 제한 준수 (DART, 금융위원회 정책)
- [ ] XSS 방지 — 투자일지·댓글 본문 HTML Escape 처리

---

# 성능 고려사항

## 예상 데이터 규모

| 테이블 | 예상 레코드 수 | 증가율 |
|---|---|---|
| `company` | ~2,500건 | 연 수십 건 (상장/상폐) |
| `financial_statement` | ~2,500 × 연도 × 계정항목 수 | 연 1회 대량 적재 |
| `stock_price` | ~2,500 × 영업일 수 | 일 ~2,500건 누적 |
| `dividend_info` | ~2,500건 (연 1회 갱신) | 연 1회 |
| `stock_indicator` | ~2,500 × calc_year 수 | 연 ~2,500건 |
| `market_index` | 영업일 수 누적 | 일 1건 |
| `top100` | 기준일 × 100 | 갱신 주기에 따라 상이 |
| `user` | 초기 소규모 | 서비스 성장에 따라 증가 |

## 캐싱 전략

- [ ] Redis 캐시 대상: 종목 목록 전체 (자주 조회, 데이터 변경 빈도 낮음)
- [ ] 캐시 TTL: (추후 결정 — 수집 파이프라인 실행 주기에 맞춰 설정)

## 미결 사항 / 추후 검토

- **업종/섹터 테이블** (`sector`): KRX 업종 분류 기반 종목 필터·비교 기능 추가 여부 미결
- **가격 알림 테이블** (`price_alert`): 유저별 목표가 알림 기능 기획 후 결정
- **top100 기준 확정**: 시가총액 외 PER·거래량 병행 여부, 갱신 주기(일별/주별) 미결
- **투자일지 공개 범위**: `visibility` 컬럼 (전체공개/비공개) 추가 여부
- **댓글 계층 구조**: 대댓글 지원 시 `parent_comment_id` 컬럼 추가 필요
- **user 테이블 컬럼**: 소셜 로그인(OAuth) vs 자체 회원가입 방식 확정 후 추가 정의
- **코스닥 수집**: 현재 제외, 향후 `mrkt_ctg` 컬럼 기반으로 확장 예정
