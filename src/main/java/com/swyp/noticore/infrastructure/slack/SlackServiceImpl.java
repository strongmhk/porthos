package com.swyp.noticore.infrastructure.slack;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SlackServiceImpl implements SlackService{

    @Value("${slack.webhook-url}")
    private String webhookURL;

    Slack slack = new Slack();

    @Override
    public ResponseEntity<String> sendErrorNotification() {
        try {
            // 테스트용 메시지
            String subject = "[테스트 메일] 서비스 장애 발생";
            String sender = "alert@example.com";
            String body = "서버에서 예기치 못한 오류가 발생했습니다. 조치가 필요합니다.";

            SlackMessageFormatter formatter = new SlackMessageFormatter(subject, sender, body);
            Payload payload = formatter.generalErrorMessageFormat();

            WebhookResponse response = slack.send(webhookURL, payload);
            System.out.println(response);

            return ResponseEntity.ok("Error Message is successfully sent to Slack.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Sending Error Notification to Slack is failed" + e.getMessage());
        }
    }
}
