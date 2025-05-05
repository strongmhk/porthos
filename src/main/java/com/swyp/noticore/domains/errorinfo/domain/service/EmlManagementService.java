package com.swyp.noticore.domains.errorinfo.domain.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class EmlManagementService {

    private final S3Client s3Client = S3Client.builder()
        .region(Region.US_EAST_1)
        .build();

    public InputStream getEmlFromS3(Map<String, String> payload) {
        String bucket = payload.get("bucket");
        String key = payload.get("key");

        log.info("===== S3 EMAIL NOTIFY =====");
        log.info("Bucket: {}", bucket);
        log.info("Key: {}", key);

        // S3에서 .eml In-Memory Load
        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
            GetObjectRequest.builder().bucket(bucket).key(key).build()
        );

        return new ByteArrayInputStream(objectBytes.asByteArray());
    }
}
