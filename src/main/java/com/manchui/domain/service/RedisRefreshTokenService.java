package com.manchui.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    //Refresh 토큰 저장
    public void saveRefreshToken(String userEmail, String refreshToken, Long expiredMs) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(userEmail, refreshToken, expiredMs, TimeUnit.MILLISECONDS);
    }

    //Refresh 토큰 삭제
    public void deleteRefreshToken(String userEmail) {
        redisTemplate.delete(userEmail);
    }

    //Refresh 토큰 존재 여부 확인
    public Boolean existsByRefreshToken(String userEmail) {
        return redisTemplate.hasKey(userEmail);
    }
}
