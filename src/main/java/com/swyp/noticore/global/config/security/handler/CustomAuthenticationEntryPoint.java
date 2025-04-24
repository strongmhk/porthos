package com.swyp.noticore.global.config.security.handler;

import static com.swyp.noticore.domains.auth.exception.AuthErrorCode.AUTHENTICATION_FAILED;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.noticore.global.response.ApplicationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        log.error("AuthenticationEntryPoint : {} {}", AUTHENTICATION_FAILED.getMessage(), request.getRequestURI());

        objectMapper.writeValue(
            response.getOutputStream(),
            ApplicationResponse.onFailure(AUTHENTICATION_FAILED.getCustomCode(), AUTHENTICATION_FAILED.getMessage())
        );
    }
}