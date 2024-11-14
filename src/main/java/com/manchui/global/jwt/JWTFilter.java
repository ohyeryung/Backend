package com.manchui.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.entity.User;
import com.manchui.domain.service.RedisRefreshTokenService;
import com.manchui.global.exception.CustomException;
import com.manchui.global.exception.ErrorCode;
import com.manchui.global.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RedisRefreshTokenService redisRefreshTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, CustomException {

        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        // 인증이 필요 없는 요청 처리
        if ((requestUri.matches("^\\/api\\/auths\\/signup$") && requestMethod.equals("POST")) ||
                (requestUri.matches("^\\/api\\/auths\\/check-name$") && requestMethod.equals("POST")) ||
                (requestUri.matches("^\\/api\\/auths\\/signin$") && requestMethod.equals("POST")) ||
                (requestUri.matches("^\\/api\\/reviews$") && requestMethod.equals("GET")) ||
                (requestUri.matches("^/swagger-ui(/.*)?$")) ||
                (requestUri.matches("^/swagger-ui.html$")) ||
                (requestUri.matches("^/v3/api-docs(?:/.*)?$"))) {
            filterChain.doFilter(request, response);
            return;
        }

        // 인증이 필요한 요청 처리
        String accessToken;
        String authorization = request.getHeader("Authorization");

        // /api/gatherings/public/** 요청에 대한 처리
        if (requestUri.matches("^/api/gatherings/public.*$") && requestMethod.equals("GET")) {
            // 토큰이 없는 경우 비회원으로 처리
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            accessToken = validateAccessToken(request, response, filterChain);
        } catch (Exception e) {
            return;
        }

        // 사용자 이메일 추출 및 인증 처리
        String userEmail = jwtUtil.getUsername(accessToken);
        User user = new User(userEmail);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private String validateAccessToken(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {

            handleException(response, ErrorCode.MISSING_AUTHORIZATION_ACCESS_TOKEN);
            throw new CustomException(ErrorCode.MISSING_AUTHORIZATION_ACCESS_TOKEN);
        }

        //Bearer 부분 제거 후 순수 토큰만 획득
        String accessToken = authorization.split(" ")[1];

        //응답 header에 accessToken이 없는 경우
        if (accessToken == null) {

            handleException(response, ErrorCode.MISSING_AUTHORIZATION_ACCESS_TOKEN);
            throw new CustomException(ErrorCode.MISSING_AUTHORIZATION_ACCESS_TOKEN);
        }

        //accessToken이 만료된 경우
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            handleException(response, ErrorCode.EXPIRED_JWT);
            throw new CustomException(ErrorCode.EXPIRED_JWT);
        } catch (SignatureException e) {
            handleException(response, ErrorCode.INVALID_ACCESS_TOKEN);
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {
            handleException(response, ErrorCode.INVALID_ACCESS_TOKEN);
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        String userEmail = jwtUtil.getUsername(accessToken);
        //Redis에 저장된 access 토큰 확인
        if (!redisRefreshTokenService.existsByAccessToken(userEmail)) {
            handleException(response, ErrorCode.INVALID_ACCESS_TOKEN);
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        return accessToken;
    }

    // 예외 처리 응답을 직접 설정하는 메서드
    private void handleException(HttpServletResponse response, ErrorCode errorCode) {

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse
                .create()
                .message(errorCode.getMessage())
                .httpStatus(errorCode.getHttpStatus());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
        } catch (IOException e) {
            log.error("Failed to write error response", e);
        }
    }

}
