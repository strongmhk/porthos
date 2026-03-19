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

    // ============================================================
    // 1단계: 트랜잭션 분리 (DB 커넥션 점유 문제)
    // ============================================================

    /**
     * [1단계 Before] 트랜잭션 내 알림 I/O 처리 — DB 커넥션 점유 문제 재현
     * 응답 시간 ≈ 300 + members × 200 ms (members=3 → ~900ms)
     */
    @PostMapping("/before")
    public ResponseEntity<Map<String, Object>> before(
            @RequestParam(defaultValue = "3") int members) {
        long start = System.currentTimeMillis();
        loadTestUseCase.runBefore(members);
        return ResponseEntity.ok(Map.of(
                "scenario", "1단계-before",
                "members", members,
                "durationMs", System.currentTimeMillis() - start
        ));
    }

    /**
     * [1단계 After] 트랜잭션 커밋 후 비동기 이벤트 기반 알림 처리 — 커넥션 즉시 반환
     * 응답 시간 ≈ DB 저장 시간만 (~50-120ms)
     */
    @PostMapping("/after")
    public ResponseEntity<Map<String, Object>> after(
            @RequestParam(defaultValue = "3") int members) {
        long start = System.currentTimeMillis();
        loadTestUseCase.runAfter(members);
        return ResponseEntity.ok(Map.of(
                "scenario", "1단계-after",
                "members", members,
                "durationMs", System.currentTimeMillis() - start
        ));
    }

    // ============================================================
    // 2단계: 알림 채널 병렬화 (순차 vs 병렬 채널 실행)
    // ============================================================

    /**
     * [2단계 Before] Email → SMS × n → OnCall × n 순차 실행
     * 응답 시간 ≈ 300 + members × 150 + members × 150 ms
     * (members=3 → ~1,200ms)
     */
    @PostMapping("/notification/before")
    public ResponseEntity<Map<String, Object>> notificationBefore(
            @RequestParam(defaultValue = "3") int members) {
        long start = System.currentTimeMillis();
        loadTestUseCase.runNotificationBefore(members);
        return ResponseEntity.ok(Map.of(
                "scenario", "2단계-before",
                "members", members,
                "durationMs", System.currentTimeMillis() - start
        ));
    }

    /**
     * [2단계 After - 방향A] Email / SMS × n / OnCall × n / Slack 병렬 실행 + .join() 대기
     * 응답 시간 ≈ max(300ms, members × 150ms, members × 150ms, 100ms)
     * (members=3 → ~450ms) — channelExecutor 스레드 충분 시
     */
    @PostMapping("/notification/after")
    public ResponseEntity<Map<String, Object>> notificationAfter(
            @RequestParam(defaultValue = "3") int members) {
        long start = System.currentTimeMillis();
        loadTestUseCase.runNotificationAfter(members);
        return ResponseEntity.ok(Map.of(
                "scenario", "2단계-after-A",
                "members", members,
                "durationMs", System.currentTimeMillis() - start
        ));
    }

    /**
     * [2단계 After - 방향B] Email / SMS × n / OnCall × n / Slack 병렬 실행 후 즉시 반환 (fire-and-forget)
     * 응답 시간 ≈ task 제출 시간만 (~5ms)
     */
    @PostMapping("/notification/after-async")
    public ResponseEntity<Map<String, Object>> notificationAfterAsync(
            @RequestParam(defaultValue = "3") int members) {
        long start = System.currentTimeMillis();
        loadTestUseCase.runNotificationAfterAsync(members);
        return ResponseEntity.ok(Map.of(
                "scenario", "2단계-after-B",
                "members", members,
                "durationMs", System.currentTimeMillis() - start
        ));
    }

    // ============================================================
    // 공통
    // ============================================================

    /**
     * 부하 테스트로 생성된 더미 데이터 삭제
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanup() {
        int deleted = loadTestUseCase.cleanup();
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }
}
