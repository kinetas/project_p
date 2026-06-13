# TASK-013 완료 보고 — Service Developer

## 태스크
DartCollectorService 작성

## 결과
- 파일 생성: `E:\pp\develope\backend\src\main\java\com\example\demo\service\DartCollectorService.java`
- @Value("${dart.api.key}") 주입
- fetchCorpCodes():
  - DART API corpCode.xml zip 다운로드
  - ZipInputStream + DOM 파싱으로 stockCode → dartCorpCode 매핑
  - 전체 StockEntity 일괄 업데이트
  - 실패 시 log.error 후 graceful fallback
- collectFinancials(dartCorpCode, stockCode, year):
  - fnlttSinglAcntAll API 호출 (사업보고서 CFS 기준)
  - 매출액/영업이익/순이익/자산/부채/자본 파싱 (account_id + account_nm 이중 매칭)
  - FinancialEntity upsert
  - 실패 시 log.warn/error 후 graceful fallback

## 이슈
없음
