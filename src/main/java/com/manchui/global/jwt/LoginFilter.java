package com.manchui.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manchui.domain.dto.LoginDTO;
import com.manchui.global.exception.ErrorCode;
import com.manchui.global.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final Validator validator;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, Validator validator) {
        super();
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.validator = validator;
        setFilterProcessesUrl("/api/auths/signin");  // 필터의 URL 매핑 설정
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException{

        LoginDTO loginDTO = new LoginDTO();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            loginDTO = objectMapper.readValue(messageBody, LoginDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // DTO 유효성 검증
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<LoginDTO> violation : violations) {
                String fieldName = violation.getPropertyPath().toString();  // 필드명 가져오기
                if (fieldName.equals("email")) {
                    log.info("아이디 미입력");
                    handleException(response, ErrorCode.MISSING_EMAIL);
                    return null; // 예외가 발생한 경우 인증을 진행하지 않고 메서드를 종료
                } else if (fieldName.equals("password")) {
                    log.info("비밀번호 미입력");
                    handleException(response, ErrorCode.MISSING_PASSWORD);
                    return null;
                }
            }
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword(), null);

        return authenticationManager.authenticate(authToken);
    }

    //로그인 성공시 실행
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {

        String userEmail = authentication.getName();

        String access = jwtUtil.createJwt("access", userEmail, 600000L);
        String refresh = jwtUtil.createJwt("refresh", userEmail, 8640000L);

        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse =
                "{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"로그인 성공\",\n" +
                "    \"data\": null\n" +
                "}";

        response.getWriter().write(jsonResponse);
    }

    //로그인 실패시 실행
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse =
                "{\n" +
                "    \"success\": false,\n" +
                "    \"message\": \"회원 정보가 일치하지 않습니다.\",\n" +
                "    \"httpStatus\": \"UNAUTHORIZED\"\n" +
                "}";

        response.getWriter().write(jsonResponse);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
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
