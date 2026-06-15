# TASK-030 완료 보고서: RegisterRequest passwordConfirm 필드 제거

## 완료 시각
2026-06-15 (작업 완료)

## 작업 요약

### 버그 원인
프론트엔드(`auth.js`)는 회원가입 시 `{ email, nickname, password }` 만 서버로 전송하지만,
백엔드 `RegisterRequest.java`에 `@NotBlank private String passwordConfirm;` 가 존재하여
서버가 해당 필드를 `null`로 받아 Bean Validation에서 400 오류가 발생함.

### 수정 내용
- **파일**: `develope/backend/src/main/java/com/example/demo/dto/user/RegisterRequest.java`
- **변경**: `@NotBlank` 애노테이션이 붙은 `passwordConfirm` 필드 전체 삭제
- **근거**: `UserService.java`에서 `passwordConfirm`을 전혀 참조하지 않음 (email, password, nickname만 사용). 프론트엔드에서 이미 비밀번호 일치 검증을 처리하고 있으므로 백엔드 필드 자체가 불필요함.

### 수정 전후 비교
```java
// 수정 전
@NotBlank
private String passwordConfirm;  // <-- 삭제됨

// 수정 후: 필드 자체 제거됨
```

### 영향 범위
- `RegisterRequest.java`: passwordConfirm 필드 삭제
- `UserService.java`: 변경 없음 (passwordConfirm 미사용 확인)
- 프론트엔드 `auth.js`: 변경 없음 (이미 서버로 passwordConfirm 미전송)

## 예상 토큰 소모량
- 입력 토큰: 약 2,500 tokens
- 출력 토큰: 약 400 tokens
- 합계: 약 2,900 tokens
