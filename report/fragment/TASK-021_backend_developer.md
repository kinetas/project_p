# TASK-021 백엔드 DTO 필드 보완 — 완료 보고서

## 완료 시각
2026-06-14 (작업 완료)

## 작업 요약
프론트엔드 data.js의 MOCK_STOCKS 필드와 백엔드 DTO 간 불일치를 해소하기 위해 누락 필드를 추가하고, 저PBR TOP10 조회 기능을 신규 추가하였다.

### 변경 내용

#### 1. StockListResponse.java — 필드 추가
- `changeRate (Double)` — 기본값 0.0
- `changeAmount (Long)` — 기본값 0L
- `dividendYield (Double)` — 기본값 0.0
- `from(StockEntity)` 팩토리 메서드에서 위 세 필드를 0/0.0으로 세팅

#### 2. StockDetailResponse.java — 필드 추가
- `sharesOutstanding (Long)` — entity.getSharesOutstanding() 매핑
- `dividendYield (Double)` — 기본값 0.0
- `from(StockEntity)` 팩토리 메서드에서 위 두 필드 세팅

#### 3. StockRepository.java — 메서드 추가
- `findTop10ByPbrIsNotNullOrderByPbrAsc()` — 저PBR TOP10 JPA 파생 쿼리

#### 4. StockService.java — switch 케이스 추가
- `case "lowPbr"` → `stockRepository.findTop10ByPbrIsNotNullOrderByPbrAsc()` 호출

## 수정 파일 목록

| 파일 | 변경 유형 |
|------|---------|
| `E:\pp\develope\backend\src\main\java\com\example\demo\dto\stock\StockListResponse.java` | 수정 |
| `E:\pp\develope\backend\src\main\java\com\example\demo\dto\stock\StockDetailResponse.java` | 수정 |
| `E:\pp\develope\backend\src\main\java\com\example\demo\repository\StockRepository.java` | 수정 |
| `E:\pp\develope\backend\src\main\java\com\example\demo\service\StockService.java` | 수정 |

## 비고
- 기존 필드 및 코드 구조 유지 확인
- Lombok `@Builder` / `@Getter` 패턴 유지
- `StockEntity.java` 수정 없음 (`sharesOutstanding`은 이미 존재)
- Entity 외 허용 경로 내 파일만 수정

## 예상 토큰 소모량
- 규모: **소** (파일 4개, 각 30~100줄 내외의 소규모 필드/메서드 추가)
- 예상 입력 토큰: ~3,000
- 예상 출력 토큰: ~800
