package com.swyp.noticore.domains.errorinfo.domain.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SnsClient snsClient = SnsClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    public void sendSmsAlert(String subject, String phoneNumber) {
        Map<String, MessageAttributeValue> smsAttributes = Map.of(
            "AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                .stringValue("Transactional")
                .dataType("String")
                .build()
        );

        try {
            PublishRequest request = PublishRequest.builder()
                .phoneNumber(phoneNumber)
                .message(subject)
                .messageAttributes(smsAttributes)
                .build();

            PublishResponse result = snsClient.publish(request);
            System.out.printf("SMS 전송 성공 → %s | Message ID: %s%n", phoneNumber, result.messageId());
        } catch (Exception e) {
            System.err.printf("SMS 전송 실패 → %s | 이유: %s%n", phoneNumber, e.getMessage());
        }
    }
}