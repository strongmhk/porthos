package com.swyp.noticore.domains.incident.domain.service;

import com.slack.api.webhook.Payload;

public interface SlackSender {

    void sendSlackAlert(Payload payload, String webhookUrl);
}
