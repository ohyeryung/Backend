package com.manchui.domain.controller;

import com.manchui.domain.service.Oauth2Service;
import com.manchui.global.response.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/login/oauth2/callback")
@RequiredArgsConstructor
public class Oauth2Controller {

    private final Oauth2Service oauth2Service;

    @GetMapping("/kakao")
    public ResponseEntity<SuccessResponse<Void>> kakaoLogin(HttpServletRequest request) {
        // 인가 코드 가져오기
        String code = request.getParameter("code");
        // 카카오 액세스 토큰 요청
        String kakaoAccessToken = oauth2Service.getKakaoAccessToken(code);
        // 로그인 처리 및 응답 반환
        return oauth2Service.kakaoLogin(kakaoAccessToken);
    }

    @GetMapping("/google")
    public ResponseEntity<SuccessResponse<Void>> googleLogin(HttpServletRequest request) {
        String code = request.getParameter("code");
        String googleAccessToken = oauth2Service.getGoogleAccessToken(code);
        return oauth2Service.googleLogin(googleAccessToken);
    }

    @GetMapping("/naver")
    public ResponseEntity<SuccessResponse<Void>> naverLogin(HttpServletRequest request) {
        String code = request.getParameter("code");
        String naverAccessToken = oauth2Service.getNaverAccessToken(code);
        return oauth2Service.naverLogin(naverAccessToken);
    }
}