package com.swyp.noticore.global.test;

import com.swyp.noticore.infrastructure.slack.SlackService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final S3Client s3Client = S3Client.builder()
            .region(Region.US_EAST_1)  // Ensure this matches the S3 bucket region
            .build();

    private final SlackService slackService;

    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("Backend Conn Success ~ !\nCurrent Backend IP: %s\nCurrent Time: %s", clientIp, now);
    }

    @PostMapping("/notify")
    public ResponseEntity<String> notify(@RequestBody Map<String, String> payload) {
        String bucket = payload.get("bucket");
        String key = payload.get("key");

        if (bucket == null || key == null) {
            return ResponseEntity.badRequest().body("Missing 'bucket' or 'key' in payload");
        }

        try {
            // Download .eml file from S3
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());

            InputStream inputStream = new ByteArrayInputStream(objectBytes.asByteArray());

            // JavaMail parsing
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(session, inputStream);

            String subject = message.getSubject();
            Address[] froms = message.getFrom();
            String sender = (froms != null && froms.length > 0) ? ((InternetAddress) froms[0]).getAddress() : "unknown";
            String body = extractText(message);

            if (subject == null || body == null) {
                return ResponseEntity.badRequest().body("Missing subject or body in email");
            }

            System.out.println("====== EMAIL RECEIVED ======");
            System.out.println("Subject: " + subject);
            System.out.println("From: " + sender);
            System.out.println("Body:\n" + body);
            System.out.println("============================");

            return ResponseEntity.ok("Email processed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to process .eml file: " + e.getMessage());
        }
    }

    @GetMapping("/test/slack")
    public ResponseEntity<String> sendSlackTextMessage() throws Exception {
        slackService.sendErrorNotification();
        return ResponseEntity.ok("Error Message is successfully sent to Slack.");
    }

    private String extractText(Part part) throws Exception {
        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        } else if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String result = extractText(mp.getBodyPart(i));
                if (result != null) return result;
            }
        }
        return null;
    }
}

