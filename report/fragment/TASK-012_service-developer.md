# TASK-012 완료 보고 — Service Developer

## 태스크
KrxCollectorService 작성

## 결과
- 파일 생성: `E:\pp\develope\backend\src\main\java\com\example\demo\service\KrxCollectorService.java`
- 파일 생성: `E:\pp\develope\backend\src\main\java\com\example\demo\config\RestTemplateConfig.java`
- @Value("${krx.api.key}") 주입
- collectStockList(): KOSPI(STK)/KOSDAQ(KSQ) 시장 순차 수집
- collectByMarket(): KRX 정보데이터시스템 API 호출, RestTemplate 사용
  - 종목코드, 종목명, 현재가, 시가총액, 발행주식수 파싱
  - 기존 종목 upsert (존재 시 가격/시총/주식수만 갱신, 기타 필드 유지)
  - API 실패 시 log.error 후 graceful fallback (메서드 종료)
- RestTemplateConfig: @Bean RestTemplate 등록

## 이슈
없음
