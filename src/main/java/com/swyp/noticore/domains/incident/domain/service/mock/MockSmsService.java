package com.swyp.noticore.domains.incident.domain.service.mock;

import com.swyp.noticore.domains.incident.domain.service.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * prod 프로필이 아닌 환경(local, dev, qa)에서 AWS SNS 대신 사용되는 SMS 목 구현체.
 * 실제 SNS 호출 없이 150ms 지연으로 SMS 전송을 시뮬레이션한다.
 */
@Slf4j
@Service
@Profile("!prod")
public class MockSmsService implements SmsSender {

    private static final long SIMULATED_DELAY_MS = 150L;

    @Override
    public void sendSmsAlert(String subject, String phoneNumber) {
        try {
            log.info("[MOCK-SMS] 전송 시뮬레이션 시작 → {}", phoneNumber);
            Thread.sleep(SIMULATED_DELAY_MS);
            log.info("[MOCK-SMS] 전송 시뮬레이션 완료 ({}ms) → {}", SIMULATED_DELAY_MS, phoneNumber);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
