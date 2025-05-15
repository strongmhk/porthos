package com.swyp.noticore.global.utils;

import com.swyp.noticore.global.constants.SameSitePolicy;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    public static String addCookie(
        String name,
        String value,
        int maxAgeSeconds,
        boolean httpOnly,
        boolean secure,
        SameSitePolicy sameSitePolicy
    ) {
        StringBuilder cookieBuilder = new StringBuilder();
        cookieBuilder.append(name).append("=").append(value)
            .append("; Max-Age=").append(maxAgeSeconds)
            .append("; Path=/");

        if (secure) {
            cookieBuilder.append("; Secure");
        }
        if (httpOnly) {
            cookieBuilder.append("; HttpOnly");
        }

        if (sameSitePolicy != null) {
            cookieBuilder.append("; SameSite=").append(sameSitePolicy.getValue());
        }

        return cookieBuilder.toString();
    }

    public static void deleteCookie(HttpServletResponse response, String key) {
        Cookie cookie = new Cookie(key, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
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
