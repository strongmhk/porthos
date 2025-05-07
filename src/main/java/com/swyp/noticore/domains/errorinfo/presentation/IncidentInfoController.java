package com.swyp.noticore.domains.errorinfo.presentation;

import com.swyp.noticore.domains.errorinfo.application.usecase.IncidentInfoUseCase;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentInfoController {

    private final IncidentInfoUseCase incidentInfoUseCase;

    @PostMapping("/notify")
    public ResponseEntity<String> notify(@RequestBody Map<String, String> payload) {
        CompletableFuture.runAsync(() -> {
            incidentInfoUseCase.processAndForward(payload);
        });
        return ResponseEntity.ok("Accepted");
    }
}
