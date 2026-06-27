package com.ndd.viebook.user;

public class UserMapper {
    public static UserResponse fromEntity(User user){
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .userStatus(user.getStatus().name())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
