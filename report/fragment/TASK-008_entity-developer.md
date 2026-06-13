# TASK-008 완료 보고 — Entity Developer

## 태스크
StockEntity 작성

## 결과
- 파일 생성: `E:\pp\develope\backend\src\main\java\com\example\demo\entity\StockEntity.java`
- @Entity, @Table(name = "stocks") 적용
- Lombok: @Getter, @NoArgsConstructor, @AllArgsConstructor, @Builder
- 전체 19개 필드 (id ~ updatedAt) camelCase 필드 + snake_case @Column 매핑 완료
- stockCode: unique = true, nullable = false
- stockName: nullable = false

## 이슈
없음
