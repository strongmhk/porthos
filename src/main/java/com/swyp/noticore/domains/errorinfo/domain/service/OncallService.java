package com.swyp.noticore.domains.errorinfo.domain.service;

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
@Transactional
@Service
@RequiredArgsConstructor
public class OncallService {

    private final LambdaClient lambdaClient = LambdaClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    public void triggerOnCall(String subject, String phoneNumber) {
        try {
            String payload = String.format("{\"phoneNumbers\": [\"%s\"]}", phoneNumber);

            InvokeRequest request = InvokeRequest.builder()
                .functionName("pjh-TriggerEmergencyCall-ua1")
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

            InvokeResponse response = lambdaClient.invoke(request);
            String responseStr = response.payload().asUtf8String();
            System.out.printf("Lambda OnCall 호출 성공 → %s | Response: %s%n", phoneNumber, responseStr);
        } catch (Exception e) {
            System.err.printf("Lambda OnCall 호출 실패 → %s | 이유: %s%n", phoneNumber, e.getMessage());
        }
    }
}