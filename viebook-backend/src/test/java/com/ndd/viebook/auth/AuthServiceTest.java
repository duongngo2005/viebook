package com.ndd.viebook.auth;

import com.ndd.viebook.common.exception.ConflictException;
import com.ndd.viebook.config.JwtConfig;
import com.ndd.viebook.security.JwtService;
import com.ndd.viebook.user.User;
import com.ndd.viebook.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.config.ConfigDataException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private JwtConfig jwtConfig;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register: email chưa tồn tại -> đăng ký thành công, trả về token")
    void register_success(){
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duong@gmail.com");
        request.setPassword("123456");
        request.setFullName("Ngô Đình Dương");

        User savedUser = User.builder()
                .email("duong@gmail.com")
                .passwordHash("hashed_password")
                .fullName("Ngô Đình Dương")
                .build();

        when(userRepository.existsByEmail("duong@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token-abc");
        when(jwtService.generateRefresh(any(User.class))).thenReturn("refresh-token-xyz");
        when(jwtConfig.getRefreshExpirationMs()).thenReturn(86400000L);

        AuthTokenResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token-abc");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-xyz");
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        verify(userRepository, times(1)).save(any(User.class));
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("register: email đã tồn tại -> ném ConflictException")
    void login_fail_existsEmail(){
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duong@gmail.com");
        request.setPassword("123456");
        request.setFullName("Ngo Dinh Duong");

        when(userRepository.existsByEmail("duong@gmail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login: thông tin đăng nhập đúng -> đăng nhập thành công, trả token")
    void login_success(){
        LoginRequest request = new LoginRequest();
        request.setEmail("duong@gmail.com");
        request.setPassword("123456");

        User user = User.builder()
                    .email("duong@gmail.com")
                    .passwordHash("hashed_password")
                    .fullName("Ngô Đình Dương")
                    .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.generateRefresh(user)).thenReturn("refresh-token");
        when(jwtConfig.getRefreshExpirationMs()).thenReturn(86400000L);

        AuthTokenResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("login: password sai -> đăng nhập không thành công, ném BadCredientialsException")
    void login_fail_wrongPassword(){
        LoginRequest request = new LoginRequest();
        request.setPassword("123456");
        request.setEmail("duong@gmail.com");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Mật khẩu sai"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(refreshTokenRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }
}
