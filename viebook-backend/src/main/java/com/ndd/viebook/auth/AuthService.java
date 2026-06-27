package com.ndd.viebook.auth;

import com.ndd.viebook.common.exception.ConflictException;
import com.ndd.viebook.common.exception.InvalidTokenException;
import com.ndd.viebook.config.JwtConfig;
import com.ndd.viebook.security.JwtService;
import com.ndd.viebook.user.User;
import com.ndd.viebook.user.UserMapper;
import com.ndd.viebook.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthTokenResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())){
            throw new ConflictException(HttpStatus.CONFLICT, "Email đã được đăng ký");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        user = userRepository.save(user);

        return createAuthTokenResponse(user);
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        return createAuthTokenResponse(user);
    }

    public AuthTokenResponse refreshToken(String refreshTokenStr){
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenStr)
                .orElseThrow(() -> new InvalidTokenException(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ"));

        if(refreshToken.isRevoked()){
            throw new InvalidTokenException(HttpStatus.UNAUTHORIZED, "Refresh token đã bị thu hồi");
        }

        if(refreshToken.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new InvalidTokenException(HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn");
        }

        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateToken(user);

        return AuthTokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(newAccessToken)
                .userResponse(UserMapper.fromEntity(user))
                .build();
    }

    @Transactional
    public void logout(String refreshTokenStr){
        refreshTokenRepository.findByRefreshToken(refreshTokenStr)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                });
    }

    private AuthTokenResponse createAuthTokenResponse(User user){
        String accessToken = jwtService.generateToken(user);
        String refreshTokenStr = jwtService.generateRefresh(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .refreshToken(refreshTokenStr)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusNanos(jwtConfig.getRefreshExpirationMs() * 1000000))
                .user(user)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userResponse(UserMapper.fromEntity(user))
                .refreshToken(refreshTokenStr)
                .build();
    }
}
