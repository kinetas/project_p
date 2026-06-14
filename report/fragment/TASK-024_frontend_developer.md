# TASK-024: list.js — 백엔드 API 연동

## 작업 요약

`develope/front/js/list.js`를 수정하여 MOCK_STOCKS 직접 참조 대신 `fetchStocks` API를 사용하도록 변경하였습니다.

## 변경 사항

### 1. DOMContentLoaded 핸들러 async 변경
- `document.addEventListener('DOMContentLoaded', () => {` → `async () => {`
- 초기 API 로드를 위한 `await` 사용 가능하도록 설정

### 2. 상태 변수 변경
- `let stocks = [...MOCK_STOCKS]` → `let stocks = []`
- 초기 검색어 처리: URL에서 `searchQuery` 추출 후 DOM 반영만 수행 (필터링은 API 호출 시)

### 3. 초기 로딩 함수 `loadStocks()` 추가
- 로딩 중: `tableBody`에 "종목 데이터를 불러오는 중..." 메시지 표시
- 검색어 있을 시: `await fetchStocks({ keyword: searchQuery, size: 200 })`
- 없을 시: `await fetchStocks({ size: 200 })`
- 결과를 `stocks` 변수에 저장
- 오류 발생 시: try-catch로 잡아 `MOCK_STOCKS` fallback 적용 (검색어 필터 포함)

### 4. `applyFilters()` 함수 수정
- `let result = [...MOCK_STOCKS]` → `let result = [...stocks]`
- 기존 로컬 searchQuery 필터 로직 제거 (API 호출 시 처리됨)
- per/pbr/roe/dividendYield/marketCap 범위 필터 및 정렬 로직 유지

### 5. `render()` 함수 수정
- `stocks = applyFilters()` → `const filtered = applyFilters()`
- 외부 `stocks` 변수를 필터 결과로 덮어쓰지 않도록 수정 (API 원본 데이터 보존)

### 6. 초기 실행 순서
- `await loadStocks()` → `render()` 순서로 실행

## 파일 경로
- 수정: `develope/front/js/list.js`

## 완료 일자
2026-06-14
