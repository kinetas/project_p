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
- [ ] 가치주 종합점수 계산 (백분위 환산 합산) — 가중치 추후 확정
- [ ] 배당수익률 표시 — API 확보 후 상세 페이지에 추가
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

### User 테이블 정의

| 컬럼명 (DB) | Java 필드명 | 타입 | 설명 |
|---|---|---|---|
| id | id | BIGINT (PK, AUTO_INCREMENT) | 사용자 고유 ID |
| email | email | VARCHAR | 이메일 (unique) |
| password | password | VARCHAR | 암호화된 비밀번호 |
| nickname | nickname | VARCHAR | 닉네임 |
| created_at | createdAt | DATETIME | 가입일시 |
| updated_at | updatedAt | DATETIME | 수정일시 |

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
    종목코드 확보 (KRX)
        ↓
  DART corp_code 매핑
        ↓
    재무제표 수집 (DART)
        ↓
  PER / PBR / ROE 등 계산
        ↓
      DB 저장
        ↓
      필터링 서빙
```

### Input
- KRX 주식시세 데이터
- DART 재무제표 원문

### Output
DB에 저장된 종목별 지표 (현재가, EPS, BPS, PER, PBR, ROE, 부채비율, 영업이익률 등)

### Dependencies
- DART OpenAPI
- KRX 정보데이터시스템

---

# 지표 정의

| 지표 | 분류 | 계산 방법 | 출처 |
|---|---|---|---|
| 현재가 | 기본 | — | KRX |
| 시가총액 | 기본 | — | KRX |
| 발행주식수 | 기본 | — | KRX |
| 매출액 | 수익성 | — | DART |
| 영업이익 | 수익성 | — | DART |
| 순이익 | 수익성 | — | DART |
| 자산 | 안전성 | — | DART |
| 부채 | 안전성 | — | DART |
| 자본 | 안전성 | — | DART |
| EPS | 수익성 | 순이익 ÷ 발행주식수 | 계산 |
| BPS | 저평가 | 자본 ÷ 발행주식수 | 계산 |
| PER | 저평가 | 현재가 ÷ EPS | 계산 |
| PBR | 저평가 | 현재가 ÷ BPS | 계산 |
| ROE | 수익성 | 순이익 ÷ 자본 | 계산 |
| 영업이익률 | 수익성 | 영업이익 ÷ 매출액 | 계산 |
| 부채비율 | 안전성 | 부채 ÷ 자본 | 계산 |
| 배당수익률 | 저평가 | — | 추가 API 필요 (후순위) |

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
| DART OpenAPI | 재무제표 원문 수집 (매출, 영업이익, 자산, 부채, 자본 등) |
| KRX 정보데이터시스템 | 현재가, 시가총액, 발행주식수, 종목코드 수집 |
| 배당수익률 API | 미확보 — 탐색 필요 (후순위) |

---

# Development Rules

## Coding Style
- 컴포넌트/함수 단위 소규모 분리
- 공통 지표 계산 로직은 백엔드 단일 모듈에서 처리
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

---

# Performance Requirements

## Expected Scale
- 코스피 + 코스닥 전체 종목 (~2,500개) 처리
- 5년치 연도별 재무 데이터 저장

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
```

---

# Future Plans

| 항목 | 내용 | 우선순위 |
|---|---|---|
| 가치주 종합점수 | PER+PBR+ROE+부채비율 백분위 환산 합산, 가중치 팀 합의 | 높음 |
| 배당수익률 | 공공 API 탐색 후 상세 페이지 추가 | 중간 |
| 주가 차트 연동 | KRX 또는 외부 API 연동 | 낮음 |

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
- 배당수익률은 목록 컬럼에서 제외, 상세 페이지에서만 표시 (API 확보 시)
- 자산/부채/자본 개별 차트 대신 부채비율 단일 차트로 정보 집약
- PER·PBR은 상단 요약카드에 표시, 중단 가치지표 섹션에서 중복 제거
