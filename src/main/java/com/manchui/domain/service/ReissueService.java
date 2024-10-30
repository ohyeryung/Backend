package com.manchui.domain.service;

import com.manchui.global.exception.CustomException;
import com.manchui.global.exception.ErrorCode;
import com.manchui.global.jwt.JWTUtil;
import com.manchui.global.response.SuccessResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RedisRefreshTokenService redisRefreshTokenService;
    @Value("${token.access.expiration}")
    private long accessTokenExpiration;
    @Value("${token.refresh.expiration}")
    private long refreshTokenExpiration;

    public ResponseEntity<SuccessResponse<Void>> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {

            throw new CustomException(ErrorCode.MISSING_AUTHORIZATION_REFRESH_TOKEN);
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            throw new CustomException(ErrorCode.MISSING_AUTHORIZATION_REFRESH_TOKEN);
        }

        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {

            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String userEmail = jwtUtil.getUsername(refresh);
        //Redis에 저장된 refresh 토큰 확인
        if (!redisRefreshTokenService.existsByRefreshToken(userEmail)) {

            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccess = jwtUtil.createJwt("access", userEmail, accessTokenExpiration);
        String newRefresh = jwtUtil.createJwt("refresh", userEmail, refreshTokenExpiration);

        redisRefreshTokenService.deleteRefreshToken(userEmail);
        redisRefreshTokenService.saveRefreshToken(userEmail, newRefresh, refreshTokenExpiration);

        response.setHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return ResponseEntity.ok().body(SuccessResponse.successWithNoData("refresh 토큰 재발급 성공"));
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

}
