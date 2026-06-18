# Project Report

Last Update: 2026-06-18 (TASK-044, TASK-045 완료 반영)

---

# Project Status

## Current Progress
- 시스템 초기화 완료
- PRD 작성 완료
- Coding Rule 작성 완료
- 프로젝트 시작 (projectStart 실행)
- SEG-01 infra 완료 (schema.sql, application.properties 생성)
- SEG-02 backend_auth 완료 (UserEntity, UserRepository, DTO 3종, UserService, UserController 생성)
- SEG-03 backend_data 완료 (StockEntity, FinancialEntity, Repository 2종, Service 3종, Controller 2종, Scheduler, DTO 5종 생성)
- SEG-04 frontend 완료 (index.html, detail.html, login.html, register.html, CSS 3종, JS 3종 생성)
- SEG-05 backend_dto 완료 (StockListResponse/StockDetailResponse 필드 보완, 저PBR TOP10 기능 추가)
- SEG-06 frontend_api 완료 (data.js API 함수 추가, home.js/list.js/detail.js 백엔드 API 연동)
- SEG-07 backend_market 완료 (MarketIndexResponse DTO, MarketIndexController 생성, data.js fetchMarketIndices() 추가, home.js 시장 지표 API 연동)
- SEG-08 frontend_deploy 완료 (list.js 검색/시장 필터 보완, develope/front/ → resources/static/ 16개 파일 배포)
- TASK-032 완료: StockEntity에 isinCd 필드 추가, KrxCollectorService isinCd 파싱/저장 연동
- TASK-033 완료: 배당정보 Entity/Repository/CollectorService 신규 생성 (DividendId, DividendEntity, DividendRepository, DividendCollectorService)
- TASK-034 완료: DataCollectionScheduler에 배당정보 수집 Step 2 추가, 전체 파이프라인 7단계로 확장
- TASK-035~040 완료: 단일 테이블 구조 → 분리 구조 전환 (Entity/Repository 리팩토링, Service/DTO/Scheduler 전면 재작성, schema.sql 교체)
- TASK-044 완료: StockDetailResponse DTO 불필요 필드 7개 제거, GET /api/stocks/featured (시가총액 TOP10) 엔드포인트 추가, 프론트 data.js/home.js 연동
- TASK-045 완료: PageResponse<T> 제네릭 DTO 신규 생성, 백엔드 in-memory 페이징 구현, 프론트 종목 리스트 페이징 UI 완성 (list.js/list.html/list.css)
- 프로젝트 전체 개발 완료 + 배당 데이터 파이프라인 추가 + 신규 스키마 기반 리팩토링 완료

## Overall Completion
- Planning: 100%
- Core System: 100%
- Extension System: 100%
- UI/UX: 100%

---

# Current Working Tasks

| Task | Assigned AI | Status |
|---|---|---|
| SEG-01 infra (TASK-001,002) | Manager AI-01 | 완료 |
| SEG-02 backend_auth (TASK-003~007) | Manager AI-02 | 완료 |
| SEG-03 backend_data (TASK-008~017) | Manager AI-03 | 완료 |
| SEG-04 frontend (TASK-018~020) | Manager AI-01 | 완료 |
| SEG-05 backend_dto (TASK-021) | Manager AI | 완료 |
| SEG-06 frontend_api (TASK-022~025) | Manager AI | 완료 |
| SEG-07 backend_market (TASK-026~027) | Manager AI | 완료 |
| SEG-08 frontend_deploy (TASK-028~029) | Manager AI | 완료 |
| isinCd 필드 추가 (TASK-032) | backend-developer | 완료 |
| 배당정보 파이프라인 구축 (TASK-033) | backend-developer | 완료 |
| Scheduler 배당 수집 연동 (TASK-034) | backend-developer | 완료 |
| Entity/Repository 리팩토링 (TASK-035) | backend-developer | 완료 |
| KrxCollectorService 신규 엔티티 교체 (TASK-036) | backend-developer | 완료 |
| DartCollectorService 신규 엔티티 교체 (TASK-037) | backend-developer | 완료 |
| IndicatorCalculationService 재작성 (TASK-038) | backend-developer | 완료 |
| StockService/DTO 리팩토링 (TASK-039) | backend-developer | 완료 |
| schema.sql 교체 및 Scheduler 파이프라인 재정비 (TASK-040) | backend-developer | 완료 |
| DTO 정리 + 시가총액 TOP10 엔드포인트 (TASK-044) | backend-developer | 완료 |
| 종목 리스트 페이징 (TASK-045) | fullstack-developer | 완료 |

전체 세그먼트 완료 + 배당 데이터 파이프라인 추가 + 신규 스키마 기반 전면 리팩토링 완료

---

# Patch Notes

## 2026-06-18 (TASK-035 ~ TASK-040: Entity/Repository 리팩토링)

### 개요
가치투자 종목 발굴 서비스의 단일 테이블 구조(StockEntity, FinancialEntity)를 신규 스키마 기반 분리 구조(CompanyEntity, StockPriceEntity, FinancialStatementEntity, StockIndicatorEntity)로 전환.

---

- TASK-035 (Entity/Repository 리팩토링): 구 엔티티 4개 삭제, 신규 Entity/Repository 10개 생성 — 완료
  - 생성 파일:
    - entity/CompanyEntity.java (company 테이블, PK: corp_code)
    - entity/StockPriceId.java (stock_price 복합 PK: basDt, srtnCd)
    - entity/StockPriceEntity.java (stock_price 테이블, EmbeddedId)
    - entity/FinancialStatementEntity.java (financial_statement 테이블, uq_fs unique constraint)
    - entity/StockIndicatorId.java (stock_indicator 복합 PK: stock_code, calc_year)
    - entity/StockIndicatorEntity.java (stock_indicator 테이블, EmbeddedId)
    - repository/CompanyRepository.java (findByStockCode)
    - repository/StockPriceRepository.java (findTopByIdSrtnCdOrderByIdBasDtDesc)
    - repository/FinancialStatementRepository.java (JPQL 포함 4개 쿼리 메서드)
    - repository/StockIndicatorRepository.java (findByIdStockCode, findTopByIdStockCodeOrderByIdCalcYearDesc)
  - 삭제 파일: entity/StockEntity.java, entity/FinancialEntity.java, repository/StockRepository.java, repository/FinancialRepository.java
  - 토큰 소모량: 약 10,500 tokens (입력 6,000 + 출력 4,500)

- TASK-036 (KrxCollectorService 신규 엔티티 교체): StockEntity/StockRepository → CompanyEntity+StockPriceEntity 기반으로 완전 교체 — 완료
  - 수정 파일: service/KrxCollectorService.java
  - 주요 변경: saveStock(item, market) → saveStock(item, market, basDt), company UPSERT(toBuilder), stock_price saveAndFlush UPSERT, convertCorpCls() 메서드 추가, parseBigDecimal() 추가
  - 토큰 소모량: 약 4,700 tokens (입력 3,500 + 출력 1,200)

- TASK-037 (DartCollectorService 신규 엔티티 교체): StockRepository/FinancialRepository → CompanyRepository/FinancialStatementRepository 기반으로 전면 재작성 — 완료
  - 수정 파일: service/DartCollectorService.java
  - 주요 변경: fetchCorpCodes() → CompanyEntity UPSERT(corp_code/stock_code/corp_name/corp_cls 4필드), fetchCompanyInfo() 제거(company 테이블 컬럼 없음), collectFinancials() → FinancialStatementEntity 전 행 saveAll(deleteAll+saveAll UPSERT), parseAmount()/parseOrd()/parseAndUpsertCorpCodes() 헬퍼 추가
  - 토큰 소모량: 약 5,700 tokens (입력 4,500 + 출력 1,200)

- TASK-038 (IndicatorCalculationService 재작성): StockRepository/FinancialRepository → 4개 신규 Repository 기반으로 전면 재작성, DividendRepository 메서드 추가 — 완료
  - 수정 파일:
    - service/IndicatorCalculationService.java (CompanyRepository, StockPriceRepository, FinancialStatementRepository, StockIndicatorRepository, DividendRepository 5개 의존성, CFS 우선/OFS 폴백, BigDecimal EPS/BPS/PER/PBR/ROE/영업이익률/부채비율/배당수익률 계산, StockIndicatorEntity UPSERT)
    - repository/DividendRepository.java (findTopByIdIsinCdOrderByIdBasDtDesc 추가)
  - 토큰 소모량: 약 10,500 tokens (입력 8,000 + 출력 2,500)

- TASK-039 (StockService/DTO 리팩토링): StockRepository/FinancialRepository 의존성 제거, 신규 4개 Repository 기반으로 Service/DTO 전면 수정 — 완료
  - 수정 파일:
    - service/StockService.java (getStockList: CompanyRepository.findAll() 기반, getStockDetail: 3개 레포지토리 조회, getTop10: type별 정렬 후 상위 10개, getFinancials: 최근 5년 account_nm 집계)
    - dto/stock/StockListResponse.java (from(StockEntity) 제거 → from(CompanyEntity, StockPriceEntity, StockIndicatorEntity) 추가)
    - dto/stock/StockDetailResponse.java (from(StockEntity) 제거 → from(CompanyEntity, StockPriceEntity, StockIndicatorEntity) 추가)
    - dto/stock/FinancialResponse.java (from(FinancialEntity) 제거, 필드 축소: year/revenue/operatingProfit/netIncome)
  - 토큰 소모량: 약 7,500 tokens (입력 4,000 + 출력 3,500)

- TASK-042 (Backend FinancialResponse DTO 확장): FinancialResponse.java에 totalAssets, totalLiabilities, totalEquity 필드 추가, StockService.java getFinancials() 매핑 3줄 추가 — 완료
- TASK-043 (Frontend data.js 필드명 매핑 수정): data.js normalizeStock() 필드명 6개 수정, normalizeDetail() 함수 전체 교체 (shares 단위 변환, sector/listedDate/ceo 하드코딩 "-") — 완료

- TASK-040 (schema.sql 교체 및 Scheduler 파이프라인 재정비): schema.sql 신규 생성, DataCollectionScheduler 파이프라인 순서 재정비 및 CompanyRepository 교체 — 완료
  - 생성 파일: develope/backend/src/main/resources/schema.sql (doc/schema.sql 내용 복사, 11개 테이블 정의)
  - 수정 파일: config/DataCollectionScheduler.java (StockRepository → CompanyRepository 교체, 파이프라인 순서 변경: Step1 DART→Step2 KRX→Step3 재무→Step4 배당→Step5 지표)
  - 토큰 소모량: 약 8,000 tokens (입력 5,000 + 출력 3,000)

### TASK-035~040 총 예상 토큰 소모량
- TASK-035: 약 10,500 tokens
- TASK-036: 약 4,700 tokens
- TASK-037: 약 5,700 tokens
- TASK-038: 약 10,500 tokens
- TASK-039: 약 7,500 tokens
- TASK-040: 약 8,000 tokens
- **합계: 약 46,900 tokens**

---

- TASK-032 (isinCd 필드 추가): StockEntity에 `isinCd` 필드 추가, KrxCollectorService `saveStock()` 파싱/빌더 체인 연동 — 완료
  - 수정 파일:
    - entity/StockEntity.java (`isinCd` 필드 추가, `@Column(name="isin_cd")`)
    - service/KrxCollectorService.java (`saveStock()` 내 `isinCd` 파싱 및 신규/기존 빌더 체인 반영)
  - 토큰 소모량: 약 4,300 tokens (입력 3,500 + 출력 800)
- TASK-033 (배당정보 파이프라인 구축): 배당정보 수집 Entity/Repository/Service 신규 생성 — 완료
  - 생성 파일:
    - entity/DividendId.java (`@Embeddable` 복합키, isinCd + basDt, `Serializable`)
    - entity/DividendEntity.java (`@Entity`, `@Table(name="dividend_info")`, 필드 22개, BigDecimal 8개)
    - repository/DividendRepository.java (`JpaRepository<DividendEntity, DividendId>`)
    - service/DividendCollectorService.java (금융위원회 API 페이지네이션 수집, upsert, graceful fallback)
  - 토큰 소모량: 약 6,500 tokens (입력 4,500 + 출력 2,000)
- TASK-034 (Scheduler 배당 수집 연동): DataCollectionScheduler에 배당정보 수집 Step 2 삽입, 파이프라인 7단계로 확장 — 완료
  - 수정 파일:
    - config/DataCollectionScheduler.java (DividendCollectorService 주입, dailyCollection() Step 2 신규 추가, 기존 Step 2~6 → Step 3~7 재조정)
  - 최종 파이프라인: Step1(KRX) → Step2(배당정보) → Step3(전체종목조회) → Step4(DART매핑) → Step5(기업개황) → Step6(재무수집) → Step7(지표계산)
  - 토큰 소모량: 약 3,700 tokens (입력 2,500 + 출력 1,200)

## 2026-06-18 (TASK-044, TASK-045: DTO 정리 + 시가총액 TOP10 + 페이징)

- TASK-044 (DTO 정리 + 시가총액 TOP10 엔드포인트): StockDetailResponse 불필요 필드 7개 제거, GET /api/stocks/featured 신규 추가 — 완료
  - 수정 파일:
    - dto/stock/StockDetailResponse.java (mkp/hipr/lopr/trqu/trPrc/isinCd/corpCls 필드 제거, from() 동기화)
    - service/StockService.java (getFeaturedStocks(): mrktTotAmt 내림차순 정렬 후 상위 10개 반환)
    - controller/StockController.java (GET /api/stocks/featured → ResponseEntity<List<StockListResponse>>)
    - front/js/data.js (fetchFeaturedStocks() 함수 추가)
    - front/js/home.js ("주요 종목" 섹션 fetchStocks({size:8}) → fetchFeaturedStocks() 교체)

- TASK-045 (종목 리스트 페이징): PageResponse<T> 제네릭 DTO 신규 생성, 백엔드/프론트 페이징 전면 구현 — 완료
  - 생성 파일:
    - dto/stock/PageResponse.java (data/totalCount/page/size/totalPages 필드, 제네릭 DTO)
  - 수정 파일:
    - service/StockService.java (getStockList(): in-memory subList 방식 페이징, PageResponse<StockListResponse> 반환)
    - controller/StockController.java (getStockList() 반환 타입 PageResponse<StockListResponse>로 변경)
    - front/js/data.js (fetchStocks() 반환 구조 변경: 배열 → { stocks, totalCount, totalPages, page })
    - front/js/list.js (currentPage/PAGE_SIZE 변수 추가, loadStocks() 페이징 파라미터 전달, renderPagination() 신규 추가)
    - front/list.html (id="pagination" div 추가)
    - front/css/list.css (.pagination/.page-btn/.page-btn.active 스타일 추가)

---

## 2026-06-14
- SEG-01 infra: DB스키마 DDL, application.properties — 완료
- SEG-02 backend_auth: UserEntity/Repository/DTO/Service/Controller — 완료
  - 생성 파일: UserEntity.java, UserRepository.java, RegisterRequest.java, LoginRequest.java, UserResponse.java, UserService.java, PasswordEncoderConfig.java, UserController.java
- SEG-03 backend_data: Stock/Financial Entity, 데이터 수집 파이프라인, API — 완료
  - 생성 파일:
    - entity/StockEntity.java (19개 필드, @Table stocks)
    - entity/FinancialEntity.java (16개 필드, @Table financials, UniqueConstraint)
    - repository/StockRepository.java (JPA + 검색/TOP10/@Query 가치주)
    - repository/FinancialRepository.java (JPA + 연도별/최근5년 조회)
    - service/KrxCollectorService.java (KOSPI/KOSDAQ 현재가/시총 수집, upsert)
    - config/RestTemplateConfig.java (@Bean RestTemplate)
    - service/DartCollectorService.java (corpCode.xml 파싱, 재무 수집 upsert)
    - service/IndicatorCalculationService.java (EPS/BPS/PER/PBR/ROE/영업이익률/부채비율)
    - service/StockService.java (검색/필터/페이징, 상세, TOP10, 재무 5년)
    - controller/StockController.java (GET /api/stocks, /{code}, /top10)
    - controller/FinancialController.java (GET /api/stocks/{code}/financials)
    - config/DataCollectionScheduler.java (@Scheduled 매일 오전 7시 전체 파이프라인)
    - dto/stock/StockListResponse.java
    - dto/stock/StockDetailResponse.java
    - dto/stock/FinancialResponse.java
    - dto/stock/StockSearchRequest.java
    - dto/stock/Top10Response.java
  - Boss AI: DemoApplication.java에 @EnableScheduling 어노테이션 추가
- SEG-04 frontend: 목록/상세/인증 HTML 페이지 — 완료
  - 생성 파일:
    - static/index.html (종목 목록 — 검색/필터/TOP10 탭/페이지네이션)
    - static/css/stock-list.css
    - static/js/stock-list.js (fetch API, 숫자 포맷, XSS 방어, 정렬)
    - static/detail.html (종목 상세 — 요약카드/기업정보/가치지표/재무 탭)
    - static/css/stock-detail.css
    - static/js/stock-detail.js (Chart.js 4.4.2, 차트 3종, 재무 테이블 3탭)
    - static/login.html
    - static/register.html
    - static/css/auth.css
    - static/js/auth.js (body[data-page] 분기, JWT sessionStorage, 실시간 유효성 검사)
- SEG-05 backend_dto: 프론트 MOCK_STOCKS 필드 불일치 해소 — 완료
  - 수정 파일:
    - dto/stock/StockListResponse.java (changeRate/changeAmount/dividendYield 필드 추가, from() 팩토리 반영)
    - dto/stock/StockDetailResponse.java (sharesOutstanding/dividendYield 필드 추가, from() 팩토리 반영)
    - repository/StockRepository.java (findTop10ByPbrIsNotNullOrderByPbrAsc() 저PBR TOP10 쿼리 추가)
    - service/StockService.java (switch "lowPbr" 케이스 추가)
- SEG-06 frontend_api: 프론트엔드 JS 백엔드 API 연동 — 완료
  - 수정 파일:
    - front/js/data.js (API_BASE 상수, normalizeStock/normalizeDetail 정규화 함수, fetchStocks/fetchTop10/fetchStockDetail/fetchFinancials/fetchStockFull 함수 추가)
    - front/js/home.js (async 전환, 주요 종목 fetchStocks({size:8}) 연동, 랭킹 4종 Promise.all 병렬 fetch, MOCK_STOCKS fallback 유지)
    - front/js/list.js (async 전환, MOCK_STOCKS → fetchStocks 교체, loadStocks() 함수 추가, applyFilters/render 수정, MOCK_STOCKS fallback 유지)
    - front/js/detail.js (async 전환, getStockByCode → fetchStockFull 교체, yearLabels/renderTable years 파라미터 동적화, 로딩 UI 및 오류 처리 추가)
- SEG-07 backend_market: 시장 지표 API 엔드포인트 및 프론트 연동 — 완료
  - 생성 파일:
    - dto/market/MarketIndexResponse.java (name, value, changeRate, changeAmount 필드, @Getter @Builder)
    - controller/MarketIndexController.java (GET /api/market/indices, KOSPI/KOSDAQ/USD/JPY 정적 데이터 반환, KRX 실시간 연동 TODO 명시)
  - 수정 파일:
    - front/js/data.js (fetchMarketIndices() 함수 추가 — GET /api/market/indices 호출)
    - front/js/home.js (시장 지표 섹션 MARKET_INDICES 하드코딩 → fetchMarketIndices() API 연동, fallback 유지)
- SEG-08 frontend_deploy: list.js 검색 보완 및 프론트엔드 파일 Spring Boot static 배포 — 완료
  - 수정 파일:
    - front/js/list.js (loadStocks() market 파라미터 지원, size 200→1000, applyFilter/resetFilter async 전환 및 loadStocks() 재호출, MOCK 폴백에 market 필터 적용)
  - 배포 파일 (develope/front/ → resources/static/, 총 16개):
    - HTML 5개: index.html(덮어쓰기), list.html(신규), detail.html(덮어쓰기), login.html(덮어쓰기), register.html(덮어쓰기)
    - CSS 5개: common.css(신규), home.css(신규), list.css(신규), detail.css(신규), auth.css(덮어쓰기)
    - JS 6개: common.js(신규), data.js(신규), home.js(신규), list.js(신규), detail.js(신규), auth.js(덮어쓰기)
  - 보존 파일: static/css/stock-list.css, stock-detail.css, static/js/stock-list.js, stock-detail.js

---

# Current Issues

| Priority | Issue | Status |
|---|---|---|
| 높음 | build.gradle에 JPA/MySQL/Security 의존성 누락 (주석 처리됨) | 미해결 |

---

# Next Targets

- build.gradle JPA/MySQL/Security 의존성 주석 해제 후 서버 실행 및 테스트

---

# AI Activity Summary

| AI | Activity |
|---|---|
| Boss AI | 시스템 초기화, projectStart 실행, DAG 생성, DemoApplication.java @EnableScheduling 추가 |
| Manager AI-01 | SEG-01 infra 완료 (TASK-001, 002) / SEG-04 frontend 완료 (TASK-018~020) — index.html, detail.html, login.html, register.html, CSS 3종, JS 3종 생성 |
| Manager AI-02 | SEG-02 backend_auth 완료 (TASK-003~007) — UserEntity, Repository, DTO 3종, Service, Controller 생성 |
| Manager AI-03 | SEG-03 backend_data 완료 (TASK-008~017) — StockEntity, FinancialEntity, Repository 2종, Service 3종, Controller 2종, Scheduler, DTO 5종 생성 |
| Manager AI | SEG-05 backend_dto 완료 (TASK-021) — DTO 필드 보완 (changeRate/changeAmount/dividendYield/sharesOutstanding), 저PBR TOP10 기능 추가 |
| Manager AI | SEG-06 frontend_api 완료 (TASK-022~025) — data.js API fetch/정규화 함수 추가, home.js/list.js/detail.js 백엔드 연동 및 MOCK_STOCKS fallback 적용 |
| Manager AI | SEG-07 backend_market 완료 (TASK-026~027) — MarketIndexResponse DTO, MarketIndexController(GET /api/market/indices) 신규 생성, data.js fetchMarketIndices() 추가, home.js 시장 지표 API 연동 및 fallback 처리 |
| Manager AI | SEG-08 frontend_deploy 완료 (TASK-028~029) — list.js market 파라미터/async 개선, develope/front/ → resources/static/ 16개 파일 배포 (HTML 5, CSS 5, JS 6) |
| backend-developer | TASK-032 완료 — StockEntity isinCd 필드 추가, KrxCollectorService 파싱/빌더 연동 (~4,300 tokens) |
| backend-developer | TASK-033 완료 — DividendId, DividendEntity, DividendRepository, DividendCollectorService 신규 생성 (~6,500 tokens) |
| backend-developer | TASK-034 완료 — DataCollectionScheduler 배당 수집 Step 2 추가, 파이프라인 7단계 확장 (~3,700 tokens) |
| backend-developer | TASK-035 완료 — 신규 Entity 6개/Repository 4개 생성, 구 Entity/Repository 4개 삭제 (~10,500 tokens) |
| backend-developer | TASK-036 완료 — KrxCollectorService: StockEntity → CompanyEntity+StockPriceEntity 교체 (~4,700 tokens) |
| backend-developer | TASK-037 완료 — DartCollectorService: 신규 스키마 기반 전면 재작성, fetchCompanyInfo() 제거 (~5,700 tokens) |
| backend-developer | TASK-038 완료 — IndicatorCalculationService 재작성, DividendRepository 메서드 추가 (~10,500 tokens) |
| backend-developer | TASK-039 완료 — StockService 재작성, StockListResponse/StockDetailResponse/FinancialResponse DTO 수정 (~7,500 tokens) |
| backend-developer | TASK-040 완료 — schema.sql 신규 생성(11개 테이블), DataCollectionScheduler 파이프라인 재정비 (~8,000 tokens) |
| backend-developer | TASK-044 완료 — StockDetailResponse 필드 7개 제거, GET /api/stocks/featured 추가, data.js fetchFeaturedStocks()/home.js 연동 |
| fullstack-developer | TASK-045 완료 — PageResponse<T> DTO 신규 생성, StockService/StockController 페이징 적용, list.js/list.html/list.css 페이징 UI 구현 |

---

# Reference Fragments

- TASK-018_frontend-developer.md (종목 목록 페이지)
- TASK-019_frontend-developer.md (종목 상세 페이지)
- TASK-020_frontend-developer.md (회원 인증 페이지)
- TASK-021_backend_developer.md (backend_dto 필드 보완 및 저PBR TOP10 추가)
- TASK-022_frontend_developer.md (data.js API 상수·fetch·정규화 함수 추가)
- TASK-023_frontend_developer.md (home.js 백엔드 API 연동)
- TASK-024_frontend_developer.md (list.js 백엔드 API 연동)
- TASK-025_frontend_developer.md (detail.js 백엔드 API 연동)
- TASK-026_backend_developer.md (GET /api/market/indices 엔드포인트 신규 추가)
- TASK-027_frontend_developer.md (data.js fetchMarketIndices() 추가, home.js 시장 지표 API 연동)
- TASK-028_frontend_developer.md (list.js loadStocks market 파라미터, applyFilter/resetFilter async 전환 및 API 재호출)
- TASK-029_devops.md (develope/front/ → resources/static/ 배포, 총 16개 파일)
- TASK-032_backend-developer.md (StockEntity isinCd 필드 추가, KrxCollectorService isinCd 파싱/저장 연동)
- TASK-033_backend-developer.md (DividendId/DividendEntity/DividendRepository/DividendCollectorService 신규 생성)
- TASK-034_backend-developer.md (DataCollectionScheduler 배당 수집 Step 2 추가, 파이프라인 7단계 확장)
- TASK-035_backend-developer.md (CompanyEntity/StockPriceEntity/FinancialStatementEntity/StockIndicatorEntity + Repository 4종 생성, StockEntity/FinancialEntity 삭제)
- TASK-036_backend-developer.md (KrxCollectorService: CompanyEntity+StockPriceEntity 기반 교체, convertCorpCls()/parseBigDecimal() 추가)
- TASK-037_backend-developer.md (DartCollectorService: CompanyEntity UPSERT, FinancialStatementEntity 전 행 저장, fetchCompanyInfo() 제거)
- TASK-038_backend-developer.md (IndicatorCalculationService 재작성, DividendRepository findTopByIdIsinCdOrderByIdBasDtDesc 추가)
- TASK-039_backend-developer.md (StockService 재작성, StockListResponse/StockDetailResponse/FinancialResponse DTO 수정)
- TASK-040_backend-developer.md (schema.sql 11개 테이블 신규 생성, DataCollectionScheduler DART→KRX→재무→배당→지표 파이프라인 재정비)
- TASK-042_financial-dto.md (FinancialResponse DTO totalAssets/totalLiabilities/totalEquity 필드 추가, StockService getFinancials() 매핑 연동)
- TASK-043_frontend-mapping.md (data.js normalizeStock() 필드명 6개 수정, normalizeDetail() 함수 전체 교체)
- TASK-044_dto-featured.md (StockDetailResponse 불필요 필드 7개 제거, GET /api/stocks/featured 신규, data.js fetchFeaturedStocks() 추가, home.js 연동)
- TASK-045_pagination.md (PageResponse<T> DTO 신규 생성, StockService/StockController 페이징 적용, list.js/list.html/list.css 페이징 UI)

---
## TASK-042 — Backend FinancialResponse DTO 확장 (완료)
- FinancialResponse.java에 totalAssets, totalLiabilities, totalEquity 필드 추가
- StockService.java getFinancials() 매핑 3줄 추가
완료일: 2026-06-18

## TASK-043 — Frontend data.js 필드명 매핑 수정 (완료)
- data.js normalizeStock() 필드명 6개 수정
- data.js normalizeDetail() 함수 전체 교체 (shares 단위 변환, sector/listedDate/ceo 하드코딩 "-")
완료일: 2026-06-18

## TASK-044 — DTO 정리 + 시가총액 TOP10 엔드포인트 (완료: 2026-06-18)
- StockDetailResponse.java에서 불필요 필드 7개 제거 (mkp/hipr/lopr/trqu/trPrc/isinCd/corpCls), from() 메서드 동기화
- StockService.getFeaturedStocks(): 전체 종목 mrktTotAmt 기준 내림차순 정렬 후 상위 10개 반환
- StockController.getFeaturedStocks(): GET /api/stocks/featured 엔드포인트 신규 추가 (ResponseEntity<List<StockListResponse>>)
- data.js: fetchFeaturedStocks() 함수 추가 (GET /api/stocks/featured 호출 후 normalizeStock 매핑)
- home.js: "주요 종목" 섹션에서 fetchStocks({ size: 8 }) → fetchFeaturedStocks()로 교체

## TASK-045 — 종목 리스트 페이징 (완료: 2026-06-18)
- PageResponse<T> 제네릭 DTO 신규 생성 (data/totalCount/page/size/totalPages 필드)
- StockService.getStockList(): 전체 필터링 후 in-memory subList 방식 페이징, PageResponse<StockListResponse> 반환
- StockController.getStockList(): 반환 타입 PageResponse<StockListResponse>로 변경
- data.js: fetchStocks() 반환 구조 변경 (배열 → { stocks, totalCount, totalPages, page })
- list.js: currentPage(0-indexed)/PAGE_SIZE=20 상태 변수 추가, loadStocks() 페이징 파라미터 전달, renderPagination() 신규 추가 (이전/다음/페이지 번호 버튼)
- list.html: id="pagination" div 추가
- list.css: .pagination/.page-btn/.page-btn.active 스타일 추가
