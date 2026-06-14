# TASK-022: data.js에 백엔드 API fetch 함수 및 정규화 함수 추가

## 완료 시각
2026-06-14T00:00:00+09:00

## 작업 요약
`develope/front/js/data.js`에 백엔드 REST API 연동을 위한 상수, 정규화 함수, fetch 함수를 추가했다.
기존 MOCK_STOCKS 데이터와 유틸 함수는 변경 없이 유지하여 fallback 및 기존 페이지와의 호환성을 보장했다.

### 추가된 내용

| 항목 | 설명 |
|---|---|
| `API_BASE` | 빈 문자열 상수 — 배포 시 base URL 교체 가능 |
| `normalizeStock(s)` | StockListResponse → 프론트 stock 객체 변환 |
| `normalizeDetail(s, financials)` | StockDetailResponse + FinancialResponse[] → 상세 stock 객체 변환 (financials year 오름차순 정렬) |
| `fetchStocks(params)` | GET /api/stocks, URLSearchParams 직렬화, 배열 정규화 반환 |
| `fetchTop10(type)` | GET /api/stocks/top10?type={type}, stocks 배열 정규화 반환 |
| `fetchStockDetail(code)` | GET /api/stocks/{code}, 원본 StockDetailResponse 반환 |
| `fetchFinancials(code)` | GET /api/stocks/{code}/financials, 원본 FinancialResponse[] 반환 |
| `fetchStockFull(code)` | fetchStockDetail + fetchFinancials 병렬 호출 후 normalizeDetail 반환 |

## 생성/수정 파일 목록

| 파일 | 변경 유형 |
|---|---|
| `develope/front/js/data.js` | 수정 (API 상수·함수 추가, 기존 코드 유지) |
| `report/fragment/TASK-022_frontend_developer.md` | 생성 |

## 예상 토큰 소모량
소 (참고 파일 읽기 + 단일 파일 수정)
