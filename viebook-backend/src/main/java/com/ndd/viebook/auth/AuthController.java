package com.ndd.viebook.auth;

import com.ndd.viebook.common.exception.InvalidTokenException;
import com.ndd.viebook.common.response.ApiResponse;
import com.ndd.viebook.config.JwtConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtConfig jwtConfig;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> register(@Valid @RequestBody RegisterRequest request){
        AuthTokenResponse authTokenResponse = authService.register(request);

        ApiResponse<AuthTokenResponse> apiResponse = ApiResponse.<AuthTokenResponse>builder()
                .status(200)
                .message("Đăng ký thành công")
                .body(authTokenResponse)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshCookie(authTokenResponse.getRefreshToken()).toString())
                .body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> login(@Valid @RequestBody LoginRequest request){
        AuthTokenResponse authTokenResponse = authService.login(request);

        ApiResponse<AuthTokenResponse> apiResponse = ApiResponse.<AuthTokenResponse>builder()
                .status(200)
                .message("Đăng nhập thành công")
                .body(authTokenResponse)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshCookie(authTokenResponse.getRefreshToken()).toString())
                .body(apiResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ){

        if (refreshToken == null || refreshToken.isBlank()){
            throw new InvalidTokenException(HttpStatus.UNAUTHORIZED, "Refresh token null hoặc không có giá trị");
        }

        ApiResponse<AuthTokenResponse> apiResponse = ApiResponse.<AuthTokenResponse>builder()
                .status(200)
                .body(authService.refreshToken(refreshToken))
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ){
        authService.logout(refreshToken);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .httpOnly(true)
                .secure(false)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    private ResponseCookie createRefreshCookie(String refreshToken){
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .sameSite("Lax")
                .maxAge(Duration.ofMillis(jwtConfig.getRefreshExpirationMs()))
                .build();
    }
}
