# TASK-001 완료 보고서 — Schema Designer

## 완료 시각
2026-06-14

## 작업 요약
PRD Feature 0(회원 인증), Feature 1/2(종목 목록/상세), Feature 3(데이터 수집 파이프라인) 요구사항을 바탕으로 MySQL DDL 파일(`schema.sql`)을 작성하였다.

## 주요 결정사항

| 항목 | 결정 내용 |
|---|---|
| 엔진 | InnoDB (트랜잭션 및 외래키 지원) |
| 문자셋 | utf8mb4 / utf8mb4_unicode_ci (한글 + 이모지 안전) |
| DDL 방식 | `CREATE TABLE IF NOT EXISTS` — 멱등성 확보 |
| stocks 인덱스 | market, per, pbr, roe, debt_ratio — 목록 페이지 필터·정렬 성능 최적화 |
| financials FK | stock_code → stocks.stock_code (CASCADE UPDATE/DELETE) |
| JPA ddl-auto | `validate` 모드 사용 — 스키마를 코드가 아닌 schema.sql이 단독 관리 |

## 생성/수정 파일 목록

| 구분 | 경로 |
|---|---|
| 신규 생성 | `develope/backend/src/main/resources/schema.sql` |

## 예상 토큰 소모량
소
