# TASK-019 완료 보고 — 종목 상세 페이지

## 완료 시각
2026-06-14

## 작업 요약
종목 상세 페이지(detail.html)와 전용 CSS/JS를 작성했다.
URL 파라미터 ?code= 에서 종목코드를 추출하여 /api/stocks/{code} 및 /api/stocks/{code}/financials 를 호출한다.
Chart.js CDN을 사용하여 매출액/영업이익 묶음 막대, ROE 추이, 부채비율 추이 차트 3개를 구현했다.

## 주요 결정사항

| 항목 | 결정 | 이유 |
|---|---|---|
| Chart.js 버전 | 4.4.2 CDN | 안정 최신 버전, 번들러 미사용 환경 |
| 배당수익률 | "-" 표시 (PRD 후순위 표기) | API 미확보 상태 반영 |
| 차트 영역 | CSS grid 2fr:1fr:1fr 비율 | 매출/영업이익 차트가 상대적으로 넓어야 가독성 확보 |
| 재무 데이터 없을 때 | 차트 캔버스 유지, 메시지 출력 | DOM 구조 일관성 유지 |
| 금액 포맷 | formatAmount (조/억 단위 자동 변환) | 재무제표 수치 가독성 |
| 날짜 포맷 | YYYYMMDD → YYYY.MM.DD 변환 | DART API 응답 형식 대응 |
| 차트 인스턴스 | chartRevenue/chartRoe/chartDebt 전역 변수 유지 | 재호출 시 destroy() 후 재생성으로 메모리 누수 방지 |
| 탭 전환 | fin-tab-btn data-tab 속성 | 재사용 가능한 선언적 구조 |

## 생성/수정 파일 목록

| 경로 | 액션 |
|---|---|
| E:\pp\develope\backend\src\main\resources\static\detail.html | 생성 |
| E:\pp\develope\backend\src\main\resources\static\css\stock-detail.css | 생성 |
| E:\pp\develope\backend\src\main\resources\static\js\stock-detail.js | 생성 |

## 예상 토큰 소모량
대 (차트 3개 + 재무 테이블 3탭 + 요약카드/기업정보/가치지표 렌더링 포함)
