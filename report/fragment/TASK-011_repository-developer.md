# TASK-011 완료 보고 — Repository Developer

## 태스크
FinancialRepository 작성

## 결과
- 파일 생성: `E:\pp\develope\backend\src\main\java\com\example\demo\repository\FinancialRepository.java`
- JpaRepository<FinancialEntity, Long> 상속
- findByStockCodeOrderByYearDesc(String stockCode) — 종목별 전체 연도 최신 순
- findTop5ByStockCodeOrderByYearDesc(String stockCode) — 최근 5년
- findByStockCodeAndYear(String stockCode, Integer year) — Optional 반환

## 이슈
없음
