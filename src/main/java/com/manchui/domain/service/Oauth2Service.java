package com.manchui.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manchui.domain.entity.Image;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.ImageRepository;
import com.manchui.domain.repository.UserRepository;
import com.manchui.global.exception.CustomException;
import com.manchui.global.exception.ErrorCode;
import com.manchui.global.jwt.JWTUtil;
import com.manchui.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class Oauth2Service {

    private final ObjectMapper objectMapper;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisRefreshTokenService redisRefreshTokenService;
    private final ImageServiceImpl imageService;
    private final ImageRepository imageRepository;
    @Value("${token.access.expiration}")
    private Long accessTokenExpiration;

    @Value("${token.refresh.expiration}")
    private Long refreshTokenExpiration;

    @Value("${kakao.client.id}")
    private String kakaoClientId;

    @Value("${kakao.client.secret}")
    private String kakaoClientSecret;

    @Value("${kakao.client.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.secret}")
    private String googleClientSecret;

    @Value("${google.client.redirect-uri}")
    private String googleRedirectUri;

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${naver.client.secret}")
    private String naverClientSecret;

    @Value("${naver.client.redirect-uri}")
    private String naverRedirectUri;


    // 카카오 액세스 토큰 가져오기
    public String getKakaoAccessToken(String code) {
        try {
            // 카카오 API의 액세스 토큰 요청 URL
            String tokenUrl = "https://kauth.kakao.com/oauth/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 액세스 토큰 요청에 필요한 파라미터 설정
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code"); // 인증 방식
            body.add("client_id", kakaoClientId); // 애플리케이션의 클라이언트 ID
            body.add("client_secret", kakaoClientSecret); // 애플리케이션의 클라이언트 시크릿
            body.add("redirect_uri", kakaoRedirectUri); // 리다이렉트 URI
            body.add("code", code); // 카카오에서 전달받은 인가 코드

            // HTTP 요청 엔티티 생성
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // RestTemplate를 사용하여 카카오 API에 POST 요청을 보냄
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            // 응답 상태 코드 확인, 성공이 아닐 경우 예외 발생
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new CustomException(ErrorCode.KAKAO_TOKEN_FETCH_FAILED);
            }

            // JSON 파싱하여 액세스 토큰 반환
            JsonNode tokenNode = objectMapper.readTree(response.getBody());
            return tokenNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("KAKAO_AUTHENTICATION_FAILED: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.KAKAO_AUTHENTICATION_FAILED);
        }
    }

    public ResponseEntity<SuccessResponse<Void>> kakaoLogin(String kakaoAccessToken) {
        try {
            // 카카오 사용자 정보 요청 URL
            String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
            HttpHeaders headers = new HttpHeaders();

            // Bearer 방식으로 액세스 토큰을 헤더에 설정
            headers.setBearerAuth(kakaoAccessToken);

            // HTTP 요청 엔티티 생성
            HttpEntity<Void> request = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            // RestTemplate를 사용하여 사용자 정보 요청
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

            // 응답 상태 코드 확인, 성공이 아닐 경우 예외 발생
            if (userInfoResponse.getStatusCode() != HttpStatus.OK) {
                throw new CustomException(ErrorCode.KAKAO_USER_INFO_FETCH_FAILED);
            }

            // 사용자 정보 파싱
            JsonNode userInfoNode = objectMapper.readTree(userInfoResponse.getBody());
            String email = userInfoNode.path("kakao_account").path("email").asText();
            String nickname = userInfoNode.path("properties").path("nickname").asText();
            String id = userInfoNode.path("id").asText();
            String profileImageUrl = userInfoNode.path("properties").path("profile_image").asText();

            String filePath = null;
            if (!(profileImageUrl.isEmpty())) {
                Long imageId = imageService.uploadUserProfileImageFromUrl(profileImageUrl);
                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
                filePath = image.getFilePath();
            }

            // 데이터베이스에서 해당 이메일의 사용자 조회
            User findUser = userRepository.findByEmail(email);
            if (findUser == null) {
                // 사용자가 없으면 새로 생성하여 저장
                User user = new User("kakao" + id, email, nickname, filePath);
                userRepository.save(user);
            } else {
                // 다른 OAuth 제공자로 동일한 이메일을 사용하는 경우 예외 발생
                if (!findUser.getOauth2Id().equals("kakao" + id)) {
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER);
                }
            }

            // JWT 토큰 생성
            String accessToken = jwtUtil.createJwt("access", email, accessTokenExpiration);
            String refreshToken = jwtUtil.createJwt("refresh", email, refreshTokenExpiration);

            // 토큰 저장
            redisRefreshTokenService.saveAccessToken(email, accessToken, accessTokenExpiration);
            redisRefreshTokenService.saveRefreshToken(email, refreshToken, refreshTokenExpiration);

            // HttpHeaders에 액세스 토큰 설정
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Authorization", "Bearer " + accessToken);

            // 리프레시 토큰 쿠키 생성 및 HttpHeaders에 추가
            ResponseCookie refreshTokenCookie = setResponseCookie(refreshToken);
            responseHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            // 응답 반환
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(SuccessResponse.successWithNoData("로그인 성공"));
        }catch (CustomException e) {
            // DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER 예외일 경우 처리
            if (e.getErrorCode() == ErrorCode.DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER) {
                log.error("DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER: {}", e.getMessage(), e);
                throw e; // 그대로 다시 던짐
            }
            // 다른 CustomException도 그대로 던짐
            log.error("CustomException occurred: {}", e.getMessage(), e);
            throw e;
        }catch (Exception e) {
            log.error("KAKAO_LOGIN_PROCESS_FAILED: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.KAKAO_LOGIN_PROCESS_FAILED);
        }
    }

    // Google 액세스 토큰 가져오기
    public String getGoogleAccessToken(String code) {
        try {
            // Google 토큰 발급 API URL
            String googleTokenUri = "https://oauth2.googleapis.com/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 액세스 토큰 요청에 필요한 파라미터 설정
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", googleClientId);
            body.add("client_secret", googleClientSecret);
            body.add("redirect_uri", googleRedirectUri);
            body.add("code", code);

            // HTTP 요청 엔티티 생성
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            // RestTemplate를 사용하여 카카오 API에 POST 요청을 보냄
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(googleTokenUri, request, String.class);

            // 응답 상태 코드 확인, 성공이 아닐 경우 예외 발생
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new CustomException(ErrorCode.GOOGLE_TOKEN_FETCH_FAILED);
            }
            // JSON 파싱하여 액세스 토큰 반환
            JsonNode tokenNode = objectMapper.readTree(response.getBody());
            return tokenNode.get("access_token").asText();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GOOGLE_AUTHENTICATION_FAILED);
        }
    }

    // Google 사용자 정보 가져오기 및 로그인 처리
    public ResponseEntity<SuccessResponse<Void>> googleLogin(String googleAccessToken) {
        try {
            // Google 사용자 정보 요청 URL
            String googleUserInfoUri = "https://www.googleapis.com/oauth2/v2/userinfo";
            HttpHeaders headers = new HttpHeaders();
            // Bearer 방식으로 액세스 토큰을 헤더에 설정
            headers.setBearerAuth(googleAccessToken);

            // HTTP 요청 엔티티 생성
            HttpEntity<Void> request = new HttpEntity<>(headers);
            // RestTemplate를 사용하여 사용자 정보 요청
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(googleUserInfoUri, HttpMethod.GET, request, String.class);

            // 응답 상태 코드 확인, 성공이 아닐 경우 예외 발생
            if (userInfoResponse.getStatusCode() != HttpStatus.OK) {
                throw new CustomException(ErrorCode.GOOGLE_USER_INFO_FETCH_FAILED);
            }

            // 사용자 정보 파싱
            JsonNode userInfoNode = objectMapper.readTree(userInfoResponse.getBody());
            String email = userInfoNode.path("email").asText();
            String name = userInfoNode.path("name").asText();
            String id = userInfoNode.path("id").asText();
            String profileImageUrl = userInfoNode.path("picture").asText();

            String filePath = null;
            if (!(profileImageUrl.isEmpty())) {
                Long imageId = imageService.uploadUserProfileImageFromUrl(profileImageUrl);
                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
                filePath = image.getFilePath();
                log.info("filePath = {}", filePath);
            }
            // 데이터베이스에서 해당 이메일의 사용자 조회
            User findUser = userRepository.findByEmail(email);
            if (findUser == null) {
                // 사용자가 없으면 새로 생성하여 저장
                User user = new User("google" + id, email, name, filePath);
                userRepository.save(user);
            } else {
                if (!findUser.getOauth2Id().equals("google" + id)) {
                    // 다른 OAuth 제공자로 동일한 이메일을 사용하는 경우 예외 발생
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER);
                }
            }

            // JWT 토큰 생성
            String accessToken = jwtUtil.createJwt("access", email, accessTokenExpiration);
            String refreshToken = jwtUtil.createJwt("refresh", email, refreshTokenExpiration);

            // 토큰 저장
            redisRefreshTokenService.saveAccessToken(email, accessToken, accessTokenExpiration);
            redisRefreshTokenService.saveRefreshToken(email, refreshToken, refreshTokenExpiration);

            // HttpHeaders에 액세스 토큰 설정
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Authorization", "Bearer " + accessToken);

            // 리프레시 토큰 쿠키 생성 및 HttpHeaders에 추가
            ResponseCookie refreshTokenCookie = setResponseCookie(refreshToken);
            responseHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            // 응답 반환
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(SuccessResponse.successWithNoData("로그인 성공"));
        } catch (CustomException e) {
            // DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER 예외일 경우 처리
            if (e.getErrorCode() == ErrorCode.DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER) {
                log.error("DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER: {}", e.getMessage(), e);
                throw e; // 그대로 다시 던짐
            }
            // 다른 CustomException도 그대로 던짐
            log.error("CustomException occurred: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GOOGLE_LOGIN_PROCESS_FAILED);
        }
    }


    // 네이버 액세스 토큰 가져오기
    public String getNaverAccessToken(String code) {
        try {
            // 네이버 API의 액세스 토큰 요청 URL
            String naverTokenUri =  "https://nid.naver.com/oauth2.0/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 액세스 토큰 요청에 필요한 파라미터 설정
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", naverClientId);
            body.add("client_secret", naverClientSecret);
            body.add("redirect_uri", naverRedirectUri);
            body.add("code", code);

            // HTTP 요청 엔티티 생성
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            // RestTemplate를 사용하여 카카오 API에 POST 요청을 보냄
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(naverTokenUri, request, String.class);

            // 응답 상태 코드 확인, 성공이 아닐 경우 예외 발생
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new CustomException(ErrorCode.NAVER_TOKEN_FETCH_FAILED);
            }

            // JSON 파싱하여 액세스 토큰 반환
            JsonNode tokenNode = objectMapper.readTree(response.getBody());
            return tokenNode.get("access_token").asText();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.NAVER_AUTHENTICATION_FAILED);
        }
    }

    // 네이버 사용자 정보 가져오기 및 로그인 처리
    public ResponseEntity<SuccessResponse<Void>> naverLogin(String naverAccessToken) {
        try {
            // 네이버 사용자 정보 요청 URL
            String naverUserInfoUri = "https://openapi.naver.com/v1/nid/me";
            HttpHeaders headers = new HttpHeaders();
            // Bearer 방식으로 액세스 토큰을 헤더에 설정
            headers.setBearerAuth(naverAccessToken);

            // HTTP 요청 엔티티 생성
            HttpEntity<Void> request = new HttpEntity<>(headers);
            // RestTemplate를 사용하여 사용자 정보 요청
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(naverUserInfoUri, HttpMethod.GET, request, String.class);

            // 응답 상태 코드 확인, 성공이 아닐 경우 예외 발생
            if (userInfoResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to fetch Naver user info");
            }

            // 사용자 정보 파싱
            JsonNode userInfoNode = objectMapper.readTree(userInfoResponse.getBody());
            String email = userInfoNode.path("response").path("email").asText();
            String nickname = userInfoNode.path("response").path("nickname").asText();
            String id = userInfoNode.path("response").path("id").asText();
            String profileImageUrl = userInfoNode.path("response").path("profile_image").asText();

            String filePath = null;
            if (!(profileImageUrl.isEmpty())) {
                Long imageId = imageService.uploadUserProfileImageFromUrl(profileImageUrl);
                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
                filePath = image.getFilePath();
            }
            // 데이터베이스에서 해당 이메일의 사용자 조회
            User findUser = userRepository.findByEmail(email);
            if (findUser == null) {
                // 사용자가 없으면 새로 생성하여 저장
                User user = new User("naver" + id, email, nickname, filePath);
                userRepository.save(user);
            } else {
                if (!findUser.getOauth2Id().equals("naver" + id)) {
                    // 다른 OAuth 제공자로 동일한 이메일을 사용하는 경우 예외 발생
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER);
                }
            }

            // JWT 토큰 생성
            String accessToken = jwtUtil.createJwt("access", email, accessTokenExpiration);
            String refreshToken = jwtUtil.createJwt("refresh", email, refreshTokenExpiration);

            // 토큰 저장
            redisRefreshTokenService.saveAccessToken(email, accessToken, accessTokenExpiration);
            redisRefreshTokenService.saveRefreshToken(email, refreshToken, refreshTokenExpiration);

            // HttpHeaders에 액세스 토큰 설정
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Authorization", "Bearer " + accessToken);

            // 리프레시 토큰 쿠키 생성 및 HttpHeaders에 추가
            ResponseCookie refreshTokenCookie = setResponseCookie(refreshToken);
            responseHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            // 응답 반환
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(SuccessResponse.successWithNoData("로그인 성공"));
        } catch (CustomException e) {
            // DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER 예외일 경우 처리
            if (e.getErrorCode() == ErrorCode.DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER) {
                log.error("DUPLICATE_EMAIL_FOR_DIFFERENT_PROVIDER: {}", e.getMessage(), e);
                throw e; // 그대로 다시 던짐
            }
            // 다른 CustomException도 그대로 던짐
            log.error("CustomException occurred: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.NAVER_LOGIN_PROCESS_FAILED);
        }
    }

    //RefreshToken 쿠키 생성 메서드
    private ResponseCookie setResponseCookie(String refreshToken) {
        return ResponseCookie.from("refresh", refreshToken)
                .maxAge(refreshTokenExpiration / 1000)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .build();
    }
}


