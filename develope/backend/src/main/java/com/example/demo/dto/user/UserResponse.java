package com.example.demo.dto.user;

import com.example.demo.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;

    public static UserResponse from(UserEntity entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
