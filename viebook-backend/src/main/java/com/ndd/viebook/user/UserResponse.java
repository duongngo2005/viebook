package com.ndd.viebook.user;

import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String avatarUrl;
    private String userStatus;
    private boolean emailVerified;
}
