package com.swyp.noticore.global.config.security.jwt;

import static com.swyp.noticore.domains.auth.exception.AuthErrorCode.INVALID_TOKEN;
import static com.swyp.noticore.domains.auth.exception.AuthErrorCode.TOKEN_EXPIRED;
import static com.swyp.noticore.domains.auth.exception.AuthErrorCode.TOKEN_NOT_FOUND;
import static com.swyp.noticore.global.config.security.jwt.constant.TokenType.REFRESH;
import static com.swyp.noticore.global.response.code.CommonErrorCode.UNAUTHORIZED;

import com.swyp.noticore.domains.auth.application.dto.MemberContext;
import com.swyp.noticore.global.config.security.auth.CustomUserDetails;
import com.swyp.noticore.global.config.security.auth.CustomUserDetailsService;
import com.swyp.noticore.global.config.security.jwt.constant.TokenType;
import com.swyp.noticore.global.exception.ApplicationException;
import com.swyp.noticore.global.response.code.BaseErrorCode;
import com.swyp.noticore.infrastructure.redis.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {

    private final CustomUserDetailsService customUserDetailsService;
    private final RedisService redisService;

    @Value("${jwt.secret-key}")
    private String secret;

    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";
    private static final String ROLE = "role";
    private static final String TYPE = "type";

    public void validateAccessToken(HttpServletResponse response, String accessToken, TokenType tokenType) {
        try {
            Claims claims = parseClaims(accessToken);
            if (!claims.get(TYPE).equals(tokenType.name())) {
                throw ApplicationException.from(INVALID_TOKEN);
            }
        } catch (ExpiredJwtException e) {
            jwtExceptionHandler(response, TOKEN_EXPIRED);
            throw ApplicationException.from(TOKEN_EXPIRED);
        } catch (Exception e) {
            jwtExceptionHandler(response, INVALID_TOKEN);
            throw ApplicationException.from(INVALID_TOKEN);
        }
    }

    public void validateRefreshToken(Long memberId, String requestToken) {
        String redisToken = redisService.getValues(REFRESH.toString() + memberId)
            .orElseThrow(() -> ApplicationException.from(TOKEN_NOT_FOUND));

        if (!redisToken.equals(requestToken)) {
            throw ApplicationException.from(INVALID_TOKEN);
        }
    }

    public Authentication getAuthentication(HttpServletResponse response, String token) throws ApplicationException {
        Claims claims = parseClaims(token);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(claims.get(ROLE).toString()));
        MemberContext memberContext = loadUserDetailsFromClaims(response, claims).getMemberContext();

        return new UsernamePasswordAuthenticationToken(memberContext, "", authorities);
    }

    public void saveAuthentication(HttpServletResponse response, String token) {
        Authentication authentication = getAuthentication(response, token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("context 인증 정보 저장 : {}", authentication.getName());
    }

    public Long getMemberIdFromToken(HttpServletResponse response, String token, TokenType tokenType) {
        try {
            Claims claims = parseClaims(token);

            if (!claims.get(TYPE).equals(tokenType.name())) {
                throw ApplicationException.from(INVALID_TOKEN);
            }

            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            jwtExceptionHandler(response, TOKEN_EXPIRED);
            throw ApplicationException.from(TOKEN_EXPIRED);
        } catch (Exception e) {
            jwtExceptionHandler(response, INVALID_TOKEN);
            throw ApplicationException.from(INVALID_TOKEN);
        }
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private CustomUserDetails loadUserDetailsFromClaims(HttpServletResponse response, Claims claims) {
        try {
            return customUserDetailsService.loadUserByUsername(claims.getSubject());
        } catch (ApplicationException e) {
            jwtExceptionHandler(response, UNAUTHORIZED);
            throw e;
        }
    }

    private void jwtExceptionHandler(HttpServletResponse response, BaseErrorCode error) {
        response.setStatus(error.getHttpStatus().value());

        log.error("errorCode {}, errorMessage {}", error.getCustomCode(), error.getMessage());
    }
}
