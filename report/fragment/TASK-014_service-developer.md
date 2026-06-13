# TASK-014 완료 보고 — Service Developer

## 태스크
IndicatorCalculationService 작성

## 결과
- 파일 생성: `E:\pp\develope\backend\src\main\java\com\example\demo\service\IndicatorCalculationService.java`
- calculateAndSave(stockCode):
  - 최근 재무 데이터 기준 StockEntity 지표 갱신 (updatedAt 포함)
  - 연도별 FinancialEntity 지표 갱신 (최근 5년)
- 지표 계산 (분모 0/null 시 null 반환):
  - EPS = 순이익 ÷ 발행주식수 (Long)
  - BPS = 자본 ÷ 발행주식수 (Long)
  - PER = 현재가 ÷ EPS (Double)
  - PBR = 현재가 ÷ BPS (Double)
  - ROE = 순이익 ÷ 자본 (Double)
  - 영업이익률 = 영업이익 ÷ 매출액 (Double)
  - 부채비율 = 부채 ÷ 자본 (Double)

## 이슈
없음
