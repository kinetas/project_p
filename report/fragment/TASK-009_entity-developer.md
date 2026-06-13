# TASK-009 완료 보고 — Entity Developer

## 태스크
FinancialEntity 작성

## 결과
- 파일 생성: `E:\pp\develope\backend\src\main\java\com\example\demo\entity\FinancialEntity.java`
- @Entity, @Table(name = "financials") 적용
- @UniqueConstraint(columnNames = {"stock_code", "year"}) 적용
- Lombok: @Getter, @NoArgsConstructor, @AllArgsConstructor, @Builder
- 전체 16개 필드 (id ~ pbr) camelCase 필드 + snake_case @Column 매핑 완료
- stockCode: nullable = false, year: nullable = false

## 이슈
없음
