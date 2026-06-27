package com.ndd.viebook.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndd.viebook.common.exception.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
    throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try{
            String userEmail = jwtService.extractEmail(jwt);

            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)){
                    UsernamePasswordAuthenticationToken authToken
                            = UsernamePasswordAuthenticationToken.authenticated(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }catch(UsernameNotFoundException ex){
            log.warn("Không tìm thấy người dùng {}", ex.getMessage());
            sendErrorResponse(response, request, "Người dùng không tồn tại");
            return;
        }
        catch(ExpiredJwtException ex){
            sendErrorResponse(response, request, "Token hết hạn");
            return;
        }catch (JwtException ex){
            sendErrorResponse(response, request, "Token không hợp lệ");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(
            HttpServletResponse response, HttpServletRequest request, String message
    ) throws IOException{
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ErrorResponse.builder()
                                .status(401)
                                .path(request.getRequestURI())
                                .message(message)
                                .errors(null)
                                .timestamp(LocalDateTime.now())
                                .build()
                )
        );
    }
}
