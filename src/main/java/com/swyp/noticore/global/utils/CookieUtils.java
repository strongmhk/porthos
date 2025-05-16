package com.swyp.noticore.global.utils;

import com.swyp.noticore.global.constants.SameSitePolicy;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    public static ResponseCookie createCookie(
        String name,
        String value,
        int maxAgeSeconds,
        boolean httpOnly,
        boolean secure,
        SameSitePolicy sameSitePolicy
    ) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
            .maxAge(maxAgeSeconds)
            .path("/")
            .httpOnly(httpOnly)
            .secure(secure);

        if (sameSitePolicy != null) {
            builder.sameSite(sameSitePolicy.getValue());
        }

        return builder.build();
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
