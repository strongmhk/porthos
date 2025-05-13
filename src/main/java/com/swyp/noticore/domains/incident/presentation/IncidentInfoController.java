package com.swyp.noticore.domains.incident.presentation;

import com.swyp.noticore.domains.auth.application.dto.MemberContext;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentDetailResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentInfoResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentUpdateRequest;
import com.swyp.noticore.domains.incident.application.usecase.IncidentInfoUseCase;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam boolean completion) {
        List<IncidentInfoResponse> incidentInfos = incidentInfoUseCase.getIncidentInfosByCompletion(completion);
        return ResponseEntity.ok(Map.of("incidents", incidentInfos));
    }

    @GetMapping("/{incidentId}")
    public ResponseEntity<IncidentDetailResponse> getIncidentDetail(@PathVariable Long incidentId) {
        return ResponseEntity.ok(incidentInfoUseCase.getIncidentDetail(incidentId));
    }

    @PatchMapping("/{incidentId}")
    public ResponseEntity<String> updateIncident(
        @PathVariable Long incidentId,
        @RequestBody IncidentUpdateRequest request
    ) {
        incidentInfoUseCase.updateIncident(incidentId, request);
        return ResponseEntity.ok("updated");
    }

    @PatchMapping("/verify/{incidentId}")
    public ResponseEntity<String> verifyIncident(@PathVariable Long incidentId, @AuthenticationPrincipal MemberContext memberContext) {
        incidentInfoUseCase.verifyIncident(incidentId, memberContext.memberId());
        return ResponseEntity.ok("verified");
    }

}
