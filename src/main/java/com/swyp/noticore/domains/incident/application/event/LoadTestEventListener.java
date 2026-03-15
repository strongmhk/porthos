package com.swyp.noticore.domains.incident.application.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class LoadTestEventListener {

    /**
     * 트랜잭션 커밋 후 비동기 스레드에서 알림 전송 지연을 시뮬레이션한다.
     * DB 커넥션은 이미 반환된 상태에서 실행된다.
     *
     * 지연 공식: 300ms(Email) + memberCount × 200ms(SMS+OnCall+Slack per member)
     */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(LoadTestNotificationEvent event) {
        long delayMs = 300L + event.memberCount() * 200L;
        log.debug("[AFTER] Simulating notifications outside transaction: members={}, delay={}ms",
                event.memberCount(), delayMs);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
