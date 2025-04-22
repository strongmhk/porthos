package com.swyp.noticore.global.utils;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    public static Cookie createCookie(String key, String value, int maxAgeSec, boolean isHttpOnly) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAgeSec);
        /*cookie.setSecure();*/
        cookie.setPath("/");
        cookie.setHttpOnly(isHttpOnly);

        return cookie;
    }
}
