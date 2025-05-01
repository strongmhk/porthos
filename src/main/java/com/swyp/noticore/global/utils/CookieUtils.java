package com.swyp.noticore.global.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    public static Cookie createCookie(String key, String value, int maxAgeSec, boolean isHttpOnly) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAgeSec);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(isHttpOnly);

        return cookie;
    }

    public static String extractFromCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> targetCookie = Arrays.stream(cookies)
                .filter(cookie -> key.equals(cookie.getName()))
                .findFirst();

            if (targetCookie.isPresent()) {
                return targetCookie.get().getValue();
            }
        }
        return null;
    }
}
