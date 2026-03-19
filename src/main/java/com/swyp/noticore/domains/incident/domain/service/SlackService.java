package com.swyp.noticore.domains.incident.domain.service;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class SlackService implements SlackSender {

    private final Slack slack = Slack.getInstance();

    @Override
    public void sendSlackAlert(Payload payload, String webhookURL) {
        try {
            WebhookResponse response = slack.send(webhookURL, payload);
            log.info("Slack response: {}", response);
        } catch (Exception e) {
            log.error("Failed to send Slack message to {}: {}", webhookURL, e.getMessage(), e);
        }
    }
}
