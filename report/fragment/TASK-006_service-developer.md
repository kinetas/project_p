# TASK-006 완료 보고서 — Service Developer

## 완료 시각
2026-06-14

## 작업 요약
회원가입(`register`)과 로그인(`login`) 비즈니스 로직을 담은 `UserService`를 작성하였다.
BCrypt 암호화를 위한 `PasswordEncoderConfig`도 함께 생성하였다.

## 주요 결정사항

| 항목 | 결정 내용 |
|---|---|
| 패키지 | `com.example.demo.service` |
| 의존성 주입 | 생성자 주입 — Coding Rule 4항(레이어 역할 분리), Spring 권장 방식 |
| BCryptPasswordEncoder | `@Bean` 등록 분리 (`PasswordEncoderConfig`) — 단일 책임 원칙 |
| register | 이메일 중복 → `existsByEmail` 확인 → BCrypt 인코딩 → 저장 → `UserResponse` 반환 |
| login | 이메일 조회 → `matches` 검증 → 불일치 시 동일 예외 메시지 (정보 노출 방지) |
| 예외 처리 | `RuntimeException` 사용 — 추후 커스텀 예외 클래스 도입 예정 |

## 생성/수정 파일 목록

| 구분 | 경로 |
|---|---|
| 신규 생성 | `E:\pp\develope\backend\src\main\java\com\example\demo\service\UserService.java` |
| 신규 생성 | `E:\pp\develope\backend\src\main\java\com\example\demo\config\PasswordEncoderConfig.java` |

## 예상 토큰 소모량
소
