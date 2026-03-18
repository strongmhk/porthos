package com.swyp.noticore.domains.incident.application.event;

import com.slack.api.webhook.Payload;
import com.swyp.noticore.domains.incident.domain.service.EmailSender;
import com.swyp.noticore.domains.incident.domain.service.OncallSender;
import com.swyp.noticore.domains.incident.domain.service.SlackMessageFormatter;
import com.swyp.noticore.domains.incident.domain.service.SlackService;
import com.swyp.noticore.domains.incident.domain.service.SmsSender;
import com.swyp.noticore.domains.member.application.mapper.MemberInfoMapper;
import com.swyp.noticore.global.constants.NationNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final OncallSender oncallSender;
    private final SlackService slackService;
    private final SlackMessageFormatter slackMessageFormatter;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        // Email 전송
        emailSender.sendEmailAlert(
                event.originalMessage(),
                MemberInfoMapper.mapToEmailAddresses(event.allMembers()),
                event.subject(),
                event.noticeMessage()
        );

        // SMS 전송
        MemberInfoMapper.mapToSmsRecipients(event.allMembers()).stream()
                .map(phone -> NationNumber.KOREA.getValue() + phone.substring(1))
                .forEach(phone -> smsSender.sendSmsAlert(event.subject(), phone));

        // OnCall 전송
        MemberInfoMapper.mapToOncallRecipients(event.allMembers()).stream()
                .map(phone -> NationNumber.KOREA.getValue() + phone.substring(1))
                .forEach(phone -> oncallSender.triggerOnCall(event.subject(), phone));

        // Slack 전송
        Payload slackPayload = slackMessageFormatter.formatGeneralErrorMessage(event.title());
        MemberInfoMapper.mapToSlackRecipients(event.allMembers()).forEach(url -> {
            try {
                slackService.sendSlackAlert(slackPayload, url);
            } catch (Exception e) {
                log.error("Failed to send Slack alert to {}: {}", url, e.getMessage());
            }
        });
    }
}
