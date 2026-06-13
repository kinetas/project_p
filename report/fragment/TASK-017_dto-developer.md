# TASK-017 완료 보고 — DTO Developer

## 태스크
Stock/Financial DTO 작성

## 결과
디렉토리: `E:\pp\develope\backend\src\main\java\com\example\demo\dto\stock\`

- StockListResponse.java — 목록 페이지용, from(StockEntity) 정적 팩토리 포함
- StockDetailResponse.java — 상세 페이지 상단/중단용, from(StockEntity) 정적 팩토리 포함
- FinancialResponse.java — 재무제표 5년 탭용, from(FinancialEntity) 정적 팩토리 포함
- StockSearchRequest.java — 검색/필터용, @Getter @NoArgsConstructor, 기본값 page=0/size=20
- Top10Response.java — TOP10 탭용, type + List<StockListResponse>

## 이슈
없음
