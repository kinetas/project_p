# TASK-010 완료 보고 — Repository Developer

## 태스크
StockRepository 작성

## 결과
- 파일 생성: `E:\pp\develope\backend\src\main\java\com\example\demo\repository\StockRepository.java`
- JpaRepository<StockEntity, Long> 상속
- findByStockCode(String stockCode) — Optional 반환
- findTop10ByPerIsNotNullOrderByPerAsc() — 저PER TOP10 (null 제외)
- findTop10ByRoeIsNotNullOrderByRoeDesc() — 고ROE TOP10 (null 제외)
- findByStockNameContainingOrStockCodeContaining(String, String, Pageable) — 검색
- findTop10ValueStocks(Pageable) — @Query 가치주 TOP10 (PER/PBR ASC, ROE DESC, debtRatio ASC 복합 정렬)

## 이슈
없음
