# TASK-036 backend-developer 완료 보고서

## 완료 시각
2026-06-18

## 작업 요약
`KrxCollectorService.java`를 수정하여 기존 `StockEntity`/`StockRepository` 기반 저장 로직을 `CompanyEntity`/`StockPriceEntity` 기반으로 완전 교체하였다.

## 수정 내용

### 의존성 교체
- 제거: `StockRepository`, `StockEntity` import
- 추가: `CompanyRepository`, `StockPriceRepository`, `CompanyEntity`, `StockPriceEntity`, `StockPriceId` import
- `RestTemplate` 유지

### saveStock() 시그니처 변경
- `saveStock(item, market)` → `saveStock(item, market, basDt)` (basDt 파라미터 추가)
- `@Transactional` 어노테이션 적용

### company 처리 로직
- `companyRepository.findByStockCode(srtnCd)` 조회
- 존재 시: `toBuilder()`로 `isinCd`, `corpName(itmsNm)`, `corpCls` UPDATE
- 미존재 시: 스킵 (DART 선행 생성 필요 — corp_code PK null 불가)
- `convertCorpCls()` 메서드 추가: "KOSPI"→"Y", "KOSDAQ"→"K", "KONEX"→"N", 그 외→"E"

### stock_price 처리 로직
- `StockPriceId(basDt, srtnCd)` 복합 키 생성
- `StockPriceEntity` 빌더로 전체 필드 설정 후 `saveAndFlush()` UPSERT
- 저장 필드: basDt, srtnCd, isinCd, itmsNm, mrktCtg, clpr, vs, fltRt, mkp, hipr, lopr, trqu, trPrc, lstgStCnt, mrktTotAmt

### API 파싱 추가
- `parseBigDecimal()` 메서드 추가 (fltRt BigDecimal 변환)
- `parseString()` 빈 문자열 → null 반환 처리 강화
- `parseLong()` 빈 문자열 → null 반환 처리 강화
- 추가 파싱 필드: basDt(파라미터), isinCd, mrktCtg, vs, fltRt, mkp, hipr, lopr, trqu, trPrc, lstgStCnt

### 제거 사항
- `parseDouble()` 메서드 제거 (BigDecimal로 대체)
- `StockEntity` 관련 모든 코드 제거

## 예상 토큰 소모량
- 입력: 약 3,500 tokens (파일 6개 읽기)
- 출력: 약 1,200 tokens (코드 작성 + 보고서)
- 합계: 약 4,700 tokens
