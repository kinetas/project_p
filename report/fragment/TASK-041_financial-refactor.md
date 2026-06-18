# TASK-041 Financial Statement Refactor 완료 보고

## 작업 내용
- FinancialStatementEntity.java: frmtrm_*/bfefrmtrm_* 필드 제거 (7개 필드 삭제)
- DartCollectorService.java: 1 row → 3 entity 변환 로직 구현 (당기/전기/전전기 분리 저장)
- schema.sql (2개): frmtrm_*/bfefrmtrm_* 컬럼 제거 (7개 컬럼 삭제)

## 변경 파일
- develope/backend/src/main/java/com/example/demo/entity/FinancialStatementEntity.java
- develope/backend/src/main/java/com/example/demo/service/DartCollectorService.java
- develope/backend/src/main/resources/schema.sql
- doc/schema.sql

## 주요 변경 사항
- FinancialStatementEntity: frmtrmNm, frmtrmDt, frmtrmAmount, frmtrmAddAmount, bfefrmtrmNm, bfefrmtrmDt, bfefrmtrmAmount 7개 필드 제거
- DartCollectorService.collectFinancials(): API row 1개 → 당기(apiYear)/전기(apiYear-1)/전전기(apiYear-2) 3개 entity 변환
  - 전기/전전기는 amount가 null인 경우 행 생성 스킵
  - 삭제 범위 확장: year, year-1, year-2 기존 데이터 모두 삭제 후 재저장
  - buildEntity() private 헬퍼 메서드 추출
- schema.sql: financial_statement 테이블에서 frmtrm_nm, frmtrm_dt, frmtrm_amount, frmtrm_add_amount, bfefrmtrm_nm, bfefrmtrm_dt, bfefrmtrm_amount 7개 컬럼 제거

## 완료
