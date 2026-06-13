# TASK-020 완료 보고 — 회원 인증 페이지

## 완료 시각
2026-06-14

## 작업 요약
로그인(login.html), 회원가입(register.html) 페이지와 공통 CSS/JS를 작성했다.
body[data-page] 속성으로 단일 auth.js에서 페이지별 분기 처리한다.

## 주요 결정사항

| 항목 | 결정 | 이유 |
|---|---|---|
| 단일 auth.js | body[data-page="login"|"register"] 분기 | 공통 로직(에러 표시, 폼 헬퍼) 재사용 |
| 비밀번호 유효성 | 8자 미만 시 실시간 필드 에러 | 사용자 경험 향상, PRD 요구사항 |
| 비밀번호 확인 일치 | 실시간 input 이벤트 검사 | 제출 전 즉각 피드백 |
| 로그인 성공 처리 | JWT token → sessionStorage 저장 후 index.html 이동 | 세션 방식도 credentials:include로 쿠키 대응 |
| 회원가입 에러 분기 | 이메일 관련 에러는 필드 에러, 그 외는 alert | 정확한 위치 에러 표시 |
| 카드 UI | max-width 400px 중앙 정렬 카드 | PRD "깔끔한 카드 형태 UI" 요구사항 |
| 접근성 | role="alert" aria-live="polite" | 스크린리더 에러 공지 지원 |

## 생성/수정 파일 목록

| 경로 | 액션 |
|---|---|
| E:\pp\develope\backend\src\main\resources\static\login.html | 생성 |
| E:\pp\develope\backend\src\main\resources\static\register.html | 생성 |
| E:\pp\develope\backend\src\main\resources\static\css\auth.css | 생성 |
| E:\pp\develope\backend\src\main\resources\static\js\auth.js | 생성 |

## 예상 토큰 소모량
중 (2개 HTML + 공통 CSS/JS, 유효성 검사 로직 포함)
