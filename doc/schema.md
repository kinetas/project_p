# DB 스키마 설계

## 데이터 적재 순서

```
1. DART 회사목록 API
   → company 테이블 저장 (corp_code, stock_code, corp_name, corp_cls)

2. company.corp_code 기준으로 DART API 2종 호출
   2a. DART 재무제표 API (fnlttMultiAcnt)
       → financial_statement 테이블 저장

   2b. DART 배당 API (alotMatter)
       se='현금배당수익률(%)' && stock_knd='보통주' 행만 추출
       → dividend_info 테이블 저장

3. 공공데이터 주식시세 API 호출 (srtnCd)
   srtnCd == company.stock_code 로 매칭
   → stock_price 테이블 저장

4. financial_statement + stock_price 데이터가 모두 쌓인 후 지표 계산
   → stock_indicator 테이블 (calc_year 기준 UPSERT)
   계산 규칙: calc_year(현재연도) = bsns_year(전년도) 재무 + 최근 영업일 주가
   (배당수익률은 dividend_info에서 직접 조회하므로 stock_indicator 미포함)
```

**키 연결 관계**
- DART `corp_code` (8자리) → 재무제표·배당 조회 키
- DART `stock_code` (6자리) == 공공데이터 `srtnCd` (6자리) → 주식시세 매칭 키

---

## 테이블 목록

### 1. 시장 지표 테이블 (`market_index`)
코스피 지수, 코스닥 지수, 달러-원화 환율을 날짜별로 저장. 회사 데이터와 무관한 시장 전체 지표.

| 컬럼 | 설명 |
|------|------|
| base_dt | 기준일자 |
| kospi_close | 코스피 종가 지수 |
| kospi_vs | 코스피 전일 대비 등락 |
| kosdaq_close | 코스닥 종가 지수 |
| kosdaq_vs | 코스닥 전일 대비 등락 |
| usd_krw_rate | 달러-원화 환율 |
| usd_krw_vs | 환율 전일 대비 등락 |

---

### 2. 회사 테이블 (`company`) — DART API 기준
적재 순서 1단계. corp_code가 이후 모든 테이블의 시작점.

| 컬럼 | 설명 |
|------|------|
| corp_code | DART 고유번호 (8자리) — 재무제표·배당 조회 키 |
| stock_code | 종목코드 (6자리) — 주식시세 매칭 키 (srtnCd와 동일) |
| corp_name | 종목명(법인명) |
| corp_cls | 법인구분 (Y: 유가, K: 코스닥, N: 코넥스, E: 기타) |

---

### 3. 재무제표 테이블 (`financial_statement`) — DART `fnlttMultiAcnt` API 기준
적재 순서 2a단계. company.corp_code로 호출. API 응답 그대로 저장.

| 컬럼 | 설명 |
|------|------|
| bsns_year | 사업연도 (4자리) |
| stock_code | 종목코드 (6자리) — company.stock_code 참조 |
| reprt_code | 보고서 코드 (11013: 1분기, 11012: 반기, 11014: 3분기, 11011: 사업보고서) |
| account_nm | 계정명 (ex. 자본총계) |
| fs_div | 개별/연결구분 (OFS: 재무제표, CFS: 연결재무제표) |
| fs_nm | 개별/연결명 (ex. 연결재무제표) |
| sj_div | 재무제표구분 (BS: 재무상태표, IS: 손익계산서) |
| sj_nm | 재무제표명 |
| thstrm_nm | 당기명 |
| thstrm_dt | 당기일자 |
| thstrm_amount | 당기금액 |
| thstrm_add_amount | 당기누적금액 |
| frmtrm_nm | 전기명 |
| frmtrm_dt | 전기일자 |
| frmtrm_amount | 전기금액 |
| frmtrm_add_amount | 전기누적금액 |
| bfefrmtrm_nm | 전전기명 (사업보고서만) |
| bfefrmtrm_dt | 전전기일자 (사업보고서만) |
| bfefrmtrm_amount | 전전기금액 (사업보고서만) |
| ord | 계정과목 정렬순서 |
| currency | 통화단위 |

---

### 4. 배당 정보 테이블 (`dividend_info`) — DART `alotMatter` API 기준
적재 순서 2b단계. company.corp_code로 호출. API 응답 중 `se='주당 현금배당금(원)' && stock_knd='보통주'` 행만 추출하여 저장.
`stock_indicator.dividend_yield` 계산 시 `div_per_share_thstrm`(전년도) ÷ 현재 주가 × 100 으로 사용.

| 컬럼 | 설명 |
|------|------|
| corp_code | DART 고유번호 (8자리) |
| stock_code | 종목코드 (6자리) — company.stock_code 참조 |
| bsns_year | 사업연도 (4자리) |
| reprt_code | 보고서 코드 (11013/11012/11014/11011) |
| stlm_dt | 결산기준일 (YYYY-MM-DD) |
| div_per_share_thstrm | 당기 주당 현금배당금 (원) — 보통주 기준 |
| div_per_share_frmtrm | 전기 주당 현금배당금 (원) |
| div_per_share_lwfr | 전전기 주당 현금배당금 (원) |
| rcept_no | DART 접수번호 (14자리) |

---

### 5. 주식시세 테이블 (`stock_price`) — 공공데이터(금융위원회) 기준
적재 순서 3단계. srtnCd == company.stock_code 로 매칭하여 저장.
현재는 코스피만 수집하나 향후 코스닥 대응을 위해 `mrktCtg` 컬럼 유지.

| 컬럼 | 설명 |
|------|------|
| basDt | 기준일자 |
| srtnCd | 단축종목코드 (6자리) — company.stock_code와 동일 |
| isinCd | ISIN 코드 (국제채권식별번호) |
| itmsNm | 종목명 |
| mrktCtg | 시장구분 (KOSPI / KOSDAQ / KONEX) |
| clpr | 종가 |
| vs | 전일 대비 등락 |
| fltRt | 등락률 |
| mkp | 시가 |
| hipr | 고가 |
| lopr | 저가 |
| trqu | 거래량 |
| trPrc | 거래대금 |
| lstgStCnt | 상장주식수 |
| mrktTotAmt | 시가총액 |

---

### 6. 지표 계산 테이블 (`stock_indicator`)
적재 순서 4단계. financial_statement + stock_price + dividend_info 기반으로 계산.
**calc_year(현재연도) 기준 1행 UPSERT** — 26년이면 25년 재무제표 + 26년 최근 영업일 주가 사용.
`dividend_yield` = `dividend_info.div_per_share_thstrm`(전년도) ÷ `stock_price.clpr`(현재) × 100 으로 계산.

| 컬럼 | 설명 |
|------|------|
| stock_code | 종목코드 |
| calc_year | 계산 기준연도 (현재연도, e.g. 2026) — PK |
| bsns_year | 계산에 사용된 재무제표 사업연도 (calc_year - 1) |
| reprt_code | 계산에 사용된 보고서 코드 |
| base_dt | 계산에 사용된 주가 기준일자 (stock_price.basDt) |
| eps | 주당순이익 = 당기순이익 / 상장주식수 |
| bps | 주당순자산 = 자본총계 / 상장주식수 |
| per | 주가수익비율 = 현재가 / EPS |
| pbr | 주가순자산비율 = 현재가 / BPS |
| roe | 자기자본이익률 = 당기순이익 / 자본총계 × 100 |
| operating_margin | 영업이익률 = 영업이익 / 매출액 × 100 |
| debt_ratio | 부채비율 = 부채총계 / 자본총계 × 100 |
| dividend_yield | 배당수익률 = 전년도 주당 현금배당금 / 현재 주가 × 100 |
| calculated_at | 계산일시 |

---

### 7. 유저 테이블 (`user`)
| 컬럼 | 설명 |
|------|------|
| user_id | 유저 고유 ID |
| (추가 컬럼은 인증 방식 확정 후 정의) | |

---

### 8. 유저 즐겨찾기 테이블 (`user_favorite`)
| 컬럼 | 설명 |
|------|------|
| user_id | 유저 ID |
| stock_code | 즐겨찾기 종목코드 |
| created_at | 등록일시 |

---

### 9. 시가총액 상위 100 스냅샷 테이블 (`top100`)
KRX 시가총액 기준 상위 100개 종목을 주기적으로 스냅샷 저장. 메인 화면 종목 리스트 및 랭킹 표시에 사용.

| 컬럼 | 설명 |
|------|------|
| base_dt | 기준일자 |
| rank | 순위 (1~100) |
| corp_code | DART 고유번호 — company.corp_code 참조 |
| stock_code | 종목코드 — company.stock_code 참조 |
| bsns_year | 지표 계산에 사용된 사업연도 |

---

### 10. 투자일지 테이블 (`investment_journal`)
유저가 종목별로 매수/매도 근거, 투자 메모 등을 기록하는 일지. user_id + 작성일시 기준으로 식별.

| 컬럼 | 설명 |
|------|------|
| journal_id | 일지 고유 ID (PK) |
| user_id | 작성자 유저 ID — user.user_id 참조 |
| stock_code | 관련 종목코드 — company.stock_code 참조 |
| title | 일지 제목 |
| content | 일지 본문 |
| created_at | 작성일시 |
| updated_at | 최종 수정일시 |

---

### 11. 댓글 테이블 (`comment`)
투자일지에 달리는 댓글. 유저가 타인 또는 자신의 일지에 의견을 남김.

| 컬럼 | 설명 |
|------|------|
| comment_id | 댓글 고유 ID (PK) |
| journal_id | 대상 일지 ID — investment_journal.journal_id 참조 |
| user_id | 작성자 유저 ID — user.user_id 참조 |
| content | 댓글 내용 |
| created_at | 작성일시 |

---

## 뷰 (선택적 추가)

### `v_stock_summary`
메인 화면, 종목 리스트, 상세 화면 모두 아래 4개 테이블을 항상 조인해서 사용하므로 뷰로 묶으면 백엔드 쿼리 단순화 가능.

- `company` (corp_name)
- `stock_price` (clpr, fltRt, mrktTotAmt 등)
- `stock_indicator` (per, pbr, roe 등)
- `dividend_info` (div_yield_thstrm)

---

## 미결 사항 / 추후 검토

- **업종/섹터 테이블** (`sector`): KRX 업종 분류 기반 종목 필터링·비교 기능에 필요 — 추가 여부 미결
- **가격 알림 테이블** (`price_alert`): 유저별 목표가 알림 기능 구현 시 필요 — 기능 기획 후 결정
- **top100 기준 확정**: 시가총액 외 PER·거래량 등 다른 기준 병행 여부, 갱신 주기(일별/주별) 미결
- **투자일지 공개 범위**: 일지를 전체 공개·팔로워 공개·비공개로 구분할지 여부 — 컬럼(`visibility`) 추가 필요 시 반영
- **댓글 계층 구조**: 대댓글(대댓글 depth) 지원 여부 — 필요 시 `parent_comment_id` 컬럼 추가
- **user 테이블 컬럼**: 소셜 로그인(OAuth) vs 자체 회원가입 방식 확정 후 정의
- 코스닥 데이터 수집은 현재 제외, 향후 `mrktCtg` 컬럼 기반으로 확장 예정
