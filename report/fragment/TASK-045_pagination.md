# TASK-045 종목 리스트 페이징 — 완료 보고

## 변경된 파일 목록

| 파일 | 변경 내용 |
|------|-----------|
| `develope/backend/src/main/java/com/example/demo/dto/stock/PageResponse.java` | 신규 생성 |
| `develope/backend/src/main/java/com/example/demo/service/StockService.java` | getStockList() 반환 타입 변경, 페이징 로직 추가, Collections import 추가 |
| `develope/backend/src/main/java/com/example/demo/controller/StockController.java` | getStockList() 반환 타입 PageResponse<StockListResponse>로 변경, PageResponse import 추가 |
| `develope/front/js/data.js` | fetchStocks() 반환 구조 변경 (배열 → { stocks, totalCount, totalPages, page }) |
| `develope/front/js/list.js` | currentPage/PAGE_SIZE 변수 추가, loadStocks() 페이징 파라미터 적용, renderPagination() 추가, render()에서 renderPagination 호출 |
| `develope/front/list.html` | </main> 직전에 pagination div 추가 |
| `develope/front/css/list.css` | .pagination, .page-btn, .page-btn.active 스타일 추가 |

## PageResponse DTO 생성 여부

완료. `PageResponse<T>` 제네릭 DTO 생성:
- `data`: 페이지 데이터 목록
- `totalCount`: 전체 항목 수
- `page`: 현재 페이지 (0-indexed)
- `size`: 페이지 크기
- `totalPages`: 전체 페이지 수

## 백엔드 페이징 로직 구현 여부

완료.
- StockService.getStockList(): 전체 필터링 후 in-memory subList 방식으로 페이징 처리
- StockSearchRequest에 이미 `page`(기본값 0), `size`(기본값 20) 필드가 존재하여 추가 수정 불필요
- 기본 페이지 크기 20, totalPages 계산, fromIndex/toIndex 범위 처리 포함

## 프론트 페이징 UI 구현 여부

완료.
- list.js: currentPage(0-indexed), PAGE_SIZE=20 상태 변수 추가
- loadStocks()에서 page/size 파라미터를 API에 전달, 응답에서 totalPages/totalCount 추출
- renderPagination(): 이전/다음 버튼 + 페이지 번호 버튼 렌더링, 클릭 시 currentPage 업데이트 후 재로드
- list.html: id="pagination" div 추가
- list.css: .pagination, .page-btn, .page-btn.active 스타일 추가
