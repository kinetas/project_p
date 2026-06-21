# 시스템 아키텍처 설계 (Front · Server · DB)

> Draw.io 다이어그램 작성용 명세서  
> 참조: [PRD.md](../PRD.md) · [schemaVER2.md](../db/schemaVER2.md) · [doc/db/api](../db/api/)  
> 다이어그램 파일: [시스템아키텍처.drawio](../시스템아키텍처.drawio)

---

## 1. 계층 개요

```
┌─────────────────────────────────────────────────────────────────┐
│  FRONTEND (HTML / CSS / JavaScript)                              │
│  페이지 UI · 클라이언트 렌더링 · 폼 검증 · 차트 그리기            │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP REST (JSON)
┌───────────────────────────▼─────────────────────────────────────┐
│  SERVER (Spring Boot)                                            │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────────┐ │
│  │ REST API     │  │ Query        │  │ Data Pipeline (Batch)  │ │
│  │ Controller   │→ │ Service      │  │ Scheduler → Collector  │ │
│  └──────────────┘  └──────┬───────┘  └───────────┬────────────┘ │
│                           │                      │              │
│                    ┌──────▼──────────────────────▼──────┐       │
│                    │         Repository (JPA)           │       │
│                    └──────────────────┬─────────────────┘       │
└───────────────────────────────────────┼─────────────────────────┘
                                        │ JDBC
┌───────────────────────────────────────▼─────────────────────────┐
│  DATABASE (MySQL · schemaVER2)                                   │
└─────────────────────────────────────────────────────────────────┘

        ┌──────────────── External Open API ────────────────┐
        │ DART · KRX 주식시세 · 금융위 지수 · 수출입은행 환율 │
        └───────────────────────────────────────────────────┘
                              ↑ (Pipeline only)
```

---

## 2. Frontend 모듈 (UI 전용 vs API 연동)

| 모듈 ID | 파일 | 역할 | API 연동 |
|---|---|---|---|
| **F-COMMON** | `js/common.js`, `css/common.css` | 헤더·네비·검색바·가격 포맷·페이지네이션 | 검색 Enter → `list.html?q=` (라우팅만) |
| **F-HOME** | `index.html`, `js/home.js` | 시장 지표, 주요 종목, TOP10 랭킹 탭 | ✅ |
| **F-LIST** | `list.html`, `js/list.js` | 종목 테이블/카드, 필터·정렬, TOP10 탭 | ✅ |
| **F-DETAIL** | `detail.html`, `js/detail.js` | 요약카드, 기업정보, 5년 차트·재무 테이블 | ✅ |
| **F-AUTH** | `login.html`, `register.html`, `js/auth.js` | 회원가입·로그인 폼, 클라이언트 유효성 검사 | ✅ |
| **F-CHART** | `js/common.js` (Chart 유틸) | Chart.js로 5년 추이 렌더링 | ❌ (API 응답을 클라이언트에서 시각화) |
| **F-WATCH** | (미구현) 마이페이지·관심종목 | 즐겨찾기 목록·등록/삭제 UI | 🔜 |
| **F-COMM** | (미구현) 투자일지·댓글 | 게시글 CRUD, 댓글 UI | 🔜 |

---

## 3. Front ↔ Server REST 매칭표

> **원칙**: 프론트 화면 기능 1개 = REST 엔드포인트 1개(이상). 프론트 전용 기능은 `UI Only`로 표기.

| # | Front 기능 | Front 모듈 | HTTP | REST Endpoint | Server Controller | Server Service |
|---|---|---|---|---|---|---|
| 1 | 회원가입 폼 제출 | F-AUTH | `POST` | `/api/users/register` | UserController | UserService |
| 2 | 로그인 폼 제출 | F-AUTH | `POST` | `/api/users/login` | UserController | UserService |
| 3 | 로그아웃 버튼 | F-AUTH | `POST` | `/api/users/logout` | UserController | UserService |
| 4 | 홈 — KOSPI/KOSDAQ/환율 배너 | F-HOME | `GET` | `/api/market/indices` | MarketIndexController | MarketIndexService* |
| 5 | 홈 — 시가총액 TOP 종목 카드 | F-HOME | `GET` | `/api/stocks/featured` | StockController | StockService |
| 6 | 홈/목록 — TOP10 탭 (가치주/저PER/고ROE) | F-HOME, F-LIST | `GET` | `/api/stocks/top10?type=value\|lowPer\|highRoe` | StockController | StockService |
| 7 | 목록 — 검색·필터·정렬·페이징 | F-LIST | `GET` | `/api/stocks?page&size&q&perMin&...` | StockController | StockService |
| 8 | 상세 — 요약카드·기업정보·가치지표 | F-DETAIL | `GET` | `/api/stocks/{code}` | StockController | StockService |
| 9 | 상세 — 5년 재무제표 (차트·테이블 원천) | F-DETAIL | `GET` | `/api/stocks/{code}/financials` | FinancialController | StockService |
| 10 | 헤더 검색 → 목록 이동 | F-COMMON | — | UI Only (URL query) | — | — |
| 11 | 5년 추이 차트 그리기 | F-CHART | — | UI Only (9번 응답 가공) | — | — |
| 12 | 테이블/카드 뷰 전환 | F-LIST | — | UI Only (localStorage) | — | — |
| 13 | 즐겨찾기 목록 | F-WATCH | `GET` | `/api/favorites` 🔜 | FavoriteController 🔜 | FavoriteService 🔜 |
| 14 | 즐겨찾기 등록 | F-WATCH | `POST` | `/api/favorites/{stockCode}` 🔜 | FavoriteController 🔜 | FavoriteService 🔜 |
| 15 | 즐겨찾기 삭제 | F-WATCH | `DELETE` | `/api/favorites/{stockCode}` 🔜 | FavoriteController 🔜 | FavoriteService 🔜 |
| 16 | 투자일지 목록 | F-COMM | `GET` | `/api/journals` 🔜 | JournalController 🔜 | JournalService 🔜 |
| 17 | 투자일지 작성/수정/삭제 | F-COMM | `POST/PUT/DELETE` | `/api/journals[/{id}]` 🔜 | JournalController 🔜 | JournalService 🔜 |
| 18 | 댓글 작성/삭제 | F-COMM | `POST/DELETE` | `/api/journals/{id}/comments[/{cid}]` 🔜 | CommentController 🔜 | CommentService 🔜 |

\* `MarketIndexService`는 현재 Controller에 mock 데이터. Pipeline 수집 후 DB 조회로 전환 예정.

---

## 4. Server 모듈 구조

### 4-1. REST API 계층 (Controller)

| Controller | Base Path | 담당 기능 |
|---|---|---|
| UserController | `/api/users` | 회원가입·로그인·로그아웃 |
| StockController | `/api/stocks` | 목록·상세·TOP10·featured |
| FinancialController | `/api/stocks` | 5년 재무제표 |
| MarketIndexController | `/api/market` | 시장 지수·환율 조회 |
| FavoriteController 🔜 | `/api/favorites` | 즐겨찾기 CRUD |
| JournalController 🔜 | `/api/journals` | 투자일지 CRUD |
| CommentController 🔜 | `/api/journals/{id}/comments` | 댓글 CRUD |

### 4-2. 비즈니스 계층 (Service — 조회/도메인)

| Service | 호출 주체 | 역할 | 내부 호출 |
|---|---|---|---|
| **UserService** | UserController | BCrypt 암호화, 이메일 PK 중복 검사, 로그인 검증 | UserRepository |
| **StockService** | StockController, FinancialController | 종목 목록 조합·필터·TOP10·상세·재무 5년 조회 | CompanyRepository, StockPriceRepository, StockIndicatorRepository, FinancialStatementRepository |
| **MarketIndexService** 🔜 | MarketIndexController | 최신 KOSPI/KOSDAQ/환율 조회 | MarketIndexRepository, ExchangeRepository |
| **FavoriteService** 🔜 | FavoriteController | 즐겨찾기 등록/삭제/목록 | UserFavoriteRepository, CompanyRepository |
| **JournalService** 🔜 | JournalController | 게시글 CRUD, 작성자 검증 | InvestmentJournalRepository, UserRepository |
| **CommentService** 🔜 | CommentController | 댓글 CRUD | CommentRepository, InvestmentJournalRepository |

### 4-3. 데이터 파이프라인 계층 (Batch — REST 미노출)

| Service | 트리거 | 역할 | 외부 API | 저장 테이블 |
|---|---|---|---|---|
| **DataCollectionScheduler** | `@Scheduled` 매일 07:00 | 전체 수집 파이프라인 오케스트레이션 | — | — |
| **DartCollectorService** | Scheduler Step 1,3 | corp_code·재무제표 수집 | DART list.json, fnlttMultiAcnt | company, financial_statement |
| **KrxCollectorService** | Scheduler Step 2 | 최신 주가 스냅샷 수집 | 금융위 주식시세 getStockPriceInfo | stock_price |
| **DividendCollectorService** | Scheduler Step 4 | 배당 정보 수집 | DART alotMatter | dividend_info |
| **IndicatorCalculationService** | Scheduler Step 5 | PER/PBR/ROE/EPS/BPS/배당수익률 계산 | — (DB 읽기) | stock_indicator |
| **MarketIndexCollectorService** 🔜 | Scheduler | KOSPI/KOSDAQ 지수 수집 | 금융위 지수시세 | market_index |
| **ExchangeCollectorService** 🔜 | Scheduler | USD/JPY 환율 수집 | 수출입은행 환율 | exchange |
| **ValueScoreService** 🔜 | Scheduler Step 6 | 가치주 종합점수·TOP100 산출 | — (DB 읽기) | top100 |

### 4-4. Server 내부 함수 호출 흐름

#### A. 사용자 요청 경로 (동기 REST)

```
[Front] ──REST──▶ [Controller] ──▶ [Query Service] ──▶ [Repository] ──▶ [MySQL]
```

예시 — 종목 상세:
```
F-DETAIL
  → GET /api/stocks/{code}
    → StockController.getStockDetail()
      → StockService.getStockDetail(code)
        → CompanyRepository.findByStockCode()
        → StockPriceRepository.findTopBySrtnCd()
        → StockIndicatorRepository.findByStockCode()
      ← StockDetailResponse
```

예시 — 5년 재무:
```
F-DETAIL + F-CHART
  → GET /api/stocks/{code}/financials
    → FinancialController.getFinancials()
      → StockService.getFinancials(code)
        → FinancialStatementRepository.findByStockCodeOrderByBsnsYearDesc()
      ← List<FinancialResponse>
  → F-CHART: 클라이언트에서 차트 렌더링
```

#### B. 배치 파이프라인 경로 (Scheduler — REST 없음)

```
DataCollectionScheduler.dailyCollection()
  │
  ├─(1)─▶ DartCollectorService.fetchCorpCodes()
  │         └─▶ DART API ──▶ company
  │
  ├─(2)─▶ KrxCollectorService.collectStockList()
  │         └─▶ KRX API ──▶ stock_price
  │
  ├─(3)─▶ DartCollectorService.collectFinancials() × N종목
  │         └─▶ DART API ──▶ financial_statement
  │
  ├─(4)─▶ DividendCollectorService.collectDividendInfo()
  │         └─▶ DART API ──▶ dividend_info
  │
  ├─(5)─▶ IndicatorCalculationService.calculateAndSave() × N종목
  │         ├─ read: company, stock_price, financial_statement, dividend_info
  │         └─ write: stock_indicator
  │
  └─(6)─▶ ValueScoreService.calculateTop100() 🔜
            ├─ read: stock_indicator (전 종목)
            └─ write: top100
```

#### C. Service 간 직접 호출 (현재·예정)

| 호출方 | 피호출方 | 목적 |
|---|---|---|
| DataCollectionScheduler | DartCollectorService | corp_code·재무 수집 위임 |
| DataCollectionScheduler | KrxCollectorService | 주가 수집 위임 |
| DataCollectionScheduler | DividendCollectorService | 배당 수집 위임 |
| DataCollectionScheduler | IndicatorCalculationService | 종목별 지표 계산 위임 |
| DataCollectionScheduler | ValueScoreService 🔜 | TOP100 점수 산출 위임 |
| IndicatorCalculationService | (Repository only) | 외부 API 직접 호출 없음 |
| StockService | (Repository only) | 수집 데이터 조합만 |

---

## 5. Server ↔ DB 매핑 (schemaVER2)

| Repository | 테이블 | 용도 |
|---|---|---|
| UserRepository | `user` | 회원 PK = email |
| UserFavoriteRepository 🔜 | `user_favorite` | (user_id, stock_code) 복합 PK |
| InvestmentJournalRepository 🔜 | `investment_journal` | 투자일지 |
| CommentRepository 🔜 | `comment` | 댓글 |
| CompanyRepository | `company` | 종목 허브 (stock_code PK) |
| StockPriceRepository | `stock_price` | 최신 시세 (srtn_cd PK) |
| StockIndicatorRepository | `stock_indicator` | 최신 지표 (stock_code PK) |
| FinancialStatementRepository | `financial_statement` | 연도별 재무 |
| DividendRepository | `dividend_info` | 배당 (corp_code PK) |
| Top100Repository 🔜 | `top100` | (base_dt, stock_code) 랭킹 |
| MarketIndexRepository 🔜 | `market_index` | 지수별 다행 |
| ExchangeRepository 🔜 | `exchange` | 통화별 환율 |

### DB 그룹 (Draw.io DB 레인 배치용)

| 그룹 | 테이블 |
|---|---|
| **사용자·커뮤니티** | user, user_favorite, investment_journal, comment |
| **종목·재무** | company, stock_price, stock_indicator, financial_statement, dividend_info, top100 |
| **시장·환율** | market_index, exchange |

---

## 6. External API ↔ Collector 매핑

| 외부 API | 문서 | Collector | DB 테이블 |
|---|---|---|---|
| DART list.json (공시검색) | [dart-company.md](../db/api/dart-company.md) | DartCollectorService | company |
| DART fnlttMultiAcnt (재무) | [dart-financial-statement.md](../db/api/dart-financial-statement.md) | DartCollectorService | financial_statement |
| DART alotMatter (배당) | [dart-dividend.md](../db/api/dart-dividend.md) | DividendCollectorService | dividend_info |
| 금융위 주식시세 | [krx-stock-price.md](../db/api/krx-stock-price.md) | KrxCollectorService | stock_price |
| 금융위 지수시세 | PRD Feature 4 | MarketIndexCollectorService 🔜 | market_index |
| 수출입은행 환율 | PRD Feature 4 | ExchangeCollectorService 🔜 | exchange |

---

## 7. Draw.io 다이어그램 작성 가이드

### 7-1. 레인( Swimlane ) 구성 — 3단

| 레인 | 색상 제안 | 배치 내용 |
|---|---|---|
| **Frontend** | `#E8F4F8` | F-COMMON, F-HOME, F-LIST, F-DETAIL, F-AUTH, F-CHART, F-WATCH🔜, F-COMM🔜 |
| **Server** | `#FFF2CC` | Controller / Query Service / Pipeline / Repository 4개 서브그룹 |
| **Database** | `#E8F5E9` | 사용자·커뮤니티 / 종목·재무 / 시장·환율 3그룹 |

**Server 서브그룹 (가로 배치 권장)**

```
┌─ REST Controller ─────────────────────────────────────────┐
│ UserController │ StockController │ FinancialController │  │
│ MarketIndexController │ Favorite🔜 │ Journal🔜 │ Comment🔜│
└───────────────────────────────────────────────────────────┘
┌─ Query Service ───────────────────────────────────────────┐
│ UserService │ StockService │ MarketIndexService🔜 │ ...  │
└───────────────────────────────────────────────────────────┘
┌─ Data Pipeline (Batch) ───────────────────────────────────┐
│ DataCollectionScheduler                                    │
│ DartCollector │ KrxCollector │ DividendCollector          │
│ IndicatorCalculation │ ValueScore🔜 │ MarketIndex🔜 ...  │
└───────────────────────────────────────────────────────────┘
┌─ Repository ──────────────────────────────────────────────┐
│ UserRepo │ CompanyRepo │ StockPriceRepo │ ... (12 repos)  │
└───────────────────────────────────────────────────────────┘
```

**External API** — Server 레인 오른쪽 또는 별도 레인, Pipeline에서만 화살표

### 7-2. 화살표(Edge) 규칙

| 연결 | 선 종류 | 라벨 예시 |
|---|---|---|
| Front → Controller | **파란 실선** | `GET /api/stocks` |
| Controller → Service | 회색 실선 | `getStockList()` |
| Service → Repository | 회색 실선 | `findAll()` |
| Repository → DB | 녹색 실선 | `SELECT` |
| Scheduler → Collector | 주황 실선 | `Step N` |
| Collector → External API | **주황 점선** | `DART fnlttMultiAcnt` |
| Front UI Only | 회색 점선 | `UI Only` |

### 7-3. 매칭 번호 표기

Front 박스와 Controller 박스에 **동일 번호(#1~#18)** 를 붙이면 REST 매칭이 한눈에 보입니다.  
(예: F-AUTH #1,2,3 ↔ UserController #1,2,3)

---

## 8. 구현 상태 범례

| 기호 | 의미 |
|---|---|
| ✅ | 구현 완료 (develope/ 기준) |
| 🔜 | PRD/schemaVER2 정의됨, 미구현 |
| UI Only | 프론트 전용, REST 불필요 |

---

*작성 기준: PRD · schemaVER2 · doc/db/api · develope/backend·front 코드*
