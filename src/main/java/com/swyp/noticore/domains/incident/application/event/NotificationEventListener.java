package com.swyp.noticore.domains.incident.application.event;

import com.slack.api.webhook.Payload;
import com.swyp.noticore.domains.incident.domain.service.EmailSender;
import com.swyp.noticore.domains.incident.domain.service.OncallSender;
import com.swyp.noticore.domains.incident.domain.service.SlackMessageFormatter;
import com.swyp.noticore.domains.incident.domain.service.SlackSender;
import com.swyp.noticore.domains.incident.domain.service.SmsSender;
import com.swyp.noticore.domains.member.application.mapper.MemberInfoMapper;
import com.swyp.noticore.global.constants.NationNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final OncallSender oncallSender;
    private final SlackSender slackSender;
    private final SlackMessageFormatter slackMessageFormatter;
    @Qualifier("channelExecutor")
    private final Executor channelExecutor;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        List<String> emailAddresses = MemberInfoMapper.mapToEmailAddresses(event.allMembers());
        List<String> smsRecipients = MemberInfoMapper.mapToSmsRecipients(event.allMembers());
        List<String> oncallRecipients = MemberInfoMapper.mapToOncallRecipients(event.allMembers());
        List<String> slackUrls = MemberInfoMapper.mapToSlackRecipients(event.allMembers());
        Payload slackPayload = slackMessageFormatter.formatGeneralErrorMessage(event.title());

        CompletableFuture<Void> emailFuture = CompletableFuture.runAsync(
                () -> emailSender.sendEmailAlert(
                        event.originalMessage(), emailAddresses, event.subject(), event.noticeMessage()
                ),
                channelExecutor
        ).exceptionally(e -> {
            log.error("Email 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture<Void> smsFuture = CompletableFuture.runAsync(
                () -> smsRecipients.stream()
                        .map(phone -> NationNumber.KOREA.getValue() + phone.substring(1))
                        .forEach(phone -> smsSender.sendSmsAlert(event.subject(), phone)),
                channelExecutor
        ).exceptionally(e -> {
            log.error("SMS 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture<Void> oncallFuture = CompletableFuture.runAsync(
                () -> oncallRecipients.stream()
                        .map(phone -> NationNumber.KOREA.getValue() + phone.substring(1))
                        .forEach(phone -> oncallSender.triggerOnCall(event.subject(), phone)),
                channelExecutor
        ).exceptionally(e -> {
            log.error("OnCall 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture<Void> slackFuture = CompletableFuture.runAsync(
                () -> slackUrls.forEach(url -> {
                    try {
                        slackSender.sendSlackAlert(slackPayload, url);
                    } catch (Exception e) {
                        log.error("Slack 전송 실패 (url={}): {}", url, e.getMessage());
                    }
                }),
                channelExecutor
        ).exceptionally(e -> {
            log.error("Slack 전송 실패: {}", e.getMessage());
            return null;
        });

        CompletableFuture.allOf(emailFuture, smsFuture, oncallFuture, slackFuture).join();
    }
}
