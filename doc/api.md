# API 명세서

> 참고: [PRD.md](./PRD.md) · [schemaVER2.md](./db/schemaVER2.md)  
> Base URL: `http://localhost:8080`  
> 인증 방식: 세션 쿠키 (로그인 후 서버 `Set-Cookie` 발급, 이후 요청에 자동 포함)

---

## 목차

- [백엔드 API](#백엔드-api)
  - [인증 (Auth)](#인증-auth)
  - [종목 (Stock)](#종목-stock)
  - [재무제표 (Financial)](#재무제표-financial)
  - [시장 현황 (Market)](#시장-현황-market)
  - [커뮤니티 (Journal)](#커뮤니티-journal)
  - [즐겨찾기 (Favorite)](#즐겨찾기-favorite)
- [프론트 API](#프론트-api)
  - [메인 페이지](#메인-페이지)
  - [종목 목록 페이지](#종목-목록-페이지)
  - [종목 상세 페이지](#종목-상세-페이지)
  - [로그인 페이지](#로그인-페이지)
  - [회원가입 페이지](#회원가입-페이지)
  - [투자일지 목록 페이지](#투자일지-목록-페이지)
  - [투자일지 상세 페이지](#투자일지-상세-페이지)
  - [마이페이지 (즐겨찾기)](#마이페이지-즐겨찾기)

---

# 백엔드 API

> 기능명 · URL · 서비스명 · 엔드포인트 · 요청방식 · 요청파라미터 · 요청헤더 · 요청바디 · 응답형식 · 설명

---

## 인증 (Auth)

---

### 회원가입

| 항목 | 내용 |
|---|---|
| **기능명** | 회원가입 |
| **URL** | `/api/auth/register` |
| **서비스명** | UserService |
| **엔드포인트** | POST `/api/auth/register` |
| **요청방식** | POST |
| **요청파라미터** | 없음 |
| **요청헤더** | `Content-Type: application/json` |

**요청바디**

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "투자고수"
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| email | String | ✅ | 이메일 형식, `user.id(PK)`로 저장 |
| password | String | ✅ | 최소 8자, BCrypt 해시 저장 |
| nickname | String | | 최대 50자 |

**응답형식** `201 Created`

```json
{
  "id": "user@example.com",
  "email": "user@example.com",
  "nickname": "투자고수",
  "role": "USER",
  "createdAt": "2026-06-21T10:00:00"
}
```

| 필드 | 타입 | 출처 컬럼 |
|---|---|---|
| id | String | user.id |
| email | String | user.id |
| nickname | String | user.nickname |
| role | String | user.role |
| createdAt | DateTime | user.created_at |

**에러**

| 상태 | 조건 |
|---|---|
| 400 | 이메일 형식 오류 · 비밀번호 8자 미만 · 닉네임 50자 초과 |
| 409 | 이미 사용 중인 이메일 |

**설명** 이메일을 PK(`user.id`)로 직접 사용하여 신규 회원을 등록한다.

---

### 로그인

| 항목 | 내용 |
|---|---|
| **기능명** | 로그인 |
| **URL** | `/api/auth/login` |
| **서비스명** | UserService |
| **엔드포인트** | POST `/api/auth/login` |
| **요청방식** | POST |
| **요청파라미터** | 없음 |
| **요청헤더** | `Content-Type: application/json` |

**요청바디**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**응답형식** `200 OK`

```json
{
  "id": "user@example.com",
  "email": "user@example.com",
  "nickname": "투자고수",
  "role": "USER",
  "createdAt": "2026-06-21T10:00:00"
}
```

응답 헤더: `Set-Cookie: JSESSIONID=abc123; Path=/; HttpOnly`

**에러**

| 상태 | 조건 |
|---|---|
| 400 | 이메일 형식 오류 |
| 401 | 이메일 또는 비밀번호 불일치 |

**설명** 인증 성공 시 서버 세션을 생성하고 쿠키를 발급한다. 이후 요청에서 쿠키가 자동 포함된다.

---

### 로그아웃

| 항목 | 내용 |
|---|---|
| **기능명** | 로그아웃 |
| **URL** | `/api/auth/logout` |
| **서비스명** | UserService |
| **엔드포인트** | POST `/api/auth/logout` |
| **요청방식** | POST |
| **요청파라미터** | 없음 |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식** `200 OK`

```json
{
  "message": "로그아웃 되었습니다."
}
```

**설명** 서버 세션을 무효화한다.

---

## 종목 (Stock)

---

### 종목 목록 조회

| 항목 | 내용 |
|---|---|
| **기능명** | 종목 목록 조회 |
| **URL** | `/api/stocks` |
| **서비스명** | StockService |
| **엔드포인트** | GET `/api/stocks` |
| **요청방식** | GET |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**요청파라미터** (Query String)

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| keyword | String | | | 종목명 또는 종목코드 부분 검색 |
| market | String | | | `KOSPI` / `KOSDAQ` |
| minPer | Double | | | PER 하한 |
| maxPer | Double | | | PER 상한 |
| minPbr | Double | | | PBR 하한 |
| maxPbr | Double | | | PBR 상한 |
| minRoe | Double | | | ROE 하한 (%) |
| maxDebtRatio | Double | | | 부채비율 상한 (%) |
| sort | String | | `mrktTotAmt` | 정렬 기준: `per` · `pbr` · `roe` · `debtRatio` · `mrktTotAmt` |
| dir | String | | `desc` | 정렬 방향: `asc` · `desc` |
| page | int | | `0` | 페이지 번호 (0-based) |
| size | int | | `20` | 페이지 크기 (최대 100) |

**응답형식** `200 OK`

```json
{
  "content": [
    {
      "stockCode": "005930",
      "corpName": "삼성전자",
      "corpCls": "Y",
      "basDate": "2026-06-21",
      "clpr": 75000,
      "fltRt": 0.67,
      "mrktTotAmt": 4478500000000,
      "per": 12.34,
      "pbr": 1.56,
      "roe": 12.67,
      "debtRatio": 35.21,
      "dividendYield": 2.13
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2483,
  "totalPages": 125
}
```

| 응답 필드 | 타입 | 출처 테이블.컬럼 |
|---|---|---|
| stockCode | String | company.stock_code |
| corpName | String | company.corp_name |
| corpCls | String | company.corp_cls (Y:유가 K:코스닥) |
| basDate | String | stock_price.bas_dt |
| clpr | Long | stock_price.clpr |
| fltRt | Decimal | stock_price.flt_rt |
| mrktTotAmt | Long | stock_price.mrkt_tot_amt |
| per | Decimal | stock_indicator.per |
| pbr | Decimal | stock_indicator.pbr |
| roe | Decimal | stock_indicator.roe |
| debtRatio | Decimal | stock_indicator.debt_ratio |
| dividendYield | Decimal | stock_indicator.dividend_yield |

**설명** `company` · `stock_price` · `stock_indicator`를 조인하여 반환한다. 페이지네이션 기본 20건.

---

### TOP10 조회

| 항목 | 내용 |
|---|---|
| **기능명** | TOP10 탭 조회 |
| **URL** | `/api/stocks/top10` |
| **서비스명** | StockService |
| **엔드포인트** | GET `/api/stocks/top10` |
| **요청방식** | GET |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**요청파라미터**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| type | String | | `value` | `value` (가치주 종합점수) · `lowPer` (저PER) · `highRoe` (고ROE) |

**응답형식** `200 OK`

```json
{
  "type": "lowPer",
  "stocks": [
    {
      "stockCode": "005930",
      "corpName": "삼성전자",
      "corpCls": "Y",
      "clpr": 75000,
      "fltRt": 0.67,
      "mrktTotAmt": 4478500000000,
      "per": 4.12,
      "pbr": 0.98,
      "roe": 24.31,
      "debtRatio": 35.21,
      "dividendYield": 2.13
    }
  ]
}
```

**설명** 목록 페이지 상단 탭 전환에 사용. `type`에 따라 정렬 기준이 달라진다.

---

### TOP100 조회

| 항목 | 내용 |
|---|---|
| **기능명** | 가치주 TOP100 조회 |
| **URL** | `/api/stocks/top100` |
| **서비스명** | StockService |
| **엔드포인트** | GET `/api/stocks/top100` |
| **요청방식** | GET |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**요청파라미터**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| date | String | | 최신 기준일 | 기준일자 `YYYY-MM-DD` |

**응답형식** `200 OK`

```json
{
  "baseDate": "2026-06-21",
  "stocks": [
    {
      "rank": 1,
      "stockCode": "005930",
      "corpName": "삼성전자",
      "score": 87,
      "per": 12.34,
      "pbr": 1.56,
      "roe": 12.67,
      "debtRatio": 35.21,
      "dividendYield": 2.13
    }
  ]
}
```

| 응답 필드 | 출처 테이블.컬럼 |
|---|---|
| baseDate | top100.base_dt |
| rank | 서버 계산 (score 내림차순 순번) |
| stockCode | top100.stock_code |
| score | top100.score |

**설명** `top100` 테이블의 특정 기준일 스냅샷을 score 내림차순으로 반환한다.

---

### 메인 노출 종목 조회

| 항목 | 내용 |
|---|---|
| **기능명** | 메인 노출 종목 조회 |
| **URL** | `/api/stocks/featured` |
| **서비스명** | StockService |
| **엔드포인트** | GET `/api/stocks/featured` |
| **요청방식** | GET |
| **요청파라미터** | 없음 |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**응답형식** `200 OK` — 배열 (최대 10건)

```json
[
  {
    "stockCode": "005930",
    "corpName": "삼성전자",
    "corpCls": "Y",
    "clpr": 75000,
    "fltRt": 0.67,
    "mrktTotAmt": 4478500000000,
    "per": 12.34,
    "pbr": 1.56,
    "roe": 12.67,
    "debtRatio": 35.21,
    "dividendYield": 2.13
  }
]
```

**설명** 메인 페이지 종목 섹션용. 시가총액 상위 10종목을 반환한다.

---

### 종목 상세 조회

| 항목 | 내용 |
|---|---|
| **기능명** | 종목 상세 조회 |
| **URL** | `/api/stocks/{stockCode}` |
| **서비스명** | StockService |
| **엔드포인트** | GET `/api/stocks/{stockCode}` |
| **요청방식** | GET |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**요청파라미터** (Path)

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| stockCode | String | ✅ | 종목코드 6자리 (예: `005930`) |

**응답형식** `200 OK`

```json
{
  "stockCode": "005930",
  "corpName": "삼성전자",
  "corpCls": "Y",
  "basDate": "2026-06-21",
  "clpr": 75000,
  "mkp": 74500,
  "fltRt": 0.67,
  "lstgStCnt": 5969782550,
  "mrktTotAmt": 4478500000000,
  "eps": 6081.23,
  "bps": 48041.56,
  "per": 12.34,
  "pbr": 1.56,
  "roe": 12.67,
  "operatingMargin": 8.45,
  "debtRatio": 35.21,
  "dividendYield": 2.13,
  "calculatedAt": "2026-06-21T03:00:00"
}
```

| 응답 필드 | 타입 | 출처 테이블.컬럼 | PRD 위치 |
|---|---|---|---|
| stockCode | String | company.stock_code | 요약카드 |
| corpName | String | company.corp_name | 요약카드 |
| corpCls | String | company.corp_cls | 기업정보 |
| basDate | String | stock_price.bas_dt | — |
| clpr | Long | stock_price.clpr | 요약카드 — 현재가 |
| mkp | Long | stock_price.mkp | 기업정보 — 시가 |
| fltRt | Decimal | stock_price.flt_rt | 요약카드 |
| lstgStCnt | Long | stock_price.lstg_st_cnt | — |
| mrktTotAmt | Long | stock_price.mrkt_tot_amt | 요약카드 — 시가총액 |
| eps | Decimal | stock_indicator.eps | 가치지표 |
| bps | Decimal | stock_indicator.bps | 가치지표 |
| per | Decimal | stock_indicator.per | 요약카드 |
| pbr | Decimal | stock_indicator.pbr | 요약카드 |
| roe | Decimal | stock_indicator.roe | 요약카드 · 가치지표 |
| operatingMargin | Decimal | stock_indicator.operating_margin | 가치지표 |
| debtRatio | Decimal | stock_indicator.debt_ratio | 가치지표 |
| dividendYield | Decimal | stock_indicator.dividend_yield | 요약카드 |
| calculatedAt | DateTime | stock_indicator.calculated_at | — |

**에러**

| 상태 | 조건 |
|---|---|
| 404 | 존재하지 않는 종목코드 |

**설명** 상세 페이지 상단 요약카드 + 중단 기업정보/가치지표에 필요한 데이터를 단일 호출로 반환한다.

---

## 재무제표 (Financial)

---

### 재무제표 5년 조회

| 항목 | 내용 |
|---|---|
| **기능명** | 재무제표 5년 조회 |
| **URL** | `/api/stocks/{stockCode}/financials` |
| **서비스명** | StockService |
| **엔드포인트** | GET `/api/stocks/{stockCode}/financials` |
| **요청방식** | GET |
| **요청바디** | 없음 |

**요청파라미터**

| 파라미터 | 타입 | 위치 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|---|
| stockCode | String | Path | ✅ | | 종목코드 6자리 |
| fsDivision | String | Query | | `CFS` | `CFS` (연결) · `OFS` (개별) |

**요청헤더** 없음

**응답형식** `200 OK` — 연도 오름차순 배열, 최대 5건

```json
[
  {
    "year": "2021",
    "revenue": 279604800000000,
    "operatingIncome": 51633900000000,
    "netIncome": 39907400000000,
    "totalAssets": 426621200000000,
    "totalLiabilities": 102115800000000,
    "totalEquity": 324505400000000,
    "operatingMargin": 18.47,
    "debtRatio": 31.47
  },
  {
    "year": "2022",
    "revenue": 302231200000000,
    "operatingIncome": 43376900000000,
    "netIncome": 55654600000000,
    "totalAssets": 448367700000000,
    "totalLiabilities": 93674200000000,
    "totalEquity": 354693500000000,
    "operatingMargin": 14.35,
    "debtRatio": 26.41
  }
]
```

| 응답 필드 | 타입 | 출처 테이블.컬럼 | 비고 |
|---|---|---|---|
| year | String | financial_statement.bsns_year | |
| revenue | Long | financial_statement.revenue | |
| operatingIncome | Long | financial_statement.operating_income | |
| netIncome | Long | financial_statement.net_income | |
| totalAssets | Long | financial_statement.total_assets | |
| totalLiabilities | Long | financial_statement.total_liabilities | |
| totalEquity | Long | financial_statement.total_equity | |
| operatingMargin | Decimal | 서버 계산 | operating_income ÷ revenue × 100 |
| debtRatio | Decimal | 서버 계산 | total_liabilities ÷ total_equity × 100 |

**에러**

| 상태 | 조건 |
|---|---|
| 404 | 존재하지 않는 종목코드 |

**설명** 상세 페이지 하단 차트 3개(매출+영업이익 / ROE / 부채비율)와 연도별 테이블 탭 2개(손익계산서 / 재무상태표)에 사용된다. ROE 추이는 `/api/stocks/{stockCode}` 의 단일 시점 roe가 아닌 연도별 계산값이 필요하므로 서버에서 `net_income ÷ total_equity × 100`을 추가 계산하여 포함하는 것을 권장한다.

---

## 시장 현황 (Market)

---

### 지수 조회

| 항목 | 내용 |
|---|---|
| **기능명** | 시장 지수 조회 |
| **URL** | `/api/market/indices` |
| **서비스명** | MarketService |
| **엔드포인트** | GET `/api/market/indices` |
| **요청방식** | GET |
| **요청파라미터** | 없음 |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**응답형식** `200 OK`

```json
[
  {
    "idxNm": "KOSPI",
    "basDate": "2026-06-21",
    "clsprcIdx": 2678.22,
    "cmpprevddIdx": 32.84,
    "flucRt": 1.24,
    "mktcap": 2134567890000000
  },
  {
    "idxNm": "KOSDAQ",
    "basDate": "2026-06-21",
    "clsprcIdx": 758.45,
    "cmpprevddIdx": -5.19,
    "flucRt": -0.68,
    "mktcap": 456789012000000
  }
]
```

| 응답 필드 | 타입 | 출처 테이블.컬럼 |
|---|---|---|
| idxNm | String | market_index.idxNm |
| basDate | String | market_index.basDd |
| clsprcIdx | Decimal | market_index.clsprcIdx |
| cmpprevddIdx | Decimal | market_index.cmpprevddIdx |
| flucRt | Decimal | market_index.flucRt |
| mktcap | Long | market_index.mktcap |

**설명** `market_index` 테이블 최신 `basDd` 기준으로 KOSPI · KOSDAQ 행을 반환한다. `idxNm`별 다행 구조(schemaVER2).

---

### 환율 조회

| 항목 | 내용 |
|---|---|
| **기능명** | 환율 조회 |
| **URL** | `/api/market/exchange` |
| **서비스명** | MarketService |
| **엔드포인트** | GET `/api/market/exchange` |
| **요청방식** | GET |
| **요청파라미터** | 없음 |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**응답형식** `200 OK`

```json
[
  {
    "curUnit": "USD",
    "country": "미국",
    "baseDate": "2026-06-21",
    "dealBasR": 1328.50,
    "changeRate": 0.15,
    "changeAmount": 2.00
  },
  {
    "curUnit": "JPY(100)",
    "country": "일본",
    "baseDate": "2026-06-21",
    "dealBasR": 895.23,
    "changeRate": -0.22,
    "changeAmount": -2.01
  }
]
```

| 응답 필드 | 타입 | 출처 테이블.컬럼 |
|---|---|---|
| curUnit | String | exchange.curUnit (PK) |
| country | String | exchange.country |
| baseDate | String | exchange.baseDate |
| dealBasR | Decimal | exchange.dealBasR |
| changeRate | Decimal | exchange.changeRate |
| changeAmount | Decimal | exchange.changeAmount |

**설명** `exchange` 테이블 최신 `baseDate` 기준 전 통화를 반환한다. 통화별 다행 구조(schemaVER2).

---

## 커뮤니티 (Journal)

> 쓰기 엔드포인트(POST · PUT · DELETE)는 로그인 필요. 미인증 시 `401` 반환.

---

### 투자일지 목록 조회

| 항목 | 내용 |
|---|---|
| **기능명** | 투자일지 목록 조회 |
| **URL** | `/api/journals` |
| **서비스명** | JournalService |
| **엔드포인트** | GET `/api/journals` |
| **요청방식** | GET |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**요청파라미터**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| page | int | | `0` | 페이지 번호 |
| size | int | | `20` | 페이지 크기 |

**응답형식** `200 OK`

```json
{
  "content": [
    {
      "id": 1,
      "userId": "user@example.com",
      "nickname": "투자고수",
      "title": "삼성전자 저PER 분석",
      "createdAt": "2026-06-21T10:00:00",
      "updatedAt": "2026-06-21T10:00:00",
      "commentCount": 3
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

**설명** 목록에서는 본문(content)을 제외하고 반환한다.

---

### 투자일지 작성

| 항목 | 내용 |
|---|---|
| **기능명** | 투자일지 작성 |
| **URL** | `/api/journals` |
| **서비스명** | JournalService |
| **엔드포인트** | POST `/api/journals` |
| **요청방식** | POST |
| **요청파라미터** | 없음 |
| **요청헤더** | `Content-Type: application/json` · `Cookie: JSESSIONID={세션ID}` |

**요청바디**

```json
{
  "title": "삼성전자 저PER 분석",
  "content": "현재 PER 12배 수준으로 역사적 저점..."
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| title | String | ✅ | 최대 200자 |
| content | String | ✅ | |

**응답형식** `201 Created`

```json
{
  "id": 1,
  "userId": "user@example.com",
  "nickname": "투자고수",
  "title": "삼성전자 저PER 분석",
  "content": "현재 PER 12배 수준으로 역사적 저점...",
  "createdAt": "2026-06-21T10:00:00",
  "updatedAt": "2026-06-21T10:00:00"
}
```

**에러**

| 상태 | 조건 |
|---|---|
| 400 | title 누락 · 200자 초과 · content 누락 |
| 401 | 비로그인 |

**설명** 세션에서 `userId`(이메일)를 추출하여 `investment_journal.user_id`에 저장한다.

---

### 투자일지 상세 조회

| 항목 | 내용 |
|---|---|
| **기능명** | 투자일지 상세 조회 |
| **URL** | `/api/journals/{id}` |
| **서비스명** | JournalService |
| **엔드포인트** | GET `/api/journals/{id}` |
| **요청방식** | GET |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**요청파라미터** (Path)

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| id | Long | ✅ | 게시글 ID |

**응답형식** `200 OK`

```json
{
  "id": 1,
  "userId": "user@example.com",
  "nickname": "투자고수",
  "title": "삼성전자 저PER 분석",
  "content": "현재 PER 12배 수준으로 역사적 저점...",
  "createdAt": "2026-06-21T10:00:00",
  "updatedAt": "2026-06-21T10:00:00",
  "comments": [
    {
      "id": 1,
      "userId": "other@example.com",
      "nickname": "가치투자자",
      "content": "좋은 분석이네요.",
      "createdAt": "2026-06-21T11:00:00",
      "updatedAt": "2026-06-21T11:00:00"
    }
  ]
}
```

**에러**

| 상태 | 조건 |
|---|---|
| 404 | 존재하지 않는 게시글 |

**설명** 게시글 본문과 하위 댓글 전체를 함께 반환한다.

---

### 투자일지 수정

| 항목 | 내용 |
|---|---|
| **기능명** | 투자일지 수정 |
| **URL** | `/api/journals/{id}` |
| **서비스명** | JournalService |
| **엔드포인트** | PUT `/api/journals/{id}` |
| **요청방식** | PUT |
| **요청헤더** | `Content-Type: application/json` · `Cookie: JSESSIONID={세션ID}` |

**요청파라미터** (Path)

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| id | Long | ✅ | 게시글 ID |

**요청바디**

```json
{
  "title": "수정된 제목",
  "content": "수정된 본문..."
}
```

**응답형식** `200 OK` — 수정된 게시글 전체 (상세 조회 응답과 동일 구조)

**에러**

| 상태 | 조건 |
|---|---|
| 401 | 비로그인 |
| 403 | 작성자 본인이 아닌 경우 |
| 404 | 존재하지 않는 게시글 |

**설명** 세션의 `userId`와 `investment_journal.user_id`를 비교하여 본인 확인 후 수정한다.

---

### 투자일지 삭제

| 항목 | 내용 |
|---|---|
| **기능명** | 투자일지 삭제 |
| **URL** | `/api/journals/{id}` |
| **서비스명** | JournalService |
| **엔드포인트** | DELETE `/api/journals/{id}` |
| **요청방식** | DELETE |
| **요청파라미터** | Path: `id` (Long, 게시글 ID) |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식** `204 No Content`

**에러**

| 상태 | 조건 |
|---|---|
| 401 | 비로그인 |
| 403 | 작성자 본인이 아닌 경우 |
| 404 | 존재하지 않는 게시글 |

**설명** 삭제 시 하위 `comment` 행도 CASCADE 삭제된다 (`fk_comment_journal ON DELETE CASCADE`).

---

### 댓글 작성

| 항목 | 내용 |
|---|---|
| **기능명** | 댓글 작성 |
| **URL** | `/api/journals/{id}/comments` |
| **서비스명** | CommentService |
| **엔드포인트** | POST `/api/journals/{id}/comments` |
| **요청방식** | POST |
| **요청헤더** | `Content-Type: application/json` · `Cookie: JSESSIONID={세션ID}` |

**요청파라미터** (Path)

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| id | Long | ✅ | 소속 게시글 ID |

**요청바디**

```json
{
  "content": "좋은 분석이네요."
}
```

**응답형식** `201 Created`

```json
{
  "id": 1,
  "journalId": 1,
  "userId": "other@example.com",
  "nickname": "가치투자자",
  "content": "좋은 분석이네요.",
  "createdAt": "2026-06-21T11:00:00",
  "updatedAt": "2026-06-21T11:00:00"
}
```

**에러**

| 상태 | 조건 |
|---|---|
| 400 | content 누락 |
| 401 | 비로그인 |
| 404 | 존재하지 않는 게시글 |

**설명** `comment.journal_id` = 경로의 `id`, `comment.user_id` = 세션의 이메일.

---

### 댓글 삭제

| 항목 | 내용 |
|---|---|
| **기능명** | 댓글 삭제 |
| **URL** | `/api/journals/{journalId}/comments/{commentId}` |
| **서비스명** | CommentService |
| **엔드포인트** | DELETE `/api/journals/{journalId}/comments/{commentId}` |
| **요청방식** | DELETE |
| **요청파라미터** | Path: `journalId` (Long) · `commentId` (Long) |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식** `204 No Content`

**에러**

| 상태 | 조건 |
|---|---|
| 401 | 비로그인 |
| 403 | 작성자 본인이 아닌 경우 |
| 404 | 존재하지 않는 게시글 또는 댓글 |

---

## 즐겨찾기 (Favorite)

> 모든 엔드포인트 로그인 필요. 미인증 시 `401` 반환.

---

### 즐겨찾기 목록 조회

| 항목 | 내용 |
|---|---|
| **기능명** | 즐겨찾기 목록 조회 |
| **URL** | `/api/favorites` |
| **서비스명** | FavoriteService |
| **엔드포인트** | GET `/api/favorites` |
| **요청방식** | GET |
| **요청파라미터** | 없음 |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식** `200 OK`

```json
[
  {
    "stockCode": "005930",
    "corpName": "삼성전자",
    "corpCls": "Y",
    "clpr": 75000,
    "fltRt": 0.67,
    "per": 12.34,
    "pbr": 1.56,
    "roe": 12.67,
    "debtRatio": 35.21,
    "addedAt": "2026-06-01T09:00:00"
  }
]
```

| 응답 필드 | 출처 테이블.컬럼 |
|---|---|
| stockCode | user_favorite.stock_code |
| addedAt | user_favorite.created_at |
| 나머지 | stock_price · company · stock_indicator JOIN |

**설명** 세션의 이메일(`user_id`)로 `user_favorite`를 조회하고 종목 정보를 JOIN하여 반환한다.

---

### 즐겨찾기 추가

| 항목 | 내용 |
|---|---|
| **기능명** | 즐겨찾기 추가 |
| **URL** | `/api/favorites/{stockCode}` |
| **서비스명** | FavoriteService |
| **엔드포인트** | POST `/api/favorites/{stockCode}` |
| **요청방식** | POST |
| **요청파라미터** | Path: `stockCode` (String, 종목코드 6자리) |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식** `201 Created`

```json
{
  "stockCode": "005930",
  "addedAt": "2026-06-21T10:00:00"
}
```

**에러**

| 상태 | 조건 |
|---|---|
| 404 | 존재하지 않는 종목코드 |
| 409 | 이미 즐겨찾기에 추가된 종목 |

**설명** `user_favorite` 복합 PK `(user_id, stock_code)` 기반으로 INSERT한다.

---

### 즐겨찾기 삭제

| 항목 | 내용 |
|---|---|
| **기능명** | 즐겨찾기 삭제 |
| **URL** | `/api/favorites/{stockCode}` |
| **서비스명** | FavoriteService |
| **엔드포인트** | DELETE `/api/favorites/{stockCode}` |
| **요청방식** | DELETE |
| **요청파라미터** | Path: `stockCode` (String, 종목코드 6자리) |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식** `204 No Content`

**에러**

| 상태 | 조건 |
|---|---|
| 404 | 즐겨찾기에 없는 종목 |

---

# 프론트 API

> 각 페이지가 호출하는 백엔드 API를 정리합니다.  
> 페이지명 · URL · 서비스명 · 엔드포인트 · 요청방식 · 요청파라미터 · 요청헤더 · 요청바디 · 응답형식 · 설명

---

## 메인 페이지

**페이지 URL** `/` (index.html)

---

### 시장 지수 로드

| 항목 | 내용 |
|---|---|
| **페이지명** | 메인 페이지 |
| **URL** | `/api/market/indices` |
| **서비스명** | home.js |
| **엔드포인트** | GET `/api/market/indices` |
| **요청방식** | GET |
| **요청파라미터** | 없음 |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**응답형식**

```json
[
  { "idxNm": "KOSPI", "clsprcIdx": 2678.22, "flucRt": 1.24, "cmpprevddIdx": 32.84 },
  { "idxNm": "KOSDAQ", "clsprcIdx": 758.45, "flucRt": -0.68, "cmpprevddIdx": -5.19 }
]
```

**설명** 페이지 로드 시 최상단 KOSPI · KOSDAQ 지수 표시에 사용.

---

### 환율 로드

| 항목 | 내용 |
|---|---|
| **페이지명** | 메인 페이지 |
| **URL** | `/api/market/exchange` |
| **서비스명** | home.js |
| **엔드포인트** | GET `/api/market/exchange` |
| **요청방식** | GET |
| **요청파라미터** | 없음 |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**응답형식**

```json
[
  { "curUnit": "USD", "dealBasR": 1328.50, "changeRate": 0.15 }
]
```

**설명** 페이지 로드 시 USD/KRW 환율 표시에 사용.

---

### 메인 노출 종목 로드

| 항목 | 내용 |
|---|---|
| **페이지명** | 메인 페이지 |
| **URL** | `/api/stocks/featured` |
| **서비스명** | home.js |
| **엔드포인트** | GET `/api/stocks/featured` |
| **요청방식** | GET |
| **요청파라미터** | 없음 |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**응답형식**

```json
[
  { "stockCode": "005930", "corpName": "삼성전자", "clpr": 75000, "fltRt": 0.67, "per": 12.34, "roe": 12.67 }
]
```

**설명** 메인 페이지 종목 섹션에 시가총액 상위 10종목 카드 표시.

---

## 종목 목록 페이지

**페이지 URL** `/list` (list.html)

---

### 종목 목록 로드

| 항목 | 내용 |
|---|---|
| **페이지명** | 종목 목록 페이지 |
| **URL** | `/api/stocks` |
| **서비스명** | list.js |
| **엔드포인트** | GET `/api/stocks` |
| **요청방식** | GET |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**요청파라미터**

| 파라미터 | 설명 | UI 연결 |
|---|---|---|
| keyword | 검색창 입력값 | 상단 검색창 |
| market | 시장 선택 | 필터바 |
| minPer / maxPer | PER 범위 | 필터바 |
| minRoe | ROE 하한 | 필터바 |
| maxDebtRatio | 부채비율 상한 | 필터바 |
| sort / dir | 정렬 기준/방향 | 테이블 헤더 클릭 |
| page / size | 페이지 번호 / 크기 | 하단 페이지네이션 |

**응답형식**

```json
{
  "content": [ { "stockCode": "...", "corpName": "...", "clpr": 0, "per": 0, "pbr": 0, "roe": 0, "debtRatio": 0 } ],
  "page": 0, "size": 20, "totalElements": 2483, "totalPages": 125
}
```

**설명** 필터·정렬 변경 시 또는 페이지 이동 시 재호출. 테이블 6개 컬럼(현재가·시가총액·PER·PBR·ROE·부채비율) 표시.

---

### TOP10 탭 전환

| 항목 | 내용 |
|---|---|
| **페이지명** | 종목 목록 페이지 |
| **URL** | `/api/stocks/top10` |
| **서비스명** | list.js |
| **엔드포인트** | GET `/api/stocks/top10` |
| **요청방식** | GET |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**요청파라미터**

| 파라미터 | 설명 | UI 연결 |
|---|---|---|
| type | `value` · `lowPer` · `highRoe` | 상단 탭 클릭 |

**응답형식**

```json
{ "type": "lowPer", "stocks": [ { "stockCode": "...", "corpName": "...", "per": 0 } ] }
```

**설명** 탭 클릭 시 호출. 가치주 TOP10 / 저PER TOP10 / 고ROE TOP10 섹션 갱신.

---

## 종목 상세 페이지

**페이지 URL** `/detail?code={stockCode}` (detail.html)

---

### 종목 요약 + 지표 로드

| 항목 | 내용 |
|---|---|
| **페이지명** | 종목 상세 페이지 |
| **URL** | `/api/stocks/{stockCode}` |
| **서비스명** | detail.js |
| **엔드포인트** | GET `/api/stocks/{stockCode}` |
| **요청방식** | GET |
| **요청파라미터** | Path: `stockCode` (URL 파라미터 `code`에서 추출) |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**응답형식**

```json
{
  "stockCode": "005930", "corpName": "삼성전자",
  "clpr": 75000, "mrktTotAmt": 4478500000000,
  "eps": 6081.23, "bps": 48041.56,
  "per": 12.34, "pbr": 1.56, "roe": 12.67,
  "operatingMargin": 8.45, "debtRatio": 35.21, "dividendYield": 2.13
}
```

**설명** 페이지 로드 즉시 호출. 상단 요약카드(현재가·시총·PER·PBR·ROE·배당수익률) + 중단 가치지표(EPS·BPS·ROE·부채비율·영업이익률) 렌더링.

---

### 재무제표 5년 로드

| 항목 | 내용 |
|---|---|
| **페이지명** | 종목 상세 페이지 |
| **URL** | `/api/stocks/{stockCode}/financials` |
| **서비스명** | detail.js |
| **엔드포인트** | GET `/api/stocks/{stockCode}/financials` |
| **요청방식** | GET |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**요청파라미터**

| 파라미터 | 위치 | 설명 |
|---|---|---|
| stockCode | Path | URL 파라미터 `code`에서 추출 |
| fsDivision | Query | 연결/개별 탭 선택값 (기본 `CFS`) |

**응답형식**

```json
[
  { "year": "2021", "revenue": 0, "operatingIncome": 0, "netIncome": 0,
    "totalAssets": 0, "totalLiabilities": 0, "totalEquity": 0,
    "operatingMargin": 0, "debtRatio": 0 }
]
```

**설명** 페이지 로드 즉시 호출. 하단 차트 3개(매출+영업이익 / ROE / 부채비율) 및 연도별 테이블 탭 2개(손익계산서·재무상태표) 렌더링.

---

### 즐겨찾기 상태 확인

| 항목 | 내용 |
|---|---|
| **페이지명** | 종목 상세 페이지 |
| **URL** | `/api/favorites/{stockCode}` |
| **서비스명** | detail.js |
| **엔드포인트** | GET `/api/favorites/{stockCode}` |
| **요청방식** | GET |
| **요청파라미터** | Path: `stockCode` |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식** `200 OK` (즐겨찾기 등록됨) · `404` (미등록)

**설명** 로그인 상태에서 페이지 로드 시 즐겨찾기 버튼 상태(채움/빈 하트) 초기화에 사용.

---

### 즐겨찾기 토글

| 항목 | 내용 |
|---|---|
| **페이지명** | 종목 상세 페이지 |
| **URL** | `/api/favorites/{stockCode}` |
| **서비스명** | detail.js |
| **엔드포인트** | POST 또는 DELETE `/api/favorites/{stockCode}` |
| **요청방식** | POST (추가) · DELETE (삭제) |
| **요청파라미터** | Path: `stockCode` |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식** `201 Created` (추가) · `204 No Content` (삭제)

**설명** 즐겨찾기 버튼 클릭 시 현재 상태에 따라 POST 또는 DELETE 분기 호출.

---

## 로그인 페이지

**페이지 URL** `/login` (login.html)

---

### 로그인 요청

| 항목 | 내용 |
|---|---|
| **페이지명** | 로그인 페이지 |
| **URL** | `/api/auth/login` |
| **서비스명** | auth.js |
| **엔드포인트** | POST `/api/auth/login` |
| **요청방식** | POST |
| **요청파라미터** | 없음 |
| **요청헤더** | `Content-Type: application/json` |

**요청바디**

```json
{ "email": "user@example.com", "password": "password123" }
```

**응답형식** `200 OK` → 세션 쿠키 발급 → 메인 페이지로 리디렉션

**설명** 폼 제출 시 호출. 성공 시 `/`로 이동, 실패 시 에러 메시지 표시.

---

## 회원가입 페이지

**페이지 URL** `/register` (register.html)

---

### 회원가입 요청

| 항목 | 내용 |
|---|---|
| **페이지명** | 회원가입 페이지 |
| **URL** | `/api/auth/register` |
| **서비스명** | auth.js |
| **엔드포인트** | POST `/api/auth/register` |
| **요청방식** | POST |
| **요청파라미터** | 없음 |
| **요청헤더** | `Content-Type: application/json` |

**요청바디**

```json
{ "email": "user@example.com", "password": "password123", "nickname": "투자고수" }
```

**응답형식** `201 Created` → 로그인 페이지로 리디렉션 · `409` → 중복 이메일 에러 표시

**설명** 폼 제출 시 클라이언트에서 비밀번호 확인 일치 여부를 먼저 검증한 후 호출.

---

## 투자일지 목록 페이지

**페이지 URL** `/journals` (journals.html)

---

### 투자일지 목록 로드

| 항목 | 내용 |
|---|---|
| **페이지명** | 투자일지 목록 페이지 |
| **URL** | `/api/journals` |
| **서비스명** | journal.js |
| **엔드포인트** | GET `/api/journals` |
| **요청방식** | GET |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**요청파라미터**

| 파라미터 | UI 연결 |
|---|---|
| page | 하단 페이지네이션 |
| size | 고정 20 |

**응답형식**

```json
{
  "content": [ { "id": 1, "userId": "...", "nickname": "...", "title": "...", "createdAt": "...", "commentCount": 3 } ],
  "page": 0, "totalElements": 42, "totalPages": 3
}
```

**설명** 페이지 로드 및 페이지 이동 시 호출. 제목·작성자·날짜·댓글 수 표시.

---

## 투자일지 상세 페이지

**페이지 URL** `/journals/{id}` (journal-detail.html)

---

### 게시글 + 댓글 로드

| 항목 | 내용 |
|---|---|
| **페이지명** | 투자일지 상세 페이지 |
| **URL** | `/api/journals/{id}` |
| **서비스명** | journal.js |
| **엔드포인트** | GET `/api/journals/{id}` |
| **요청방식** | GET |
| **요청파라미터** | Path: `id` (Long) |
| **요청헤더** | 없음 |
| **요청바디** | 없음 |

**응답형식**

```json
{
  "id": 1, "userId": "...", "nickname": "...", "title": "...", "content": "...",
  "createdAt": "...", "updatedAt": "...",
  "comments": [ { "id": 1, "userId": "...", "nickname": "...", "content": "...", "createdAt": "..." } ]
}
```

**설명** 페이지 로드 시 단일 호출로 본문 + 댓글 전체 렌더링.

---

### 댓글 작성

| 항목 | 내용 |
|---|---|
| **페이지명** | 투자일지 상세 페이지 |
| **URL** | `/api/journals/{id}/comments` |
| **서비스명** | journal.js |
| **엔드포인트** | POST `/api/journals/{id}/comments` |
| **요청방식** | POST |
| **요청파라미터** | Path: `id` (Long, 게시글 ID) |
| **요청헤더** | `Content-Type: application/json` · `Cookie: JSESSIONID={세션ID}` |

**요청바디**

```json
{ "content": "좋은 분석이네요." }
```

**응답형식** `201 Created` → 댓글 영역 즉시 갱신 (재호출 또는 DOM 추가)

**설명** 댓글 폼 제출 시 호출. 성공 후 댓글 목록을 다시 렌더링하거나 응답값을 DOM에 추가.

---

### 게시글 삭제

| 항목 | 내용 |
|---|---|
| **페이지명** | 투자일지 상세 페이지 |
| **URL** | `/api/journals/{id}` |
| **서비스명** | journal.js |
| **엔드포인트** | DELETE `/api/journals/{id}` |
| **요청방식** | DELETE |
| **요청파라미터** | Path: `id` (Long) |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식** `204 No Content` → 목록 페이지로 리디렉션

**설명** 본인 게시글에서만 삭제 버튼 노출. 삭제 성공 시 `/journals`로 이동.

---

## 마이페이지 (즐겨찾기)

**페이지 URL** `/mypage` (mypage.html)

---

### 즐겨찾기 목록 로드

| 항목 | 내용 |
|---|---|
| **페이지명** | 마이페이지 |
| **URL** | `/api/favorites` |
| **서비스명** | mypage.js |
| **엔드포인트** | GET `/api/favorites` |
| **요청방식** | GET |
| **요청파라미터** | 없음 |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식**

```json
[
  { "stockCode": "005930", "corpName": "삼성전자", "clpr": 75000, "fltRt": 0.67, "per": 12.34, "roe": 12.67, "addedAt": "..." }
]
```

**설명** 페이지 로드 시 호출. 즐겨찾기 종목 카드 목록 렌더링.

---

### 즐겨찾기 삭제

| 항목 | 내용 |
|---|---|
| **페이지명** | 마이페이지 |
| **URL** | `/api/favorites/{stockCode}` |
| **서비스명** | mypage.js |
| **엔드포인트** | DELETE `/api/favorites/{stockCode}` |
| **요청방식** | DELETE |
| **요청파라미터** | Path: `stockCode` (String) |
| **요청헤더** | `Cookie: JSESSIONID={세션ID}` |
| **요청바디** | 없음 |

**응답형식** `204 No Content` → 해당 종목 카드 DOM에서 즉시 제거

**설명** 카드 내 삭제 버튼 클릭 시 호출. 페이지 리로드 없이 DOM 직접 제거.
