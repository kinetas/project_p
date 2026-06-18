# TASK-040 backend-developer 작업 보고서

## 완료 시각
2026-06-18

## 작업 요약
두 파일 수정 완료:
1. `develope/backend/src/main/resources/schema.sql` 신규 생성 (doc/schema.sql 전체 내용으로 교체)
2. `develope/backend/src/main/java/com/example/demo/config/DataCollectionScheduler.java` 파이프라인 순서 변경 및 CompanyRepository 교체

---

## 수정 내용

### 파일 1: schema.sql
- 경로: `develope/backend/src/main/resources/schema.sql`
- 기존 파일이 존재하지 않아 신규 생성
- `doc/schema.sql` 전체 내용 그대로 복사 (11개 테이블 정의)

### 파일 2: DataCollectionScheduler.java
- 경로: `develope/backend/src/main/java/com/example/demo/config/DataCollectionScheduler.java`

#### Import 변경
| 제거 | 추가 |
|------|------|
| `com.example.demo.entity.StockEntity` | `com.example.demo.entity.CompanyEntity` |
| `com.example.demo.repository.StockRepository` | `com.example.demo.repository.CompanyRepository` |

#### 필드 변경
- `StockRepository stockRepository` → `CompanyRepository companyRepository`

#### 파이프라인 순서 변경
| 기존 | 변경 후 |
|------|---------|
| Step 1: KRX 회사목록/현재시세 | Step 1: DART corp_code.xml → company |
| Step 2: DART corp_code 매핑 (조건부) | Step 2: KRX 주식시세 → stock_price + company |
| Step 3: DART 재무제표 | Step 3: DART 재무제표 → financial_statement |
| Step 4: 금융위원회 배당 | Step 4: 금융위원회 배당 → dividend_info |
| Step 5: 투자지표 계산 | Step 5: 지표 계산 → stock_indicator |

#### allStocks 조회 변경
- `stockRepository.findAll()` → `companyRepository.findAll()`
- 루프 내 `StockEntity` → `CompanyEntity` 타입 변경
- `stock.getDartCorpCode()` → `company.getCorpCode()`
- `stock.getStockCode()` → `company.getStockCode()`

#### TODO 처리
- `KrxCollectorService`에 `collectStockData()` 메서드 미존재 확인
- 기존 메서드명 `collectStockList()`을 유지하며 TODO 주석 추가

---

## 예상 토큰 소모량
- 입력: 약 5,000 토큰 (파일 4개 읽기)
- 출력: 약 3,000 토큰 (파일 2개 쓰기)
- 합계: 약 8,000 토큰
