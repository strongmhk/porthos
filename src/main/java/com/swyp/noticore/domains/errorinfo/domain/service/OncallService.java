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
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OncallService {

    private final NotificationLogCommandService notificationLogCommandService;

    private final LambdaClient lambdaClient = LambdaClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    /**
     * OnCall 호출 및 전파 로그 저장
     *
     * @param incidentId  장애 ID
     * @param subject     장애 제목
     * @param member      수신 대상 회원
     */
    public void triggerOnCall(Long incidentId, String subject, MemberEntity member) {
        String phoneNumber = member.getPhone();

        try {
            String payload = String.format("{\"phoneNumbers\": [\"%s\"]}", phoneNumber);

            InvokeRequest request = InvokeRequest.builder()
                .functionName("pjh-TriggerEmergencyCall-ua1")
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

            InvokeResponse response = lambdaClient.invoke(request);
            String responseStr = response.payload().asUtf8String();

            log.info("[ONCALL] 전송 성공 → {} | Response: {}", phoneNumber, responseStr);

            notificationLogCommandService.save(
                incidentId,
                member,
                NotificationMethod.ONCALL,
                NotificationStatus.SUCCESS
            );

        } catch (Exception e) {
            log.error("[ONCALL] 전송 실패 → {} | 이유: {}", phoneNumber, e.getMessage());

            notificationLogCommandService.save(
                incidentId,
                member,
                NotificationMethod.ONCALL,
                NotificationStatus.FAIL
            );
        }
    }
}
