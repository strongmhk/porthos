package com.swyp.noticore.domains.incident.presentation;

import com.swyp.noticore.domains.incident.application.usecase.LoadTestUseCase;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/load-test")
@RequiredArgsConstructor
public class LoadTestController {

    private final LoadTestUseCase loadTestUseCase;

    /**
     * [Before] 트랜잭션 내 알림 I/O 처리 — DB 커넥션 점유 문제 재현
     * 응답 시간 ≈ 300 + members × 200 ms (members=3 기본값 → ~900ms)
     */
    @PostMapping("/before")
    public ResponseEntity<Map<String, Object>> before(
            @RequestParam(defaultValue = "3") int members) {
        long start = System.currentTimeMillis();
        loadTestUseCase.runBefore(members);
        return ResponseEntity.ok(Map.of(
                "scenario", "before",
                "members", members,
                "durationMs", System.currentTimeMillis() - start
        ));
    }

    /**
     * [After] 트랜잭션 커밋 후 비동기 이벤트 기반 알림 처리 — 커넥션 즉시 반환
     * 응답 시간 ≈ DB 저장 시간만 (~50-120ms)
     */
    @PostMapping("/after")
    public ResponseEntity<Map<String, Object>> after(
            @RequestParam(defaultValue = "3") int members) {
        long start = System.currentTimeMillis();
        loadTestUseCase.runAfter(members);
        return ResponseEntity.ok(Map.of(
                "scenario", "after",
                "members", members,
                "durationMs", System.currentTimeMillis() - start
        ));
    }

    /**
     * 부하 테스트로 생성된 더미 데이터 삭제
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanup() {
        int deleted = loadTestUseCase.cleanup();
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }
}
