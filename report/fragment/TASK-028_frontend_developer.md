# TASK-028: list.js 검색 기능 확인 및 보완

## 완료한 작업 목록

1. 관련 파일 분석
   - `develope/front/js/list.js` — 기존 구현 검토
   - `develope/backend/src/main/java/com/example/demo/dto/stock/StockSearchRequest.java` — 백엔드 지원 파라미터 확인
   - `develope/front/list.html` — 필터 패널 UI 확인
   - `develope/front/js/common.js` — headerSearch Enter 시 list.html?q=keyword 리다이렉트 확인
   - `develope/front/js/data.js` — fetchStocks() API 레이어 확인

2. 검색 기능 흐름 검증 (이상 없음)
   - common.js의 headerSearch가 Enter 시 `list.html?q=keyword`로 이동하는 것 확인
   - list.js가 URL params에서 `q` 파라미터를 읽어 `fetchStocks({ keyword: searchQuery })` 호출 확인
   - `await loadStocks(); render();` 순서로 올바르게 구현되어 있음을 확인

3. loadStocks() 함수 개선
   - `market` 파라미터를 인자로 받도록 시그니처 변경: `async function loadStocks({ market } = {})`
   - size를 200에서 1000으로 확대
   - market 파라미터가 있으면 API 호출 시 전달
   - API 실패 시 MOCK 데이터 폴백에도 market 필터 적용 추가

4. applyFilter 이벤트 핸들러 개선
   - 핸들러를 `async`로 변경
   - `marketFilter` UI 요소(id="marketFilter") 값 읽기 추가 (추후 UI 추가 시 즉시 활성화)
   - 필터 적용 시 `await loadStocks({ market: marketFilter || undefined })` API 재호출 후 render()

5. resetFilter 이벤트 핸들러 개선
   - 핸들러를 `async`로 변경
   - marketFilter UI 요소 값 초기화 추가
   - `await loadStocks()` 재호출 후 render() 처리

## 수정한 파일 경로

- `E:\pp\develope\front\js\list.js`

## 주요 변경사항

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| loadStocks 시그니처 | `async function loadStocks()` | `async function loadStocks({ market } = {})` |
| API size | 200 | 1000 |
| market 파라미터 | API에 전달 안 됨 | market 값이 있으면 API 파라미터로 전달 |
| applyFilter | 동기 핸들러, render()만 호출 | async, loadStocks() 재호출 후 render() |
| resetFilter | 동기 핸들러, render()만 호출 | async, loadStocks() 재호출 후 render() |
| MOCK 폴백 | market 필터 미적용 | market 필터 클라이언트 사이드 적용 추가 |

## 발견된 이슈 및 참고사항

1. **클라이언트 사이드 필터 설계 확인**: `applyFilters()` 내 pbr, div, cap 필터는 클라이언트 사이드 처리가 맞음. 백엔드 `StockSearchRequest`가 minPbr/maxPbr, dividendYield, marketCap 필터를 지원하지 않으므로 현재 구현이 적절함.

2. **market UI 요소 부재**: 현재 `list.html`의 필터 패널에 market 선택 UI(id="marketFilter")가 없음. list.js에서 `document.getElementById('marketFilter')`로 방어적으로 읽도록 구현했으므로, 추후 HTML에 market 선택 요소 추가 시 즉시 동작함.

3. **StockSearchRequest 백엔드 지원 파라미터**: keyword, market, minPer, maxPer, minRoe, maxDebtRatio, page, size — 이 범위 내에서만 API 파라미터를 전달하도록 구현함.
