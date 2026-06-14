# TASK-026 완료 보고서: GET /api/market/indices 엔드포인트 추가

## 완료 시각
2026-06-14 (작업 완료)

## 작업 요약
프론트엔드 홈 화면에서 필요한 KOSPI, KOSDAQ, USD/KRW, JPY/KRW 시장 지표를
제공하는 `GET /api/market/indices` REST 엔드포인트를 백엔드에 추가하였다.
현재는 정적 데이터를 반환하며, 추후 KRX 실시간 데이터 연동으로 교체 예정이다.

## 생성 파일 목록

| 파일 | 유형 | 설명 |
|------|------|------|
| `E:\pp\develope\backend\src\main\java\com\example\demo\dto\market\MarketIndexResponse.java` | 신규 생성 | 시장 지표 응답 DTO (name, value, changeRate, changeAmount) |
| `E:\pp\develope\backend\src\main\java\com\example\demo\controller\MarketIndexController.java` | 신규 생성 | GET /api/market/indices 엔드포인트 컨트롤러 |

## 수정 파일 목록
없음 (기존 파일 수정 금지 원칙 준수)

## API 스펙

- **엔드포인트**: `GET /api/market/indices`
- **응답 형식**: `application/json`
- **응답 예시**:
```json
[
  { "name": "KOSPI",   "value": "2,678.22", "changeRate": 1.24,  "changeAmount": 32.84  },
  { "name": "KOSDAQ",  "value": "758.45",   "changeRate": -0.68, "changeAmount": -5.19  },
  { "name": "USD/KRW", "value": "1,328.5",  "changeRate": 0.15,  "changeAmount": 2.00   },
  { "name": "JPY/KRW", "value": "8.95",     "changeRate": -0.22, "changeAmount": -0.02  }
]
```

## 코딩 규칙 준수 사항
- 패키지 구조: `controller/`, `dto/market/` — PRD 및 Coding_Rule.txt 준수
- 클래스명 PascalCase, 필드명 camelCase 사용
- URL은 소문자 + 하이픈(kebab-case) 사용 (`/api/market/indices`)
- TODO 주석으로 KRX 실시간 연동 예정 명시
- DTO에 `@Getter @Builder` Lombok 어노테이션 적용

## 예상 토큰 소모량
약 3,000~4,000 토큰 (참고 파일 읽기 + 파일 2개 생성 + 보고서 작성 포함)
