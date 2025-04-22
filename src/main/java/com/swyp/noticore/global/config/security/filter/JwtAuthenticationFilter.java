package com.swyp.noticore.global.config.security.filter;

import static com.swyp.noticore.global.config.security.jwt.constant.TokenType.ACCESS;
import static com.swyp.noticore.global.config.security.jwt.constant.TokenType.REFRESH;

import com.swyp.noticore.global.config.security.jwt.JwtUtils;
import com.swyp.noticore.global.exception.ApplicationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private static final String ACCESS_KEY = "accessToken";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractFromCookie(request, ACCESS_KEY);

        if (token != null) {
            log.info("토큰 함께 요청 : {}", token);
            try {
                if (request.getRequestURI().contains("/refresh")) {
                    log.info("재발급 진행");
                    jwtUtils.validateToken(response, token, REFRESH);
                } else {
                    log.info("일반 접근");
                    jwtUtils.validateToken(response, token, ACCESS);
                }

                Authentication authentication = jwtUtils.getAuthentication(response, token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("context 인증 정보 저장 : {}", authentication.getName());
            } catch (ApplicationException e) {
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractFromCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> accessTokenCookie = Arrays.stream(cookies)
                .filter(cookie -> key.equals(cookie.getName()))
                .findFirst();

            if (accessTokenCookie.isPresent()) {
                return accessTokenCookie.get().getValue();
            }
        }
        return null;
    }
}