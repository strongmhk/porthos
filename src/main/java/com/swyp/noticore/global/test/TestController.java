package com.swyp.noticore.global.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("Backend Conn Success ~ !\nCurrent Backend IP: %s\nCurrent Time: %s", clientIp, now);
    }

    @PostMapping("/notify")
    public ResponseEntity<String> notify(@RequestBody Map<String, Object> payload) {
        System.out.println("====== NOTIFY REQUEST ======");
        payload.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("============================");

        return ResponseEntity.ok("Notification received successfully");
    }
}
