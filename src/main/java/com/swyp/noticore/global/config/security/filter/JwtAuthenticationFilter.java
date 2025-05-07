package com.swyp.noticore.global.config.security.filter;

import static com.swyp.noticore.domains.auth.domain.constants.AuthConstants.ACCESS_COOKIE_KEY;
import static com.swyp.noticore.domains.auth.domain.constants.AuthConstants.REFRESH_COOKIE_KEY;
import static com.swyp.noticore.global.config.security.jwt.constant.TokenType.ACCESS;
import static com.swyp.noticore.global.config.security.jwt.constant.TokenType.REFRESH;

import com.swyp.noticore.global.config.security.jwt.JwtUtils;
import com.swyp.noticore.global.config.security.matcher.RequestMatcherHolder;
import com.swyp.noticore.global.utils.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RequestMatcherHolder requestMatcherHolder;

    private static final String REFRESH_URL = "/api/auth/refresh";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().equals(REFRESH_URL)) {
            String refreshToken = CookieUtils.extractFromCookie(request, REFRESH_COOKIE_KEY);
            log.info("재발급 진행, 토큰 = {}", refreshToken);

            Long memberId = jwtUtils.getMemberIdFromToken(response, refreshToken, REFRESH);

            jwtUtils.validateRefreshToken(memberId, refreshToken);
            jwtUtils.saveAuthentication(response, refreshToken);
        } else {
            String accessToken = CookieUtils.extractFromCookie(request, ACCESS_COOKIE_KEY);
            log.info("일반 접근, 토큰 = {}", accessToken);

            jwtUtils.validateAccessToken(response, accessToken, ACCESS);
            jwtUtils.saveAuthentication(response, accessToken);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        log.info("Request URI = {}", request.getRequestURI());
        return requestMatcherHolder.getRequestMatchersByMinPermission(null).matches(request);
    }
}