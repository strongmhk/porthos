package com.swyp.noticore.domains.incident.application.usecase;

import com.swyp.noticore.domains.incident.application.event.LoadTestNotificationEvent;
import com.swyp.noticore.domains.incident.domain.service.IncidentCommandService;
import com.swyp.noticore.domains.incident.persistence.repository.IncidentInfoRepository;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class LoadTestUseCase {

    static final String LOAD_TEST_PREFIX = "[LOAD_TEST]";

    private final IncidentCommandService incidentCommandService;
    private final IncidentInfoRepository incidentInfoRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * [Before 시나리오]
     * @Transactional 안에서 DB 저장 + 알림 전송 지연을 모두 처리한다.
     * → 알림 I/O가 완료될 때까지 DB 커넥션을 점유한다.
     *
     * 지연 공식: 300ms(Email) + memberCount × 200ms(SMS+OnCall+Slack per member)
     * 기본값 members=3 → 900ms (문제 사례와 동일한 수치)
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
     * [After 시나리오]
     * @Transactional 안에서 DB 저장만 처리하고, 알림은 AFTER_COMMIT 이벤트로 분리한다.
     * → 트랜잭션 커밋 직후 DB 커넥션 반환, 알림은 별도 스레드에서 비동기 처리된다.
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
