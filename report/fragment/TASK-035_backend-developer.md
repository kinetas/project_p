# TASK-035 Backend Developer 완료 보고서

## 완료 시각
2026-06-18

## 작업 요약
기존 단일 테이블 구조(StockEntity, FinancialEntity 등)를 삭제하고, 새 스키마에 맞는 Entity/Repository를 생성하였다.
DividendEntity는 StockEntity 참조 없이 @EmbeddedId(DividendId)만 사용 중이어서 수정 불필요.

## 생성한 파일 (10개)

### Entity
- `entity/CompanyEntity.java` — company 테이블 (PK: corp_code, unique: stock_code, isinCd)
- `entity/StockPriceId.java` — stock_price 복합 PK (basDt, srtnCd)
- `entity/StockPriceEntity.java` — stock_price 테이블 (EmbeddedId: StockPriceId)
- `entity/FinancialStatementEntity.java` — financial_statement 테이블 (PK: id auto, unique constraint: uq_fs)
- `entity/StockIndicatorId.java` — stock_indicator 복합 PK (stock_code, calc_year)
- `entity/StockIndicatorEntity.java` — stock_indicator 테이블 (EmbeddedId: StockIndicatorId)

### Repository
- `repository/CompanyRepository.java` — findByStockCode
- `repository/StockPriceRepository.java` — findTopByIdSrtnCdOrderByIdBasDtDesc
- `repository/FinancialStatementRepository.java` — 4개 쿼리 메서드 (JPQL 포함)
- `repository/StockIndicatorRepository.java` — findByIdStockCode, findTopByIdStockCodeOrderByIdCalcYearDesc

## 삭제한 파일 (4개)
- `entity/StockEntity.java` — 구 stocks 테이블 Entity 제거
- `entity/FinancialEntity.java` — 구 financials 테이블 Entity 제거
- `repository/StockRepository.java` — StockEntity 기반 Repository 제거
- `repository/FinancialRepository.java` — FinancialEntity 기반 Repository 제거

## 수정한 파일
- `entity/DividendEntity.java` — 수정 불필요 (StockEntity 참조 없음, @EmbeddedId DividendId 구조 유지)

## 예상 토큰 소모량
- 입력: 약 6,000 tokens
- 출력: 약 4,500 tokens
- 합계: 약 10,500 tokens
