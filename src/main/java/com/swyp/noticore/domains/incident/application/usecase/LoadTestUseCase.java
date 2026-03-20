package com.swyp.noticore.domains.incident.application.usecase;

import com.swyp.noticore.domains.incident.application.event.LoadTestNotificationEvent;
import com.swyp.noticore.domains.incident.domain.service.EmailSender;
import com.swyp.noticore.domains.incident.domain.service.IncidentCommandService;
import com.swyp.noticore.domains.incident.domain.service.OncallSender;
import com.swyp.noticore.domains.incident.domain.service.SlackSender;
import com.swyp.noticore.domains.incident.domain.service.SmsSender;
import com.swyp.noticore.domains.incident.persistence.entity.IncidentGroupEntity;
import com.swyp.noticore.domains.incident.persistence.entity.IncidentInfoEntity;
import com.swyp.noticore.domains.incident.persistence.entity.NotificationLogEntity;
import com.swyp.noticore.domains.incident.persistence.repository.IncidentGroupRepository;
import com.swyp.noticore.domains.incident.persistence.repository.IncidentInfoRepository;
import com.swyp.noticore.domains.incident.persistence.repository.NotificationLogRepository;
import com.swyp.noticore.domains.member.domain.constant.Role;
import com.swyp.noticore.domains.member.persistence.entity.GroupInfoEntity;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import com.swyp.noticore.domains.member.persistence.entity.MemberGroupEntity;
import com.swyp.noticore.domains.member.persistence.entity.MemberMetadataEntity;
import com.swyp.noticore.domains.member.persistence.repository.GroupInfoRepository;
import com.swyp.noticore.domains.member.persistence.repository.MemberGroupRepository;
import com.swyp.noticore.domains.member.persistence.repository.MemberRepository;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Profile("!prod")
@Slf4j
@UseCase
@RequiredArgsConstructor
public class LoadTestUseCase {

    static final String LOAD_TEST_PREFIX = "[LOAD_TEST]";

    private final IncidentCommandService incidentCommandService;
    private final IncidentInfoRepository incidentInfoRepository;
    private final IncidentGroupRepository incidentGroupRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final MemberRepository memberRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final OncallSender oncallSender;
    private final SlackSender slackSender;
    @Qualifier("channelExecutor")
    private final Executor channelExecutor;

    // ============================================================
    // 1단계: 트랜잭션 분리 (DB 커넥션 점유 문제)
    // ============================================================

    /**
     * [1단계 Before] 트랜잭션 내 알림 I/O 처리 — DB 커넥션 점유 문제 재현
     * 응답 시간 ≈ 300 + members × 200 ms (members=3 기본값 → ~900ms)
     */
    @Transactional
    public void runBefore(int memberCount) {
        long delayMs = 300L + memberCount * 200L;
        log.debug("[BEFORE] Saving incident, then holding DB connection for {}ms", delayMs);

        incidentCommandService.saveIncidentAndGroups(
                "load test body",
                LOAD_TEST_PREFIX + " before " + LocalDateTime.now(),
                "load-test-s3-key",
                List.of()
        );

        // 알림 전송 지연 시뮬레이션 — 트랜잭션 내부이므로 DB 커넥션 점유 중
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * [1단계 After] 트랜잭션 커밋 후 비동기 이벤트 기반 알림 처리 — 커넥션 즉시 반환
     * 응답 시간 ≈ DB 저장 시간만 (~50-120ms)
     */
    @Transactional
    public void runAfter(int memberCount) {
        log.debug("[AFTER] Saving incident, then publishing event for async notification");

        incidentCommandService.saveIncidentAndGroups(
                "load test body",
                LOAD_TEST_PREFIX + " after " + LocalDateTime.now(),
                "load-test-s3-key",
                List.of()
        );

        // 트랜잭션 커밋 후 알림 이벤트 발행 — 커넥션 반환 이후에 리스너가 실행됨
        eventPublisher.publishEvent(new LoadTestNotificationEvent(memberCount));
    }

    // ============================================================
    // 2단계: 알림 채널 병렬화 (순차 vs 병렬 채널 실행)
    // ============================================================

    /**
     * [2단계 Before] Email → SMS × n → OnCall × n → Slack 순차 실행
     * 응답 시간 ≈ 300 + members × 150 + members × 150 + 100 ms
     * (members=3 → 300 + 450 + 450 + 100 = 1,300ms)
     */
    public void runNotificationBefore(int memberCount) {
        log.debug("[NOTIFICATION-BEFORE] 순차 알림 전송 시작, members={}", memberCount);

        // Email — 수신자 전체에 1회 발송 (300ms)
        emailSender.sendEmailAlert(null, List.of("mock@test.com"), "[LOAD_TEST] subject", "notice");

        // SMS — 멤버당 1회 (150ms × n)
        for (int i = 0; i < memberCount; i++) {
            smsSender.sendSmsAlert("[LOAD_TEST] subject", "+821012345678");
        }

        // OnCall — 멤버당 1회 (150ms × n)
        for (int i = 0; i < memberCount; i++) {
            oncallSender.triggerOnCall("[LOAD_TEST] subject", "+821012345678");
        }

        // Slack — 채널에 1회 발송 (100ms)
        slackSender.sendSlackAlert(null, "https://mock.slack.webhook");

        log.debug("[NOTIFICATION-BEFORE] 순차 알림 전송 완료");
    }

    /**
     * [2단계 After - 방향A] Email / SMS × n / OnCall × n / Slack 을 CompletableFuture로 병렬 실행 + .join()으로 결과 대기
     * 응답 시간 ≈ max(300ms, members × 150ms, members × 150ms, 100ms)
     * (members=3 → max(300, 450, 450, 100) = 450ms)
     * channelExecutor 스레드가 충분할 때 순차 대비 효과 검증용
     */
    public void runNotificationAfter(int memberCount) {
        log.debug("[NOTIFICATION-AFTER] 병렬 알림 전송 시작, members={}", memberCount);

        CompletableFuture<Void> emailFuture = CompletableFuture.runAsync(
                () -> emailSender.sendEmailAlert(null, List.of("mock@test.com"), "[LOAD_TEST] subject", "notice"),
                channelExecutor
        ).exceptionally(e -> {
            log.error("[NOTIFICATION-AFTER] Email 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture<Void> smsFuture = CompletableFuture.runAsync(
                () -> {
                    for (int i = 0; i < memberCount; i++) {
                        smsSender.sendSmsAlert("[LOAD_TEST] subject", "+821012345678");
                    }
                },
                channelExecutor
        ).exceptionally(e -> {
            log.error("[NOTIFICATION-AFTER] SMS 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture<Void> oncallFuture = CompletableFuture.runAsync(
                () -> {
                    for (int i = 0; i < memberCount; i++) {
                        oncallSender.triggerOnCall("[LOAD_TEST] subject", "+821012345678");
                    }
                },
                channelExecutor
        ).exceptionally(e -> {
            log.error("[NOTIFICATION-AFTER] OnCall 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture<Void> slackFuture = CompletableFuture.runAsync(
                () -> slackSender.sendSlackAlert(null, "https://mock.slack.webhook"),
                channelExecutor
        ).exceptionally(e -> {
            log.error("[NOTIFICATION-AFTER] Slack 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture.allOf(emailFuture, smsFuture, oncallFuture, slackFuture).join();

        log.debug("[NOTIFICATION-AFTER] 병렬 알림 전송 완료");
    }

    /**
     * [2단계 After - 방향B] Email / SMS × n / OnCall × n / Slack 을 CompletableFuture로 병렬 실행 후 즉시 반환 (fire-and-forget)
     * 응답 시간 ≈ task 제출 시간만 (~5ms)
     * 실제 프로덕션 패턴: HTTP 응답은 즉시 반환, 알림은 백그라운드에서 처리
     */
    public void runNotificationAfterAsync(int memberCount) {
        log.debug("[NOTIFICATION-AFTER-ASYNC] fire-and-forget 알림 전송 시작, members={}", memberCount);

        CompletableFuture.runAsync(
                () -> emailSender.sendEmailAlert(null, List.of("mock@test.com"), "[LOAD_TEST] subject", "notice"),
                channelExecutor
        ).exceptionally(e -> {
            log.error("[NOTIFICATION-AFTER-ASYNC] Email 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture.runAsync(
                () -> {
                    for (int i = 0; i < memberCount; i++) {
                        smsSender.sendSmsAlert("[LOAD_TEST] subject", "+821012345678");
                    }
                },
                channelExecutor
        ).exceptionally(e -> {
            log.error("[NOTIFICATION-AFTER-ASYNC] SMS 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture.runAsync(
                () -> {
                    for (int i = 0; i < memberCount; i++) {
                        oncallSender.triggerOnCall("[LOAD_TEST] subject", "+821012345678");
                    }
                },
                channelExecutor
        ).exceptionally(e -> {
            log.error("[NOTIFICATION-AFTER-ASYNC] OnCall 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture.runAsync(
                () -> slackSender.sendSlackAlert(null, "https://mock.slack.webhook"),
                channelExecutor
        ).exceptionally(e -> {
            log.error("[NOTIFICATION-AFTER-ASYNC] Slack 전송 실패: {}", e.getMessage());
            return null;
        });

        log.debug("[NOTIFICATION-AFTER-ASYNC] fire-and-forget 알림 전송 제출 완료");
    }

    // ============================================================
    // 3단계: 장애 중복 등록 방지 (Idempotency)
    // ============================================================

    /**
     * [3단계 Before] 동일 s3_uuid 동시 요청 시 중복 등록 문제 재현
     * unique constraint 없는 상태: concurrency개 모두 성공 → DB에 중복 row 생성
     * unique constraint 있는 상태: 1개만 성공, 나머지는 errorCount로 집계
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> runIdempotencyBefore(int concurrency) {
        long start = System.currentTimeMillis();

        CountDownLatch readyLatch = new CountDownLatch(concurrency);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                // 스레드마다 고유한 UUID 생성 — unique constraint 없던 시절의 동작 재현
                // Lambda 재전달마다 새 row가 생성되는 문제를 시뮬레이션
                String ownUuid = "load-test-idempotency-" + UUID.randomUUID();
                readyLatch.countDown();
                try { startLatch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                try {
                    incidentCommandService.saveIncidentAndGroups(
                            "idempotency test body",
                            LOAD_TEST_PREFIX + " idempotency-before " + ownUuid,
                            ownUuid,
                            List.of()
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.warn("[IDEMPOTENCY-BEFORE] Save failed: {}", e.getMessage());
                }
            }, channelExecutor));
        }

        try { readyLatch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        startLatch.countDown();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // successCount = DB에 실제 생성된 row 수 (각 스레드가 다른 UUID 사용)
        long dbRowCount = successCount.get();
        long durationMs = System.currentTimeMillis() - start;

        log.info("[IDEMPOTENCY-BEFORE] concurrency={}, success={}, error={}, dbRows={}",
                concurrency, successCount.get(), errorCount.get(), dbRowCount);

        return Map.of(
                "scenario", "3단계-before",
                "concurrency", concurrency,
                "successCount", successCount.get(),
                "duplicateCount", 0,
                "errorCount", errorCount.get(),
                "actualDbRowCount", dbRowCount,
                "durationMs", durationMs
        );
    }

    /**
     * [3단계 After] unique constraint + DataIntegrityViolationException 처리로 중복 방지
     * 동일 s3_uuid로 concurrency개 동시 저장 → 1개만 성공, 나머지는 duplicate 처리
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> runIdempotencyAfter(int concurrency) {
        long start = System.currentTimeMillis();
        String testUuid = "load-test-idempotency-" + UUID.randomUUID();

        CountDownLatch readyLatch = new CountDownLatch(concurrency);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                readyLatch.countDown();
                try { startLatch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                try {
                    incidentCommandService.saveIncidentAndGroups(
                            "idempotency test body",
                            LOAD_TEST_PREFIX + " idempotency-after",
                            testUuid,
                            List.of()
                    );
                    successCount.incrementAndGet();
                } catch (DataIntegrityViolationException e) {
                    duplicateCount.incrementAndGet();
                    log.info("[IDEMPOTENCY-AFTER] Duplicate caught for s3_uuid={}", testUuid);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("[IDEMPOTENCY-AFTER] Unexpected error: {}", e.getMessage());
                }
            }, channelExecutor));
        }

        try { readyLatch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        startLatch.countDown();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long dbRowCount = incidentInfoRepository.countByS3Uuid(testUuid);
        long durationMs = System.currentTimeMillis() - start;

        log.info("[IDEMPOTENCY-AFTER] concurrency={}, success={}, duplicate={}, error={}, dbRows={}",
                concurrency, successCount.get(), duplicateCount.get(), errorCount.get(), dbRowCount);

        return Map.of(
                "scenario", "3단계-after",
                "testS3Uuid", testUuid,
                "concurrency", concurrency,
                "successCount", successCount.get(),
                "duplicateCount", duplicateCount.get(),
                "errorCount", errorCount.get(),
                "actualDbRowCount", dbRowCount,
                "durationMs", durationMs
        );
    }

    // ============================================================
    // 4단계: 장애 목록 조회 성능 개선 (인덱스 + N+1 해결)
    // ============================================================

    /**
     * [4단계 Setup] 테스트 데이터 시딩
     * 그룹, 멤버, 장애, notification_log를 대량 생성한다.
     */
    @Transactional
    public Map<String, Object> setupQueryTestData(int incidentCount, int groupCount, int memberPerGroup) {
        long start = System.currentTimeMillis();

        // 1. 그룹 생성
        List<GroupInfoEntity> groups = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            GroupInfoEntity group = GroupInfoEntity.builder()
                    .name(LOAD_TEST_PREFIX + " group-" + i)
                    .build();
            groups.add(groupInfoRepository.save(group));
        }

        // 2. 멤버 + 메타데이터 + 멤버그룹 생성
        List<MemberEntity> allMembers = new ArrayList<>();
        int memberIndex = 0;
        for (GroupInfoEntity group : groups) {
            for (int j = 0; j < memberPerGroup; j++) {
                MemberMetadataEntity metadata = MemberMetadataEntity.builder()
                        .slackUrl("https://mock.slack/" + memberIndex)
                        .slackNoti(true)
                        .smsNoti(true)
                        .oncallNoti(true)
                        .build();

                MemberEntity member = MemberEntity.builder()
                        .memberMetadata(metadata)
                        .role(Role.USER)
                        .email(LOAD_TEST_PREFIX + "-" + memberIndex + "@test.com")
                        .password("test1234")
                        .name(LOAD_TEST_PREFIX + " member-" + memberIndex)
                        .phone("+8210" + String.format("%08d", memberIndex))
                        .build();
                member = memberRepository.save(member);
                allMembers.add(member);

                MemberGroupEntity memberGroup = MemberGroupEntity.builder()
                        .member(member)
                        .groupInfo(group)
                        .build();
                memberGroupRepository.save(memberGroup);

                memberIndex++;
            }
        }

        // 3. 장애 + 장애그룹 + notification_log 생성 (배치 저장)
        Long sampleIncidentId = null;
        Long sampleMemberId = allMembers.isEmpty() ? null : allMembers.get(0).getId();
        int totalNotificationLogs = 0;

        int batchSize = 500;
        List<IncidentGroupEntity> incidentGroupBatch = new ArrayList<>();
        List<NotificationLogEntity> notificationLogBatch = new ArrayList<>();

        for (int i = 0; i < incidentCount; i++) {
            IncidentInfoEntity incident = IncidentInfoEntity.builder()
                    .s3Uuid("load-test-query-" + UUID.randomUUID())
                    .rawBody("load test body")
                    .title(LOAD_TEST_PREFIX + " incident-" + i)
                    .completion(false)
                    .registrationTime(LocalDateTime.now())
                    .build();
            incident = incidentInfoRepository.save(incident);

            if (sampleIncidentId == null) {
                sampleIncidentId = incident.getId();
            }

            for (GroupInfoEntity group : groups) {
                incidentGroupBatch.add(IncidentGroupEntity.builder()
                        .incident(incident)
                        .groupInfo(group)
                        .build());
            }

            for (MemberEntity member : allMembers) {
                notificationLogBatch.add(NotificationLogEntity.builder()
                        .incident(incident)
                        .member(member)
                        .isVerified(false)
                        .retryCount(0)
                        .build());
                totalNotificationLogs++;
            }

            // 배치 크기에 도달하면 flush
            if (notificationLogBatch.size() >= batchSize) {
                incidentGroupRepository.saveAll(incidentGroupBatch);
                notificationLogRepository.saveAll(notificationLogBatch);
                incidentGroupBatch.clear();
                notificationLogBatch.clear();
            }
        }

        // 남은 데이터 flush
        if (!incidentGroupBatch.isEmpty()) {
            incidentGroupRepository.saveAll(incidentGroupBatch);
        }
        if (!notificationLogBatch.isEmpty()) {
            notificationLogRepository.saveAll(notificationLogBatch);
        }

        long durationMs = System.currentTimeMillis() - start;
        log.info("[QUERY-SETUP] incidents={}, groups={}, members={}, notificationLogs={}, duration={}ms",
                incidentCount, groupCount, allMembers.size(), totalNotificationLogs, durationMs);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "4단계-setup");
        result.put("incidentCount", incidentCount);
        result.put("groupCount", groupCount);
        result.put("memberPerGroup", memberPerGroup);
        result.put("totalMembers", allMembers.size());
        result.put("totalNotificationLogs", totalNotificationLogs);
        result.put("sampleIncidentId", sampleIncidentId);
        result.put("sampleMemberId", sampleMemberId);
        result.put("durationMs", durationMs);
        return result;
    }

    /**
     * [4단계] notification_log verify 쿼리 성능 측정
     * findByIncidentIdAndMemberId를 iterations회 반복 호출하여 평균 시간을 측정한다.
     * 인덱스 유무에 따라 성능 차이가 발생한다.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> runVerifyQuery(String scenario, Long incidentId, Long memberId, int iterations) {
        // warm-up
        notificationLogRepository.findByIncidentIdAndMemberId(incidentId, memberId);

        long start = System.nanoTime();
        boolean found = false;
        for (int i = 0; i < iterations; i++) {
            found = notificationLogRepository.findByIncidentIdAndMemberId(incidentId, memberId).isPresent();
        }
        long totalNs = System.nanoTime() - start;
        double totalMs = totalNs / 1_000_000.0;
        double avgMs = totalMs / iterations;

        log.info("[{}] incidentId={}, memberId={}, iterations={}, totalMs={}, avgMs={}, found={}",
                scenario, incidentId, memberId, iterations, String.format("%.2f", totalMs), String.format("%.4f", avgMs), found);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", scenario);
        result.put("incidentId", incidentId);
        result.put("memberId", memberId);
        result.put("iterations", iterations);
        result.put("totalMs", Math.round(totalMs * 100.0) / 100.0);
        result.put("avgMs", Math.round(avgMs * 10000.0) / 10000.0);
        result.put("found", found);
        return result;
    }

    /**
     * [4단계 Before] N+1 쿼리 시뮬레이션 — 장애별로 개별 쿼리 실행
     * 장애 목록 → 각 장애의 그룹 → 각 그룹의 멤버 → 각 멤버의 notification_log 순서로 개별 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> runListBefore(int iterations) {
        long totalMs = 0;
        int queryCount = 0;
        int incidentCount = 0;

        for (int iter = 0; iter < iterations; iter++) {
            long start = System.nanoTime();
            int iterQueryCount = 0;

            // 1. 장애 목록 조회 (1회)
            List<IncidentInfoEntity> incidents = incidentInfoRepository.findByTitleStartingWith(LOAD_TEST_PREFIX);
            iterQueryCount++;
            incidentCount = incidents.size();

            for (IncidentInfoEntity incident : incidents) {
                // 2. 각 장애의 그룹 조회 (N회)
                List<IncidentGroupEntity> incidentGroups = incidentGroupRepository.findByIncidentId(incident.getId());
                iterQueryCount++;

                for (IncidentGroupEntity ig : incidentGroups) {
                    // 3. 각 그룹의 멤버 조회 (N×G회)
                    List<MemberGroupEntity> memberGroups = memberGroupRepository.findByGroupInfoId(ig.getGroupInfo().getId());
                    iterQueryCount++;

                    for (MemberGroupEntity mg : memberGroups) {
                        // 4. 각 멤버의 알림 확인 여부 조회 (N×G×M회)
                        notificationLogRepository.findByIncidentIdAndMemberId(incident.getId(), mg.getMember().getId());
                        iterQueryCount++;
                    }
                }
            }

            totalMs += (System.nanoTime() - start) / 1_000_000;
            queryCount = iterQueryCount;
        }

        double avgMs = (double) totalMs / iterations;

        log.info("[LIST-BEFORE] incidents={}, queryCount={}, totalMs={}, avgMs={}",
                incidentCount, queryCount, totalMs, String.format("%.2f", avgMs));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "4단계-list-before");
        result.put("incidentCount", incidentCount);
        result.put("queryCount", queryCount);
        result.put("iterations", iterations);
        result.put("totalMs", totalMs);
        result.put("avgMs", Math.round(avgMs * 100.0) / 100.0);
        return result;
    }

    /**
     * [4단계 After] QueryDSL 단일 조인 쿼리 — 장애 목록 + 그룹 + 멤버 + 알림 확인 여부를 1회 쿼리로 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> runListAfter(int iterations) {
        long totalMs = 0;
        int incidentCount = 0;

        for (int iter = 0; iter < iterations; iter++) {
            long start = System.nanoTime();

            var results = incidentInfoRepository.findIncidentInfosByCompletion(false);
            incidentCount = results.size();

            totalMs += (System.nanoTime() - start) / 1_000_000;
        }

        double avgMs = (double) totalMs / iterations;

        log.info("[LIST-AFTER] incidents={}, queryCount=1, totalMs={}, avgMs={}",
                incidentCount, totalMs, String.format("%.2f", avgMs));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "4단계-list-after");
        result.put("incidentCount", incidentCount);
        result.put("queryCount", 1);
        result.put("iterations", iterations);
        result.put("totalMs", totalMs);
        result.put("avgMs", Math.round(avgMs * 100.0) / 100.0);
        return result;
    }

    /**
     * [4단계] 쿼리 성능 테스트 데이터 정리
     */
    @Transactional
    public Map<String, Object> cleanupQueryTestData() {
        // 1. 로드테스트 장애 ID 조회
        List<IncidentInfoEntity> testIncidents = incidentInfoRepository.findByTitleStartingWith(LOAD_TEST_PREFIX);
        List<Long> incidentIds = testIncidents.stream().map(IncidentInfoEntity::getId).toList();

        // 2. notification_log 삭제
        int deletedLogs = 0;
        if (!incidentIds.isEmpty()) {
            notificationLogRepository.deleteByIncidentIdIn(incidentIds);
            deletedLogs = incidentIds.size(); // 근사치
        }

        // 3. 장애 삭제 (incident_group은 cascade로 삭제됨)
        incidentInfoRepository.deleteAllByIdInBatch(incidentIds);

        // 4. 로드테스트 멤버 삭제 (member_group은 cascade로 삭제됨)
        List<MemberEntity> testMembers = memberRepository.findByNameStartingWith(LOAD_TEST_PREFIX);
        List<Long> memberIds = testMembers.stream().map(MemberEntity::getId).toList();
        memberRepository.deleteAllByIdInBatch(memberIds);

        // 5. 로드테스트 그룹 삭제
        List<GroupInfoEntity> testGroups = groupInfoRepository.findByNameStartingWith(LOAD_TEST_PREFIX);
        List<Long> groupIds = testGroups.stream().map(GroupInfoEntity::getId).toList();
        groupInfoRepository.deleteAllByIdInBatch(groupIds);

        log.info("[QUERY-CLEANUP] incidents={}, members={}, groups={}",
                incidentIds.size(), memberIds.size(), groupIds.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deletedIncidents", incidentIds.size());
        result.put("deletedMembers", memberIds.size());
        result.put("deletedGroups", groupIds.size());
        return result;
    }

    // ============================================================
    // 공통
    // ============================================================

    /**
     * 부하 테스트로 생성된 더미 데이터를 일괄 삭제한다.
     */
    @Transactional
    public int cleanup() {
        List<Long> ids = incidentInfoRepository.findByTitleStartingWith(LOAD_TEST_PREFIX)
                .stream().map(e -> e.getId()).toList();
        incidentInfoRepository.deleteAllByIdInBatch(ids);
        log.info("[LOAD_TEST] Cleaned up {} test incidents", ids.size());
        return ids.size();
    }
}
