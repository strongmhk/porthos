package com.swyp.noticore.global.config.security.jwt;

import static com.swyp.noticore.global.config.security.jwt.constant.TokenType.ACCESS;
import static com.swyp.noticore.global.config.security.jwt.constant.TokenType.REFRESH;

import com.swyp.noticore.domains.member.domain.constant.Role;
import com.swyp.noticore.infrastructure.redis.RedisService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    private final RedisService redisService;

    @Value("${jwt.secret-key}")
    private String secret;

    @Value("${jwt.refresh-expiration}")
    private int refreshExpiration;

    @Value("${jwt.access-expiration}")
    private int accessExpiration;

    private static final String ROLE = "role";
    private static final String TYPE = "type";

    public String generateAccessToken(Long memberId, Role role) {
        SecretKey key = getSecretKey();
        Instant accessDate = getExpiration(accessExpiration);

        return Jwts.builder()
            .claim(ROLE, role)
            .claim(TYPE, ACCESS)
            .setSubject(String.valueOf(memberId))
            .setExpiration(Date.from(accessDate))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateRefreshToken(Long memberId, Role role) {
        redisService.deleteValues(REFRESH.toString() + memberId);

        SecretKey key = getSecretKey();
        Instant refreshDate = getExpiration(refreshExpiration);

        String refreshToken = Jwts.builder()
            .claim(ROLE, role)
            .claim(TYPE, REFRESH)
            .setSubject(String.valueOf(memberId))
            .setExpiration(Date.from(refreshDate))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        redisService.setValues(REFRESH.toString() + memberId, refreshToken, Duration.ofSeconds(refreshExpiration));

        return refreshToken;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    private Instant getExpiration(long expiration) {
        return LocalDateTime.now().plusSeconds(expiration).atZone(ZoneId.systemDefault()).toInstant();
    }
}
