package com.ndd.viebook.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ndd.viebook.user.UserResponse;
import lombok.*;


@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenResponse {
    private String accessToken;
    private String tokenType;
    private UserResponse userResponse;

    @JsonIgnore
    private String refreshToken;
}
