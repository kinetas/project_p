# TASK-044 완료 보고 — DTO 정리 + 시가총액 TOP10 엔드포인트

## 변경된 파일 목록

1. `develope/backend/src/main/java/com/example/demo/dto/stock/StockDetailResponse.java`
2. `develope/backend/src/main/java/com/example/demo/service/StockService.java`
3. `develope/backend/src/main/java/com/example/demo/controller/StockController.java`
4. `develope/front/js/data.js`
5. `develope/front/js/home.js`

## 제거된 필드 (StockDetailResponse.java)

- `mkp` (시가)
- `hipr` (고가)
- `lopr` (저가)
- `trqu` (거래량)
- `trPrc` (거래대금)
- `isinCd` (ISIN 코드)
- `corpCls` (법인구분)

`from()` 정적 메서드에서도 동일 필드 제거 완료.

## 추가된 엔드포인트

- `GET /api/stocks/featured` — 시가총액 TOP10 종목 반환
  - `StockService.getFeaturedStocks()`: 전체 종목을 mrktTotAmt 기준 내림차순 정렬 후 상위 10개 반환
  - `StockController.getFeaturedStocks()`: `ResponseEntity<List<StockListResponse>>` 반환

## 프론트 변경사항

- `data.js`: `fetchFeaturedStocks()` 함수 추가 (`GET /api/stocks/featured` 호출 후 `normalizeStock` 매핑)
- `home.js`: "주요 종목" 섹션에서 `fetchStocks({ size: 8 })` 호출을 `fetchFeaturedStocks()`로 교체

## 비고

- `StockService.java`에는 `Comparator`, `Optional`, `Collectors` import가 이미 존재하여 추가 import 불필요
- `StockController.java`에는 `List` import가 이미 존재하여 추가 import 불필요
