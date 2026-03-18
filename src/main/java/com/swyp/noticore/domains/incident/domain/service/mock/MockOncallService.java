package com.swyp.noticore.domains.incident.domain.service.mock;

import com.swyp.noticore.domains.incident.domain.service.OncallSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * prod 프로필이 아닌 환경(local, dev, qa)에서 AWS Lambda 대신 사용되는 OnCall 목 구현체.
 * 실제 Lambda 호출 없이 150ms 지연으로 OnCall 트리거를 시뮬레이션한다.
 */
@Slf4j
@Service
@Profile("!prod")
public class MockOncallService implements OncallSender {

    private static final long SIMULATED_DELAY_MS = 150L;

    @Override
    public void triggerOnCall(String subject, String phoneNumber) {
        try {
            log.info("[MOCK-ONCALL] 트리거 시뮬레이션 시작 → {}", phoneNumber);
            Thread.sleep(SIMULATED_DELAY_MS);
            log.info("[MOCK-ONCALL] 트리거 시뮬레이션 완료 ({}ms) → {}", SIMULATED_DELAY_MS, phoneNumber);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
