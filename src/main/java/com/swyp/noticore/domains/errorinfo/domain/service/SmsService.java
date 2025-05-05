package com.swyp.noticore.domains.errorinfo.domain.service;

import com.swyp.noticore.domains.errorinfo.model.NotificationMethod;
import com.swyp.noticore.domains.errorinfo.model.NotificationStatus;
import com.swyp.noticore.domains.errorinfo.domain.service.NotificationLogCommandService;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
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

import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SmsService {

    private final NotificationLogCommandService notificationLogCommandService;

    private final SnsClient snsClient = SnsClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    /**
     * SMS 전송 및 전파 로그 기록
     *
     * @param incidentId  장애 ID
     * @param subject     전송 메시지 내용
     * @param member      수신 대상 MemberEntity
     */
    public void sendSmsAlert(Long incidentId, String subject, MemberEntity member) {
        String phoneNumber = member.getPhone();

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

            log.info("[SMS] 전송 성공 → {} | Message ID: {}", phoneNumber, result.messageId());

            notificationLogCommandService.save(
                incidentId,
                member,
                NotificationMethod.SMS,
                NotificationStatus.SUCCESS
            );

        } catch (Exception e) {
            log.error("[SMS] 전송 실패 → {} | 이유: {}", phoneNumber, e.getMessage());

            notificationLogCommandService.save(
                incidentId,
                member,
                NotificationMethod.SMS,
                NotificationStatus.FAIL
            );
        }
    }
}
