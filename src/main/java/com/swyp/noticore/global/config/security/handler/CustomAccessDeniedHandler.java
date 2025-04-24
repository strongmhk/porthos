package com.swyp.noticore.global.config.security.handler;

import static com.swyp.noticore.domains.member.exception.MemberErrorCode.PERMISSION_DENIED;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.noticore.global.response.ApplicationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        log.error("AccessDeniedHandler : {} {}", PERMISSION_DENIED.getMessage(), request.getRequestURI());

        objectMapper.writeValue(
            response.getOutputStream(),
            ApplicationResponse.onFailure(PERMISSION_DENIED.getCustomCode(), PERMISSION_DENIED.getMessage())
        );
    }
}
