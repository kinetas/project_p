# TASK-023: home.js — 백엔드 API 연동 완료 보고서

## 작업 개요
- **태스크**: TASK-023
- **담당**: Frontend Developer Sub AI
- **대상 파일**: `develope/front/js/home.js`
- **작업 일자**: 2026-06-14

---

## 작업 내용

### 1. DOMContentLoaded 핸들러 async 변경
```js
document.addEventListener('DOMContentLoaded', async () => { ... });
```

### 2. 시장 지표 섹션 (marketGrid)
- 백엔드 미지원 → `MARKET_INDICES` 그대로 유지

### 3. 주요 종목 섹션 (stocksGrid)
- `MOCK_STOCKS` 제거 → `fetchStocks({ size: 8 })` API 호출로 교체
- 로딩 중 표시: `stocksGrid.innerHTML = '<div>로딩 중...</div>'`
- API 응답으로 `renderStockCard` 렌더링 후 `bindStockCards` 바인딩
- 실패 시 `MOCK_STOCKS` fallback 사용

### 4. 랭킹 섹션
- `Promise.all` 로 4개 랭킹 병렬 fetch:
  - 저PER: `fetchTop10("lowPer")`
  - 저PBR: `fetchTop10("lowPbr")`
  - 고ROE: `fetchTop10("highRoe")`
  - 배당수익률: `fetchTop10("value")` (dividendYield 데이터 없음)
- `getRanking` 정렬 생략: API 응답이 이미 정렬된 데이터이므로 `buildRankingItems` 헬퍼 함수로 직접 HTML 생성
- `buildRankingItems` 구조는 기존 `renderRankingList` 내부 로직과 동일 (rank-num, rank-name, rank-code, rank-value, stock-change)
- 각 랭킹 섹션 로딩 중 표시 후 결과 렌더링, `bindStockCards` 바인딩
- 실패 시 `renderRankingList(MOCK_STOCKS, key)` fallback 사용

### 5. 오류 처리
- 주요 종목: `try-catch` 로 감싸고 실패 시 `MOCK_STOCKS` fallback
- 랭킹: `try-catch` 로 감싸고 실패 시 `renderRankingList(MOCK_STOCKS, key)` fallback

---

## 결과
- `develope/front/js/home.js` 수정 완료
- MOCK_STOCKS 의존 제거, API 연동 구조로 전환
- 로딩 상태 표시 및 fallback 처리 포함
