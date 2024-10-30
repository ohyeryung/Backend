package com.manchui.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manchui.domain.service.RedisRefreshTokenService;
import com.manchui.global.exception.CustomException;
import com.manchui.global.exception.ErrorCode;
import com.manchui.global.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RedisRefreshTokenService redisRefreshTokenService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException, CustomException {

        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        // /api/auths/signout에 POST요청이 아닐시 다음 필터로
        if (!requestUri.matches("^\\/api\\/auths\\/signout$") || !requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //access 토큰 검증
            validateAccessToken(request, response, filterChain);

            String refresh = null;
            //refresh 토큰 검증
            validateRefreshToken(request, response, refresh);
        } catch (Exception e) {
            return;
        }


        //Refresh 토큰 쿠키 만료
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse =
                "{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"로그아웃 성공\",\n" +
                "    \"data\": null\n" +
                "}";

        response.getWriter().write(jsonResponse);
    }

    private void validateRefreshToken(HttpServletRequest request, HttpServletResponse response, String refresh) {

        try {
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {

                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }
        } catch (NullPointerException e) {
            refresh = null;
        }

        //응답 쿠키에 refreshToken이 없는 경우
        if (refresh == null) {
            handleException(response, ErrorCode.MISSING_AUTHORIZATION_REFRESH_TOKEN);
            throw new CustomException(ErrorCode.MISSING_AUTHORIZATION_REFRESH_TOKEN);
        }

        //refreshToken이 만료된 경우
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            handleException(response, ErrorCode.MISSING_AUTHORIZATION_REFRESH_TOKEN);
            throw new CustomException(ErrorCode.MISSING_AUTHORIZATION_REFRESH_TOKEN);
        } catch (SignatureException | MalformedJwtException e) {
            handleException(response, ErrorCode.INVALID_REFRESH_TOKEN);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            handleException(response, ErrorCode.INVALID_REFRESH_TOKEN);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String userEmail = jwtUtil.getUsername(refresh);
        //Redis에 저장된 refresh 토큰 확인
        if (!redisRefreshTokenService.existsByRefreshToken(userEmail)) {
            handleException(response, ErrorCode.INVALID_REFRESH_TOKEN);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        redisRefreshTokenService.deleteRefreshToken(userEmail);
    }

    private void validateAccessToken(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            handleException(response, ErrorCode.INVALID_ACCESS_TOKEN);
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        //Bearer 부분 제거 후 순수 토큰만 획득
        String accessToken= authorization.split(" ")[1];

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

        redisRefreshTokenService.deleteAccessToken(userEmail);
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