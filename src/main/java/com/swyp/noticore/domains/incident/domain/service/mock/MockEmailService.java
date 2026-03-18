package com.swyp.noticore.domains.incident.domain.service.mock;

import com.swyp.noticore.domains.incident.domain.service.EmailSender;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * prod 프로필이 아닌 환경(local, dev, qa)에서 AWS SES 대신 사용되는 Email 목 구현체.
 * 실제 SES 호출 없이 300ms 지연으로 이메일 전송을 시뮬레이션한다.
 */
@Slf4j
@Service
@Profile("!prod")
public class MockEmailService implements EmailSender {

    private static final long SIMULATED_DELAY_MS = 300L;

    @Override
    public void sendEmailAlert(MimeMessage originalMessage, List<String> emailAddresses, String subject, String noticeMessage) {
        try {
            log.info("[MOCK-EMAIL] 전송 시뮬레이션 시작 → 수신자 {}명, 제목: {}", emailAddresses.size(), subject);
            Thread.sleep(SIMULATED_DELAY_MS);
            log.info("[MOCK-EMAIL] 전송 시뮬레이션 완료 ({}ms)", SIMULATED_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
