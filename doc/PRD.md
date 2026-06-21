# Product Requirement Document

(이 파일을 작성한 후 /projectStart 를 입력하세요.)

---

# Project Overview

## Project Name
가치투자 종목 발굴 서비스 (Value Stock Discovery Service)

## Project Objective
코스피·코스닥 전체 종목의 재무 데이터를 자동 수집·계산하여, 가치투자자가 저평가 우량 종목을 빠르게 발굴할 수 있도록 돕는 웹 서비스를 제공한다.
**핵심 원칙: 페이지마다 딱 하나의 질문에 답한다.**

| 페이지 | 사용자 질문 | 제공 지표 |
|---|---|---|
| 메인/목록 | 이 종목 싸 보이나? | PER · PBR · ROE · 부채비율 |
| 상세 상단 | 지금 이 주식 어떤 상태인가? | 6개 핵심 지표 요약카드 |
| 상세 하단 | 이 기업이 지속적으로 좋은 기업인가? | 최근 5년 추이 (차트 + 테이블) |

---

# User Requirements

## What do you want to build?
- 코스피·코스닥 전체 종목의 PER, PBR, ROE, 부채비율 등 가치투자 핵심 지표를 한눈에 볼 수 있는 종목 목록 페이지
- 개별 종목의 현재 상태와 5년 재무 추이를 보여주는 상세 페이지
- DART(전자공시) + KRX 데이터를 자동 수집·계산하는 백엔드 파이프라인
- 시장 현황 (KOSPI/KOSDAQ 지수, 환율) 표시
- 커뮤니티 기능 (투자일지 게시글 + 댓글)

## Why are you building this?
가치투자자는 기업의 내재가치 대비 저평가 여부를 판단하기 위해 여러 사이트를 돌아다니며 지표를 수집해야 한다. 이 서비스는 필요한 지표를 한 곳에서 빠르게 확인할 수 있도록 집약한다.

## Target Users
- 가치투자 방식으로 국내 주식 종목을 발굴하는 개인 투자자
- PER, PBR, ROE 등 재무지표를 이해하고 활용하는 중급 이상 투자자

---

# Core Features

## Required Features

- [x] 회원가입 / 로그인 / 로그아웃
- [x] 종목 목록 페이지 — PER·PBR·ROE·부채비율 기준 정렬·필터
- [x] 종목 검색 기능
- [x] 가치주 TOP10 / 저PER TOP10 / 고ROE TOP10 탭 전환
- [x] 종목 상세 페이지 — 요약카드 + 기업정보 + 가치지표
- [x] 재무제표 5년 추이 차트 (매출·영업이익 / ROE / 부채비율)
- [x] 재무제표 연도별 테이블 (손익계산서 / 재무상태표 / 투자지표 탭)
- [x] DART + KRX 데이터 자동 수집 파이프라인
- [x] 시장 현황 표시 (KOSPI/KOSDAQ 지수, 환율)
- [ ] 가치주 종합점수 계산 (백분위 환산 합산) — 가중치 추후 확정
- [ ] 배당수익률 표시 — DART 배당 데이터 기반 상세 페이지에 추가
- [ ] 커뮤니티 (투자일지 게시글 + 댓글) — 미구현
- [ ] 즐겨찾기 (마이페이지) — 미구현
- [ ] 주가 차트 연동 — 후순위

---

# Detailed Feature Requirements

## Feature 0: 회원 인증

### Purpose
서비스 이용을 위한 회원가입, 로그인, 로그아웃 기능을 제공한다.

### Expected Behavior
- 회원가입: 이메일 + 비밀번호 입력, 중복 이메일 검증
- 로그인: 이메일 + 비밀번호 인증 후 세션 또는 토큰 발급
- 로그아웃: 세션/토큰 무효화

### Input
- 이메일, 비밀번호 (회원가입 시 비밀번호 확인 포함)

### Output
- 로그인 성공 시 인증 상태 유지 (세션 또는 JWT)

### User 테이블 정의 (schemaVER2 기준)

| 컬럼명 (DB) | Java 필드명 | 타입 | 설명 |
|---|---|---|---|
| id | id | VARCHAR(255) PK | 로그인 이메일 (이메일을 PK로 직접 사용) |
| password | password | VARCHAR(255) NOT NULL | BCrypt 해시 |
| nickname | nickname | VARCHAR(50) NOT NULL | 표시명 |
| role | role | ENUM('USER','ADMIN') DEFAULT 'USER' | 권한 |
| created_at | createdAt | DATETIME NOT NULL | 가입일시 |
| updated_at | updatedAt | DATETIME NOT NULL | 수정일시 |

> **VER2 변경**: 기존 `id BIGINT AUTO_INCREMENT + email UNIQUE` 구조에서 **이메일을 PK로 직접 사용**하는 구조로 변경. user_favorite, investment_journal, comment 테이블의 `user_id`는 모두 `VARCHAR(255)`로 통일.

### Dependencies
- User Entity, UserRepository, UserService, UserController
- 비밀번호 암호화: BCrypt

---

## Feature 1: 종목 목록 페이지

### Purpose
가치투자자가 전체 종목을 지표 기준으로 빠르게 스크리닝한다.

### Expected Behavior
- 상단: 검색창 + 필터바
- 중단: 가치주 TOP10 / 저PER TOP10 / 고ROE TOP10 탭 (탭 전환)
- 하단: 전체 종목 리스트 (정렬·필터 가능)

### Input
- 검색어 (종목명 또는 종목코드)
- 필터 조건 (PER 범위, PBR 범위, ROE 하한, 부채비율 상한, 시장구분)

### Output
전체 종목 테이블:

| 컬럼 | 분류 |
|---|---|
| 종목명 | 기본 |
| 현재가 | 기본 |
| 시가총액 | 기본 |
| PER | 저평가 |
| PBR | 저평가 |
| ROE | 수익성 |
| 부채비율 | 안전성 |

### Dependencies
- KRX 주식시세 API (현재가, 시가총액)
- DART 재무 데이터 + 자체 계산 지표

---

## Feature 2: 종목 상세 페이지

### Purpose
특정 종목의 현재 투자 가치와 5년 재무 건전성을 종합적으로 판단한다.

### Expected Behavior

**상단 — 요약카드**
- 종목명, 종목코드
- 현재가, 시가총액, PER, PBR, ROE, 배당수익률

**중단 — 기업정보 + 가치지표**

기업정보:
- 업종, 시장구분(KOSPI/KOSDAQ), 상장일, 대표자

가치지표:
- EPS (주당순이익), BPS (주당순자산), ROE, 부채비율, 영업이익률

**하단 — 재무제표 5년 추이**

차트 3개:
- 매출액 + 영업이익 (묶음) — 성장성·수익성
- ROE 추이 — 수익성 지속성
- 부채비율 추이 — 안전성 변화

연도별 테이블 (탭 3개):
- 탭1 손익계산서: 매출액, 영업이익, 영업이익률, 순이익
- 탭2 재무상태표: 자산, 부채, 자본, 부채비율
- 탭3 투자지표: EPS, BPS, PER, PBR, ROE

### Input
- 종목코드

### Output
- 위 상단/중단/하단 UI 구성 전체

### Dependencies
- KRX API, DART API, 자체 계산 파이프라인

---

## Feature 3: 데이터 수집 파이프라인

### Purpose
DART + KRX에서 원천 데이터를 주기적으로 수집하고, 파생 지표를 계산하여 DB에 저장한다.

### Expected Behavior
```
코스피 + 코스닥 전체 종목
        ↓
    종목코드 확보 (KRX) → company 테이블 (stock_code PK)
        ↓
  DART corp_code 매핑
        ↓
    재무제표 수집 (DART) → financial_statement
        ↓
    배당 정보 수집 (DART) → dividend_info (corp_code PK)
        ↓
    최신 주가 수집 (KRX) → stock_price (srtn_cd PK, 1:1 스냅샷)
        ↓
  PER / PBR / ROE / EPS / BPS 등 계산 → stock_indicator (stock_code PK, 최신 1건)
        ↓
    가치주 종합점수 계산 → top100 ((base_dt, stock_code) 복합 PK)
        ↓
    시장 지수 수집 (지수별 다행) → market_index
        ↓
    환율 수집 (통화별) → exchange (curUnit PK)
        ↓
      필터링 서빙
```

### Input
- KRX 주식시세 데이터
- DART 재무제표 원문
- DART 배당 정보

### Output
DB에 저장된 종목별 지표 (현재가, EPS, BPS, PER, PBR, ROE, 부채비율, 영업이익률 등)

### Dependencies
- DART OpenAPI
- KRX 정보데이터시스템
- 한국수출입은행 환율 API

---

## Feature 4: 시장 현황

### Purpose
메인 페이지에서 KOSPI/KOSDAQ 지수와 환율을 한눈에 확인한다.

### Expected Behavior
- KOSPI 지수, 전일 대비 등락률
- KOSDAQ 지수, 전일 대비 등락률
- USD/KRW 환율, 전일 대비 등락폭

### market_index 테이블 (schemaVER2)
지수명(`idxNm`)별로 행을 분리하여 KOSPI·KOSDAQ·기타 지수를 동일 스키마로 적재:

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 전역 식별자 |
| basDd | DATE NOT NULL | 기준일자 |
| idxNm | VARCHAR(50) NOT NULL | 지수명 (KOSPI, KOSDAQ 등) |
| flucRt | DECIMAL(10,4) | 등락률 |
| clsprcIdx | DECIMAL(15,4) | 종가 지수 |
| cmpprevddIdx | DECIMAL(15,4) | 전일 대비 지수 |
| mktcap | BIGINT | 상장시가총액 |

UNIQUE: `(basDd, idxNm)`

### exchange 테이블 (schemaVER2, market_index에서 분리)
통화 단위(`curUnit`)별로 행 저장:

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| curUnit | VARCHAR(10) PK | 통화 코드 (USD, JPY 등) |
| baseDate | DATE NOT NULL | 기준일자 |
| country | VARCHAR(50) | 국가명 |
| dealBasR | DECIMAL(15,4) | 매매 기준율 |
| changeRate | DECIMAL(10,4) | 등락률 |
| changeAmount | DECIMAL(15,4) | 등락폭 |

> **VER2 변경**: 기존 market_index 테이블의 `kospi_close`, `kosdaq_close`, `usd_krw_rate` 컬럼 고정 구조에서 **지수별 다행 + exchange 독립 테이블**로 분리.

### Dependencies
- 금융위원회 지수시세정보 API
- 한국수출입은행 환율 API

---

## Feature 5: 커뮤니티 (투자일지)

### Purpose
사용자가 종목에 대한 투자 의견을 게시글로 공유하고 댓글로 토론한다.

### Expected Behavior
- 투자일지 게시글 작성 / 수정 / 삭제 (로그인 필요)
- 댓글 작성 / 삭제
- 게시글 목록 및 상세 조회

### investment_journal 테이블

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 게시글 ID |
| user_id | VARCHAR(255) FK→user.id | 작성자 (이메일) |
| title | VARCHAR(200) NOT NULL | 제목 |
| content | TEXT NOT NULL | 본문 |
| created_at | DATETIME | 작성일시 |
| updated_at | DATETIME | 수정일시 |

### comment 테이블

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | 댓글 ID |
| journal_id | BIGINT FK→investment_journal.id | 소속 게시글 |
| user_id | VARCHAR(255) FK→user.id | 작성자 (이메일) |
| content | TEXT NOT NULL | 댓글 내용 |
| created_at | DATETIME | 작성일시 |
| updated_at | DATETIME | 수정일시 |

### Status
미구현 — entity/repository/service/controller 모두 신규 작성 필요.

---

## Feature 6: 즐겨찾기

### Purpose
마이페이지에서 관심 종목을 즐겨찾기에 등록하고 빠르게 접근한다.

### user_favorite 테이블 (schemaVER2)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| user_id | VARCHAR(255) PK, FK→user.id | 유저 (이메일) |
| stock_code | VARCHAR(6) PK, FK→company.stock_code | 즐겨찾기 종목 |
| created_at | DATETIME | 등록일시 |
| updated_at | DATETIME | 수정일시 |

복합 PK: `(user_id, stock_code)`

### Status
미구현.

---

# 지표 정의

| 지표 | 분류 | 계산 방법 | 출처 |
|---|---|---|---|
| 현재가 | 기본 | — | KRX (stock_price.clpr) |
| 시가총액 | 기본 | — | KRX (stock_price.mrkt_tot_amt) |
| 발행주식수 | 기본 | — | KRX (stock_price.lstg_st_cnt) |
| 매출액 | 수익성 | — | DART (financial_statement.revenue) |
| 영업이익 | 수익성 | — | DART (financial_statement.operating_income) |
| 순이익 | 수익성 | — | DART (financial_statement.net_income) |
| 자산 | 안전성 | — | DART (financial_statement.total_assets) |
| 부채 | 안전성 | — | DART (financial_statement.total_liabilities) |
| 자본 | 안전성 | — | DART (financial_statement.total_equity) |
| EPS | 수익성 | 순이익 ÷ 발행주식수 | 계산 (stock_indicator.eps) |
| BPS | 저평가 | 자본 ÷ 발행주식수 | 계산 (stock_indicator.bps) |
| PER | 저평가 | 현재가 ÷ EPS | 계산 (stock_indicator.per) |
| PBR | 저평가 | 현재가 ÷ BPS | 계산 (stock_indicator.pbr) |
| ROE | 수익성 | 순이익 ÷ 자본 | 계산 (stock_indicator.roe) |
| 영업이익률 | 수익성 | 영업이익 ÷ 매출액 | 계산 (stock_indicator.operating_margin) |
| 부채비율 | 안전성 | 부채 ÷ 자본 | 계산 (stock_indicator.debt_ratio) |
| 배당수익률 | 저평가 | 주당배당금 ÷ 현재가 × 100 | 계산 (stock_indicator.dividend_yield, dividend_info 기반) |

---

# DB 스키마 요약 (schemaVER2 기준)

> 상세 설계: [doc/db/schemaVER2.md](./db/schemaVER2.md)

## 핵심 변경 사항 (VER1 → VER2)

| 테이블 | VER1 | VER2 |
|---|---|---|
| company | corp_code PK | **stock_code PK**, corp_code UNIQUE |
| user | id BIGINT PK + email UNIQUE | **id(email) VARCHAR(255) PK** |
| stock_price | id BIGINT PK (일별 이력) | **srtn_cd PK (최신 1건, 1:1)** |
| stock_indicator | (stock_code, calc_year) 복합 PK | **stock_code PK (최신 1건)** |
| dividend_info | id BIGINT PK, isinCd FK, 22컬럼 | **corp_code PK, 4컬럼 간소화** |
| top100 | (base_dt, rank) 복합 PK | **(base_dt, stock_code) 복합 PK**, score 추가 |
| market_index | base_dt PK, 컬럼 고정 | **id PK, idxNm별 다행** |
| exchange | market_index에 포함 | **독립 테이블, curUnit PK** |

## ERD 요약

```
market_index  (독립, 지수별 다행)
exchange      (독립, 통화별 다행)

company(stock_code PK) ──< financial_statement
company ──  stock_price           (srtn_cd PK, 1:1)
company ──  dividend_info         (corp_code PK, 1:1)
company ──  stock_indicator       (stock_code PK, 1:1)
company ──< top100                ((base_dt, stock_code) 복합 PK)
company ──< user_favorite

user(id=email PK) ──< user_favorite >── company
user(id=email PK) ──< investment_journal
user(id=email PK) ──< comment

investment_journal ──< comment
```

---

# UI / UX Requirements

- 모든 페이지는 하나의 핵심 질문에 집중하는 정보 설계 원칙 준수
- 목록 페이지: 컬럼 고정 6개 (현재가·시가총액·PER·PBR·ROE·부채비율), 정렬 클릭 가능
- 상세 페이지: 상단 요약카드 → 중단 기업정보/가치지표 → 하단 차트/테이블 순서 고정
- 차트는 최근 5년 연도별 표시
- 재무 테이블은 탭 3개로 분리 (손익계산서 / 재무상태표 / 투자지표)
- 주가 차트는 후순위 (초기 버전 미포함)
- 피그마 참고: https://www.figma.com/make/UrRh5ShCQhgjFxup1znNE6/Stock-Investment-Discovery-Service

---

# Technical Requirements

## Preferred Language
- Backend: Java
- Frontend: HTML, JavaScript, CSS (초기) → React (이전 예정)

## Preferred Framework
- Frontend: 정적 HTML/JS/CSS (초기) → React SPA (분리 이전 예정)
- Backend: Spring Boot

## Database
- MySQL (종목 정보, 재무 데이터, 계산 지표 저장)

## Infrastructure
- 초기: 로컬 또는 단일 서버 배포
- 데이터 수집은 스케줄러(Spring Scheduler 또는 cron)로 주기 실행

---

# External Tools / APIs

| Tool/API | Purpose |
|---|---|
| DART OpenAPI | 재무제표 수집 (revenue, operating_income, net_income, total_assets, total_liabilities, total_equity) |
| DART OpenAPI | 배당 정보 수집 (corp_code 기반, dividend_amount, stlm_dt) |
| KRX 정보데이터시스템 | 최신 주가 스냅샷 (clpr, lstg_st_cnt, mrkt_tot_amt), 종목코드 |
| 금융위원회 지수시세정보 API | KOSPI/KOSDAQ 지수 (idxNm별 다행) |
| 한국수출입은행 환율 API | USD/KRW 등 통화별 환율 (curUnit별 다행) |

---

# Development Rules

## Coding Style
- 컴포넌트/함수 단위 소규모 분리
- 공통 지표 계산 로직은 백엔드 단일 모듈에서 처리 (IndicatorCalculationService)
- API 응답 형식은 OpenAPI(Swagger) 문서화

## Architecture Style
- **Phase 1 (현재)**: Spring Boot + 정적 HTML/JS/CSS (Spring Boot가 정적 파일 서빙)
- **Phase 2 (이전 예정)**: Spring Boot REST API + React SPA 분리 구조
- 데이터 수집 파이프라인은 서빙 API와 분리된 독립 모듈

## Backend Package Structure
```
develope/backend/src/main/java/
└── com/example/demo/
    ├── controller/   # HTTP 요청 처리, REST 엔드포인트
    ├── dto/          # 요청/응답 데이터 전송 객체
    ├── service/      # 비즈니스 로직
    ├── entity/       # JPA 엔티티 (DB 테이블 매핑)
    ├── repository/   # DB 접근 (JPA Repository)
    └── config/       # 설정 클래스 (CORS, Security, Scheduler 등)
```

## 구현 현황 (develope/ 기준)

| 기능 | 상태 | 파일 |
|---|---|---|
| 회원가입/로그인 | ✅ 구현됨 | UserController, UserService, UserEntity |
| 종목 목록 API | ✅ 구현됨 | StockController, StockService, StockListResponse |
| 종목 상세 API | ✅ 구현됨 | FinancialController, StockDetailResponse |
| 시장 지수 API | ✅ 구현됨 | MarketIndexController, MarketIndexResponse |
| DART 수집 | ✅ 구현됨 | DartCollectorService |
| KRX 수집 | ✅ 구현됨 | KrxCollectorService |
| 배당 수집 | ✅ 구현됨 | DividendCollectorService |
| 지표 계산 | ✅ 구현됨 | IndicatorCalculationService |
| 프론트엔드 | ✅ 구현됨 | index/list/detail/login/register.html |
| 커뮤니티 (투자일지) | ❌ 미구현 | — |
| 즐겨찾기 | ❌ 미구현 | — |

> **주의**: 구현된 entity들이 VER1 스키마 기준이므로 schemaVER2 변경 사항 적용 필요.

---

# Performance Requirements

## Expected Scale
- 코스피 + 코스닥 전체 종목 (~2,500개) 처리
- 5년치 연도별 재무 데이터 저장 (financial_statement)
- stock_price / stock_indicator / dividend_info 는 종목당 최신 1건만 유지

## Optimization Priority
1. 데이터 수집 파이프라인 안정성 (API rate limit 준수)
2. 목록 페이지 로딩 속도 (DB 인덱싱, 페이지네이션)
3. 차트 렌더링 성능

---

# Security Requirements
- API Key는 `src/main/resources/application.properties`에 관리
- Git에는 키 값을 비운 양식만 커밋 (실제 값은 로컬에서 직접 입력)
- 외부 API 호출 실패 시 graceful fallback 처리

```properties
# application.properties (양식 — 실제 키는 로컬에서 입력)
dart.api.key=
krx.api.key=
exchange.api.key=
```

---

# Future Plans

| 항목 | 내용 | 우선순위 |
|---|---|---|
| schemaVER2 엔티티 마이그레이션 | UserEntity email PK, CompanyEntity stock_code PK, StockPriceEntity srtn_cd PK, StockIndicatorEntity stock_code PK, DividendEntity corp_code PK 반영 | 높음 |
| 가치주 종합점수 | PER+PBR+ROE+부채비율 백분위 환산 합산, top100.score 저장, 가중치 팀 합의 | 높음 |
| 커뮤니티 기능 | investment_journal + comment Entity/Repository/Service/Controller 신규 구현 | 중간 |
| 즐겨찾기 기능 | user_favorite Entity/Repository/Service/Controller 신규 구현 | 중간 |
| 배당수익률 | dividend_info → stock_indicator.dividend_yield 계산 파이프라인 완성 | 중간 |
| 주가 차트 연동 | KRX 또는 외부 API 연동 | 낮음 |
| React 이전 | Phase 2: Spring Boot REST API + React SPA 분리 | 낮음 |

---

# Boss AI Instructions

Boss AI must:

1. Analyze all requirements.
2. Split tasks into smaller units.
3. Assign suitable AI for each task.
4. Spawn Manager AI per segment to coordinate Sub AI.
5. Follow Coding Rule.txt strictly.
6. Prioritize stability and readability.
7. Generate task workflow before development starts.

---

# Final Notes

- 메인/목록 페이지는 초기 분리 개발 후 React Router 기반으로 합산
- 배당수익률은 목록 컬럼에서 제외, 상세 페이지에서만 표시
- 자산/부채/자본 개별 차트 대신 부채비율 단일 차트로 정보 집약
- PER·PBR은 상단 요약카드에 표시, 중단 가치지표 섹션에서 중복 제거
- schemaVER2 마이그레이션 시 외래키 참조 순서: user → company → stock_price / stock_indicator / dividend_info → top100 / user_favorite / investment_journal → comment
