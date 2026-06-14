# TASK-029 DevOps 보고서: develope/front/ → resources/static/ 배포

## 완료 시각
2026-06-14 (작업 완료)

## 작업 개요
Spring Boot가 서빙하는 `develope/backend/src/main/resources/static/` 디렉토리에
`develope/front/`의 완성된 프론트엔드 파일들을 배포했다.

## 복사한 파일 수
총 16개 파일

## 파일 목록

### HTML 파일 (5개) — 덮어쓰기/신규 생성
| 소스 | 목적지 | 상태 |
|------|--------|------|
| develope/front/index.html | static/index.html | 덮어쓰기 |
| develope/front/list.html | static/list.html | 신규 생성 |
| develope/front/detail.html | static/detail.html | 덮어쓰기 |
| develope/front/login.html | static/login.html | 덮어쓰기 |
| develope/front/register.html | static/register.html | 덮어쓰기 |

### CSS 파일 (5개) — 신규 생성/덮어쓰기
| 소스 | 목적지 | 상태 |
|------|--------|------|
| develope/front/css/common.css | static/css/common.css | 신규 생성 |
| develope/front/css/home.css | static/css/home.css | 신규 생성 |
| develope/front/css/list.css | static/css/list.css | 신규 생성 |
| develope/front/css/detail.css | static/css/detail.css | 신규 생성 |
| develope/front/css/auth.css | static/css/auth.css | 덮어쓰기 |

### JS 파일 (6개) — 신규 생성/덮어쓰기
| 소스 | 목적지 | 상태 |
|------|--------|------|
| develope/front/js/common.js | static/js/common.js | 신규 생성 |
| develope/front/js/data.js | static/js/data.js | 신규 생성 |
| develope/front/js/home.js | static/js/home.js | 신규 생성 |
| develope/front/js/list.js | static/js/list.js | 신규 생성 |
| develope/front/js/detail.js | static/js/detail.js | 신규 생성 |
| develope/front/js/auth.js | static/js/auth.js | 덮어쓰기 |

## 보존된 기존 파일
- static/css/stock-list.css (유지)
- static/css/stock-detail.css (유지)
- static/js/stock-list.js (유지)
- static/js/stock-detail.js (유지)

## 주요 변경 사항
- 기존 index.html은 구버전 stock-list.css 기반이었으나, 새 Figma 디자인 기반 ValuePick UI로 교체
- detail.html은 Chart.js CDN 의존 구조에서 자체 Canvas 차트 렌더링 방식으로 교체
- login.html/register.html은 공통 헤더(header-placeholder) + common.css 연동 구조로 업그레이드
- list.html은 기존에 없던 파일로 새로 생성
- auth.js는 initHeader() 호출 + 간결한 구조의 새 버전으로 교체

## 결과
Spring Boot 기동 시 `/`, `/list.html`, `/detail.html`, `/login.html`, `/register.html` 경로로
새 ValuePick UI가 정상 서빙될 것으로 예상됨.
