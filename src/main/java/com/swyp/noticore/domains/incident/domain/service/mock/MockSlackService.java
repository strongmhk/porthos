package com.swyp.noticore.domains.incident.domain.service.mock;

import com.slack.api.webhook.Payload;
import com.swyp.noticore.domains.incident.domain.service.SlackSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * prod 프로필이 아닌 환경(local, dev, qa)에서 Slack Webhook 대신 사용되는 Slack 목 구현체.
 * 실제 Webhook 호출 없이 100ms 지연으로 Slack 전송을 시뮬레이션한다.
 */
@Slf4j
@Service
@Profile("!prod")
public class MockSlackService implements SlackSender {

    private static final long SIMULATED_DELAY_MS = 100L;

    @Override
    public void sendSlackAlert(Payload payload, String webhookUrl) {
        try {
            log.info("[MOCK-SLACK] 전송 시뮬레이션 시작 → webhookUrl: {}", webhookUrl);
            Thread.sleep(SIMULATED_DELAY_MS);
            log.info("[MOCK-SLACK] 전송 시뮬레이션 완료 ({}ms)", SIMULATED_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
