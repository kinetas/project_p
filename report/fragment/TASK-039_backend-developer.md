# TASK-039 backend-developer 작업 보고서

## 완료 시각
2026-06-18

## 작업 요약

기존 StockEntity/FinancialEntity 기반의 단일 엔티티 패턴을 실제 DB 엔티티 구조(CompanyEntity, StockPriceEntity, StockIndicatorEntity, FinancialStatementEntity)에 맞게 전면 리팩토링.

### 주요 변경 사항

1. **StockService.java** — 전면 수정
   - 의존성 교체: `StockRepository`, `FinancialRepository` → `CompanyRepository`, `StockPriceRepository`, `StockIndicatorRepository`, `FinancialStatementRepository`
   - `getStockList(StockSearchRequest)`: `CompanyRepository.findAll()` 기반으로 각 company별 최신 StockPrice, Indicator 조회 후 `StockListResponse.from()` 생성. 필터링은 DTO 레벨에서 처리.
   - `getStockDetail(String)`: 3개 레포지토리 조회 후 `StockDetailResponse.from()` 반환.
   - `getTop10(String)`: `StockIndicatorRepository.findAll()` 후 type별 정렬(lowPer/highRoe/lowPbr/value) → 상위 10개 추출 → CompanyEntity/StockPriceEntity 조회하여 `Top10Response` 생성.
   - `getFinancials(String)`: `findDistinctBsnsYearsByStockCodeOrderByDesc()` → 최근 5년 추출 → 각 연도별 `findByStockCodeAndBsnsYear()` rows에서 account_nm 기준 매출액/영업이익/당기순이익 집계.

2. **StockListResponse.java** — 수정
   - `from(StockEntity)` 제거, `from(CompanyEntity, StockPriceEntity, StockIndicatorEntity)` 추가
   - 필드: stockCode, corpName, mrktCtg, clpr, vs, fltRt, mrktTotAmt, per, pbr, roe, debtRatio, dividendYield
   - null 안전 처리: `stockPrice != null ? ... : null`, `indicator != null ? ... : null`

3. **StockDetailResponse.java** — 수정
   - `from(StockEntity)` 제거, `from(CompanyEntity, StockPriceEntity, StockIndicatorEntity)` 추가
   - 필드: company 기본정보 + StockPriceEntity 전 시세 필드 + StockIndicatorEntity 투자지표 전 필드
   - null 안전 처리 적용

4. **FinancialResponse.java** — 수정
   - `from(FinancialEntity)` 제거, `FinancialEntity` import 제거
   - 필드 축소: year(String), revenue(Long), operatingProfit(Long), netIncome(Long)
   - 빌더 패턴 유지

5. **FinancialController.java** — 변경 없음
   - 기존 코드가 이미 `stockService.getFinancials(code)` 호출 → `List<FinancialResponse>` 반환 구조로 올바르게 구성됨

## 수정한 파일 목록

| 파일 | 변경 유형 |
|------|----------|
| `develope/backend/src/main/java/com/example/demo/service/StockService.java` | 전면 수정 |
| `develope/backend/src/main/java/com/example/demo/dto/stock/StockListResponse.java` | 수정 |
| `develope/backend/src/main/java/com/example/demo/dto/stock/StockDetailResponse.java` | 수정 |
| `develope/backend/src/main/java/com/example/demo/dto/stock/FinancialResponse.java` | 수정 |

## 예상 토큰 소모량

- 파일 읽기 (14개 파일): ~4,000 tokens
- 파일 수정 (4개 파일): ~3,500 tokens
- 합계: 약 7,500 tokens
