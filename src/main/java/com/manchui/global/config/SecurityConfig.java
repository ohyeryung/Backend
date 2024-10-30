package com.manchui.global.config;

import com.manchui.domain.service.RedisRefreshTokenService;
import com.manchui.global.jwt.CustomLogoutFilter;
import com.manchui.global.jwt.JWTFilter;
import com.manchui.global.jwt.JWTUtil;
import com.manchui.global.jwt.LoginFilter;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final Validator validator;
    private final RedisRefreshTokenService redisRefreshTokenService;
    @Value("${token.access.expiration}")
    private Long accessTokenExpiration;

    @Value("${token.refresh.expiration}")
    private Long refreshTokenExpiration;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //csrf disable
        http
                .csrf((auth) -> auth.disable());

        //Form 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //http basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/api/auths/signup",
                                "/api/auths/signin",
                                "/api/auths/check-name",
                                "/api/auths/check-email",
                                "/api/auths/reissue",
                                "/login",

                                // swagger 관련 API 문서 경로
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",

                                // 비회원 조회 경로
                                "/api/gathering/public/**").permitAll()
                        .anyRequest().authenticated()
                );
        //로그인 필터 적용
        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, validator
                        , redisRefreshTokenService, accessTokenExpiration, refreshTokenExpiration), UsernamePasswordAuthenticationFilter.class);

        //JWT필터 적용
        http
                .addFilterAfter(new JWTFilter(jwtUtil), LoginFilter.class);

        //커스텀 로그아웃 필터 적용
        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, redisRefreshTokenService), LogoutFilter.class);

        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
