# TASK-002 완료 보고서 — Config Writer

## 완료 시각
2026-06-14

## 작업 요약
기존 `application.properties`(내용: `spring.application.name=demo` 1줄)에 MySQL 연결, JPA/Hibernate, 외부 API Key 양식, 서버 포트 설정을 추가하였다.

## 주요 결정사항

| 항목 | 결정 내용 |
|---|---|
| DB 이름 | `value_stock` (PRD 프로젝트명 반영) |
| ddl-auto | `validate` — schema.sql 단독 스키마 관리, JPA가 테이블 변경하지 않음 |
| show-sql | `true` — 개발 단계 디버깅 편의 |
| dialect | `MySQL8Dialect` — MySQL 8.x 최적 SQL 생성 |
| API Key | 값 비워둔 양식만 커밋 — Coding Rule 6항 준수 (Git 노출 방지) |
| 비밀번호 | 값 비워둠 — 로컬에서 직접 입력 |

## 생성/수정 파일 목록

| 구분 | 경로 |
|---|---|
| 수정 | `develope/backend/src/main/resources/application.properties` |

## 예상 토큰 소모량
소
