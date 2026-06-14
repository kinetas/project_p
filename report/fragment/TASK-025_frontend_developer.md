# TASK-025: detail.js — 백엔드 API 연동

## 작업 개요
`develope/front/js/detail.js`를 수정하여 목업 데이터(`getStockByCode`) 대신 백엔드 API(`fetchStockFull`)를 사용하도록 변경하였다.

## 변경 사항

### 1. DOMContentLoaded 핸들러 async 변경
```js
// 변경 전
document.addEventListener('DOMContentLoaded', () => {
// 변경 후
document.addEventListener('DOMContentLoaded', async () => {
```

### 2. 초기 로딩 — fetchStockFull 호출 및 로딩 UI 추가
- DOMContentLoaded 진입 시 로딩 메시지(`데이터를 불러오는 중입니다...`)를 `detailMain`에 표시
- `getStockByCode(code)` 제거, `await fetchStockFull(code)` 호출로 교체
- `try-catch`로 네트워크/서버 오류 처리, 실패 시 `stock = null`로 설정

### 3. yearLabels — stock.years 우선 사용
```js
// 변경 전
const yearLabels = YEARS.map((y) => y + '년');
// 변경 후
const yearLabels = (stock.years || YEARS).map((y) => y + '년');
```
차트(`drawLineChart`, `drawBarChart`) 및 `window.resize` 핸들러에서도 동일하게 `yearLabels` 사용 (기존 코드 그대로 유지, yearLabels가 이미 올바른 값을 가짐).

### 4. renderTable 함수 — years 파라미터 추가
```js
// 변경 전
function renderTable(title, rows) {
  const headerCells = YEARS.map((y) => `<th>${y}</th>`).join('');
// 변경 후
function renderTable(title, rows, years) {
  const headerCells = years.map((y) => `<th>${y}</th>`).join('');
```
- 함수 내부의 `YEARS` 참조를 `years` 파라미터로 교체
- 모든 호출부에 세 번째 인자 `stock.years || YEARS` 추가 (손익계산서, 재무상태표, 투자지표 탭 3곳)

### 5. 오류 처리 — not-found UI 유지
- `fetchStockFull` 실패(예외) 또는 반환값이 null인 경우 기존 not-found UI를 그대로 표시
- 종목 코드 없음(`code || '없음'`) 처리 로직 동일 유지

### 6. stock 필드 매핑 확인
- `stock.listedDate` → `normalizeDetail`에서 `s.listingDate`를 `listedDate`로 매핑 완료 (TASK-022)
- `stock.ceo` → `normalizeDetail`에서 `s.ceoName`을 `ceo`로 매핑 완료
- `stock.operatingProfit`, `per`, `pbr`, `roe`, `eps`, `bps`, `debtRatio`, `operatingMargin`, `marketCap`, `price` 등 모든 필드 정상 참조

## 수정 파일
- `develope/front/js/detail.js` — 단일 파일 수정

## 결과
- 백엔드 API(`GET /api/stocks/{code}` + `GET /api/stocks/{code}/financials`) 병렬 호출 후 정규화된 데이터를 화면에 렌더링
- 재무 연도(years)가 백엔드 응답 기준으로 동적 결정됨 (없을 경우 전역 상수 `YEARS` 폴백)
- 로딩 상태 표시 및 오류 시 not-found UI 정상 동작
