package com.swyp.noticore.domains.errorinfo.presentation;

import com.swyp.noticore.domains.errorinfo.application.usecase.ErrorInfoUseCase;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/error-info")
@RequiredArgsConstructor
public class ErrorInfoController {

    private final ErrorInfoUseCase errorInfoUseCase;

    @PostMapping("/notify")
    public ResponseEntity<String> notify(@RequestBody Map<String, String> payload) {
        CompletableFuture.runAsync(() -> {
            errorInfoUseCase.processAndForward(payload);
        });
        return ResponseEntity.ok("Accepted");
    }
}
