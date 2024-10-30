package com.manchui.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manchui.domain.dto.CustomUserDetails;
import com.manchui.domain.entity.User;
import com.manchui.global.exception.CustomException;
import com.manchui.global.exception.ErrorCode;
import com.manchui.global.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        //Bearer 부분 제거 후 순수 토큰만 획득
        String accessToken= authorization.split(" ")[1];
        if (accessToken == null) {
            handleException(response, ErrorCode.MISSING_AUTHORIZATION_ACCESS_TOKEN);
            throw new CustomException(ErrorCode.MISSING_AUTHORIZATION_ACCESS_TOKEN);
        }

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(accessToken)) {
            handleException(response, ErrorCode.EXPIRED_JWT);
            throw new CustomException(ErrorCode.EXPIRED_JWT);
        }

        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {
            handleException(response, ErrorCode.INVALID_ACCESS_TOKEN);
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        String userEmail = jwtUtil.getUsername(accessToken);

        User user = new User(userEmail);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
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
