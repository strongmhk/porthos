package com.swyp.noticore.global.test;

import com.swyp.noticore.domains.errorinfo.application.usecase.ErrorInfoUseCase;
import com.swyp.noticore.infrastructure.slack.SlackService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final ErrorInfoUseCase errorInfoUseCase;
    private final SlackService slackService;

    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("Backend Conn Success ~ !\nCurrent Backend IP: %s\nCurrent Time: %s", clientIp, now);
    }

    @PostMapping("/notify")
    public ResponseEntity<String> notify(@RequestBody Map<String, String> payload) {
        CompletableFuture.runAsync(() -> {
            try {
                errorInfoUseCase.processAndForward(payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ResponseEntity.ok("Accepted");
    }

    @GetMapping("/test/slack")
    public ResponseEntity<String> sendSlackTextMessage() throws Exception {
        slackService.sendErrorNotification();
        return ResponseEntity.ok("Error Message is successfully sent to Slack.");
    }
}

