package com.swyp.noticore.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisRepository redisRepository;

    public void setValues(String key, String data, Duration duration) {
        redisRepository.setValues(key, data, duration);
    }

    public void setValuesWithoutTTL(String key, String data) {
        redisRepository.setValuesWithoutTTL(key, data);
    }

    public Optional<String> getValues(String key) {
        return redisRepository.getValues(key);
    }

    public void deleteValues(String key) {
        redisRepository.deleteValues(key);
    }
}