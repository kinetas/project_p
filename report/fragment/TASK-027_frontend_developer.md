# TASK-027 완료 보고서 — Frontend Developer

## 완료한 작업 목록

1. `data.js`에 `fetchMarketIndices()` 함수 추가
2. `home.js` 시장 지표 섹션을 하드코딩 → API 연동으로 교체 (fallback 포함)

## 수정한 파일 경로

- `E:\pp\develope\front\js\data.js`
- `E:\pp\develope\front\js\home.js`

## 주요 변경사항

### data.js
- 기존 `fetchStockFull()` 함수 아래에 `fetchMarketIndices()` 함수 추가
- `GET /api/market/indices` 엔드포인트 호출
- 응답 형식: `Array<{ name, value, changeRate, changeAmount }>`
- 실패 시 `Error` throw (호출부에서 catch 처리)

### home.js
- 기존 `MARKET_INDICES` 하드코딩 렌더링 블록 제거
- `fetchMarketIndices()` API 호출로 교체
- 로딩 중 상태 표시 (`'<div>로딩 중...</div>'`)
- API 실패 시 `MARKET_INDICES` fallback 사용 (기존 하드코딩 데이터 유지)
- 에러 로그: `console.error('fetchMarketIndices 실패, fallback 사용:', err)`

## 비고

- 백엔드 `GET /api/market/indices` 엔드포인트 준비 전까지 fallback으로 기존 하드코딩 데이터 정상 렌더링
- Spring Boot 서빙 경로: `resources/static/` (develope/front/ 파일은 별도 배포 필요)
