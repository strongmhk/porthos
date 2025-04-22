package com.swyp.noticore.global.test;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class TestController {

    private final S3Client s3Client = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();

    private final SesClient sesClient = SesClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    private final SnsClient snsClient = SnsClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("Backend Conn Success ~ !\nCurrent Backend IP: %s\nCurrent Time: %s", clientIp, now);
    }

    @PostMapping("/notify")
    public ResponseEntity<String> notify(@RequestBody Map<String, String> payload) {
        CompletableFuture.runAsync(() -> {
            try {
                processAndForward(payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ResponseEntity.ok("Accepted");
    }

    private void processAndForward(Map<String, String> payload) throws Exception {
        String bucket = payload.get("bucket");
        String key = payload.get("key");

        System.out.println("===== S3 EMAIL NOTIFY =====");
        System.out.println("Bucket: " + bucket);
        System.out.println("Key: " + key);

        // 1. S3에서 .eml 다운로드
        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(key).build()
        );
        InputStream inputStream = new ByteArrayInputStream(objectBytes.asByteArray());

        // 2. 원본 메일 파싱
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage originalMessage = new MimeMessage(session, inputStream);
        String subject = originalMessage.getSubject();

        // String groupName = subject.replaceAll(".*\\[GROUP:([^\\]]+)\\].*", "$1"); => 그룹 명 추출

        // 2-1. 이메일 제목 형식 검사
        if (subject == null || !subject.matches(".*\\[GROUP:[^\\]]+\\].*")) { // or DB에 그룹명이 존재하지않는 경우도 향 후 추가
            // 오류 안내 메일 전송
            Address[] froms = originalMessage.getFrom();
            String sender = (froms != null && froms.length > 0) ? ((InternetAddress) froms[0]).getAddress() : null;

            if (sender != null && sender.contains("@")) {
                MimeMessage errorReply = new MimeMessage(session);
                errorReply.setFrom(new InternetAddress("no-reply@prod.aic.hanwhavision.cloud"));
                errorReply.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sender));
                errorReply.setSubject("[ERROR] 그룹 메일 전송 실패 안내");

                String body = String.join("\n",
                        "안녕하세요.",
                        "",
                        "보내주신 메일은 시스템에서 자동 처리되지 않았습니다.",
                        "",
                        "사유: 메일 제목에 [GROUP:그룹명] 형식이 누락되었거나 존재하지 않는 그룹입니다.",
                        "",
                        "정확한 형식으로 다시 시도해주세요.",
                        "");

                errorReply.setText(body);
                errorReply.saveChanges();

                ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
                errorReply.writeTo(errorBaos);
                byte[] errorRaw = errorBaos.toByteArray();

                SendRawEmailRequest errorReq = SendRawEmailRequest.builder()
                        .rawMessage(RawMessage.builder()
                                .data(SdkBytes.fromByteArray(errorRaw))
                                .build())
                        .build();

                sesClient.sendRawEmail(errorReq);

                System.out.println("Invalid format. Sent error reply to: " + sender);
            } else {
                System.out.println("Invalid format. Sender address not found.");
            }
            return;
        }

        // 3. Forward 메시지 생성
        MimeMessage forwardMessage = new MimeMessage(session);
        forwardMessage.setFrom(new InternetAddress("no-reply@prod.aic.hanwhavision.cloud"));
        forwardMessage.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("moonswok022@gmail.com,rrim33@gmail.com,yhkang2003@gmail.com,injnamek@gmail.com,kim6562166086@gmail.com,kitty14904@gmail.com,hojun121@gmail.com"));
        forwardMessage.setSubject("[FORWARD] " + subject);

        MimeMultipart multipart = new MimeMultipart();

        // 3-1: 안내 메시지
        MimeBodyPart notePart = new MimeBodyPart();
        notePart.setText("※ 이 메일은 자동 전달된 장애 보고입니다.\n\n");

        // 3-2: 원본 메일 통째로 첨부
        MimeBodyPart forwardPart = new MimeBodyPart();
        forwardPart.setContent(originalMessage, "message/rfc822");

        multipart.addBodyPart(notePart);
        multipart.addBodyPart(forwardPart);

        forwardMessage.setContent(multipart);
        forwardMessage.saveChanges();

        // 4. SES 전송
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        forwardMessage.writeTo(baos);
        byte[] rawMessageBytes = baos.toByteArray();

        SendRawEmailRequest sesRequest = SendRawEmailRequest.builder()
                .rawMessage(RawMessage.builder()
                        .data(SdkBytes.fromByteArray(rawMessageBytes))
                        .build())
                .build();

        sesClient.sendRawEmail(sesRequest);

        System.out.println("====== FORWARDED EMAIL ======");
        System.out.println("Original Subject: " + subject);
        Address[] recipients = forwardMessage.getRecipients(Message.RecipientType.TO);
        if (recipients != null) {
            String recipientList = Arrays.stream(recipients)
                .map(Address::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No recipients");

            System.out.println("Recipients: " + recipientList);
        } else {
            System.out.println("Recipients: None");
        }
        System.out.println("==============================");

        // 5. SMS 전송
        List<String> smsRecipients = List.of("+821038476467");
        sendSmsAlert(subject, smsRecipients);
    }

    private void sendSmsAlert(String subject, List<String> phoneNumbers) {
        Map<String, MessageAttributeValue> smsAttributes = Map.of(
            "AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                .stringValue("Transactional")
                .dataType("String")
                .build()
        );

        for (String number : phoneNumbers) {
            try {
                PublishRequest request = PublishRequest.builder()
                        .phoneNumber(number)
                        .message("긴급 장애 발생: " + subject)
                        .messageAttributes(smsAttributes)
                        .build();

                PublishResponse result = snsClient.publish(request);
                System.out.printf("SMS 전송 성공 → %s | Message ID: %s%n", number, result.messageId());
            } catch (Exception e) {
                System.err.printf("SMS 전송 실패 → %s | 이유: %s%n", number, e.getMessage());
            }
        }
    }
}
