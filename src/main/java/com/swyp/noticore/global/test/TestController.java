// package com.swyp.noticore.global.test;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import jakarta.servlet.http.HttpServletRequest;

// import software.amazon.awssdk.core.ResponseBytes;
// import software.amazon.awssdk.regions.Region;
// import software.amazon.awssdk.services.s3.S3Client;
// import software.amazon.awssdk.services.s3.model.GetObjectRequest;
// import software.amazon.awssdk.services.s3.model.GetObjectResponse;

// import jakarta.mail.*;
// import jakarta.mail.internet.InternetAddress;
// import jakarta.mail.internet.MimeMessage;

// import java.io.ByteArrayInputStream;
// import java.io.InputStream;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.*;

// @RestController
// @RequestMapping("/api")
// public class TestController {

//     private final S3Client s3Client = S3Client.builder()
//             .region(Region.US_EAST_1)  // Ensure this matches the S3 bucket region
//             .build();

//     @GetMapping("/test")
//     public String test(HttpServletRequest request) {
//         String clientIp = request.getRemoteAddr();
//         String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//         return String.format("Backend Conn Success ~ !\nCurrent Backend IP: %s\nCurrent Time: %s", clientIp, now);
//     }

//     @PostMapping("/notify")
//     public ResponseEntity<String> notify(@RequestBody Map<String, String> payload) {
//         String bucket = payload.get("bucket");
//         String key = payload.get("key");

//         if (bucket == null || key == null) {
//             return ResponseEntity.badRequest().body("Missing 'bucket' or 'key' in payload");
//         }

//         try {
//             // Download .eml file from S3
//             ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
//                     .bucket(bucket)
//                     .key(key)
//                     .build());

//             InputStream inputStream = new ByteArrayInputStream(objectBytes.asByteArray());

//             // JavaMail parsing
//             Session session = Session.getDefaultInstance(new Properties());
//             MimeMessage message = new MimeMessage(session, inputStream);

//             String subject = message.getSubject();
//             Address[] froms = message.getFrom();
//             String sender = (froms != null && froms.length > 0) ? ((InternetAddress) froms[0]).getAddress() : "unknown";
//             String body = extractText(message);

//             if (subject == null || body == null) {
//                 return ResponseEntity.badRequest().body("Missing subject or body in email");
//             }

//             System.out.println("====== EMAIL RECEIVED ======");
//             System.out.println("Subject: " + subject);
//             System.out.println("From: " + sender);
//             System.out.println("Body:\n" + body);
//             System.out.println("============================");

//             return ResponseEntity.ok("Email processed successfully");

//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.internalServerError().body("Failed to process .eml file: " + e.getMessage());
//         }
//     }

//     private String extractText(Part part) throws Exception {
//         if (part.isMimeType("text/plain")) {
//             return (String) part.getContent();
//         } else if (part.isMimeType("multipart/*")) {
//             Multipart mp = (Multipart) part.getContent();
//             for (int i = 0; i < mp.getCount(); i++) {
//                 String result = extractText(mp.getBodyPart(i));
//                 if (result != null) return result;
//             }
//         }
//         return null;
//     }
// }


package com.swyp.noticore.global.test;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("Backend Conn Success ~ !\nCurrent Backend IP: %s\nCurrent Time: %s", clientIp, now);
    }

    private final S3Client s3Client = S3Client.builder()
            .region(Region.US_EAST_1)  // Ensure this matches the S3 bucket region
            .build();

    private final SesClient sesClient = SesClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    @PostMapping("/notify")
    public ResponseEntity<String> notify(@RequestBody Map<String, String> payload) {
        String bucket = payload.get("bucket");
        String key = payload.get("key");

        System.out.println("===== S3 EMAIL NOTIFY =====");
        System.out.println("Bucket: " + bucket);
        System.out.println("Key: " + key);
        try {
            // S3에서 .eml 파일 다운로드
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(key).build()
            );

            InputStream inputStream = new ByteArrayInputStream(objectBytes.asByteArray());

            // JavaMail로 파싱
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(session, inputStream);

            // Subject
            String subject = message.getSubject();
            // Exception
            if (subject == null || !subject.contains("[GROUP:SYWP_TEAM]")) {
                return ResponseEntity.badRequest().body("Invalid subject: missing [GROUP:SYWP_TEAM]");
            }

            // From 수정
            message.setFrom(new InternetAddress("no-reply@prod.aic.hanwhavision.cloud"));

            // To 수정
            InternetAddress[] recipients = new InternetAddress[]{
                    new InternetAddress("hojun121@gmail.com"),
                    new InternetAddress("qkrwoghwns@gmail.com")
            };
            message.setRecipients(Message.RecipientType.TO, recipients);

            // MIME 메시지를 다시 byte로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            message.writeTo(baos);
            byte[] rawMessageBytes = baos.toByteArray();

            // SES 전송 요청
            SendRawEmailRequest request = SendRawEmailRequest.builder()
                    .rawMessage(RawMessage.builder()
                            .data(SdkBytes.fromByteArray(rawMessageBytes))
                            .build())
                    .build();

            sesClient.sendRawEmail(request);

            ///////////// Logging Test ///////////////////
            // Sender
            Address[] froms = message.getFrom();
            String sender = (froms != null && froms.length > 0) ? ((InternetAddress) froms[0]).getAddress() : "unknown";

            // Body 추출
            String body = extractText(message);

             // Receiver
            Address[] tos = message.getRecipients(Message.RecipientType.TO);
            String receiverList = (tos != null && tos.length > 0)
                    ? Arrays.stream(tos)
                            .map(addr -> ((InternetAddress) addr).getAddress())
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("unknown")
                    : "unknown";
            System.out.println("====== LOCAL EML PARSED ======");
            System.out.println("Subject: " + subject);
            System.out.println("Sender: " + sender);
            System.out.println("Receiver: " + receiverList);
            System.out.println("Body:\n" + body);
            System.out.println("==============================");

            return ResponseEntity.ok("Parsed email successfully from local file.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Spring error: " + e.getMessage());
        }
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

// import jakarta.mail.*;
// import jakarta.mail.internet.InternetAddress;
// import jakarta.mail.internet.MimeMessage;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
// import software.amazon.awssdk.core.SdkBytes;
// import software.amazon.awssdk.regions.Region;
// import software.amazon.awssdk.services.ses.SesClient;
// import software.amazon.awssdk.services.ses.model.RawMessage;
// import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

// import java.io.*;
// import java.util.*;

// @RestController
// @RequestMapping("/api")
// public class TestController {

//     private final SesClient sesClient = SesClient.builder()
//             .region(Region.US_EAST_1) // SES가 동작하는 리전
//             .credentialsProvider(DefaultCredentialsProvider.create())
//             .build();

//     @PostMapping("/local-eml-test")
//     public ResponseEntity<String> sendGroupMail(@RequestParam String filePath) {
//         try (InputStream inputStream = new FileInputStream(filePath)) {

//             Session session = Session.getDefaultInstance(new Properties());
//             MimeMessage message = new MimeMessage(session, inputStream);

//             String subject = message.getSubject();
//             if (subject == null || !subject.contains("[GROUP:SYWP_TEAM]")) {
//                 return ResponseEntity.badRequest().body("Invalid subject: missing [GROUP:SYWP_TEAM]");
//             }

//             // From 수정
//             message.setFrom(new InternetAddress("no-reply@prod.aic.hanwhavision.cloud"));

//             // To 수정
//             InternetAddress[] recipients = new InternetAddress[]{
//                     new InternetAddress("hojun121@gmail.com"),
//                     new InternetAddress("qkrwoghwns@gmail.com")
//             };
//             message.setRecipients(Message.RecipientType.TO, recipients);

//             // MIME 메시지를 다시 byte로 변환
//             ByteArrayOutputStream baos = new ByteArrayOutputStream();
//             message.writeTo(baos);
//             byte[] rawMessageBytes = baos.toByteArray();

//             // SES 전송 요청
//             SendRawEmailRequest request = SendRawEmailRequest.builder()
//                     .rawMessage(RawMessage.builder()
//                             .data(SdkBytes.fromByteArray(rawMessageBytes))
//                             .build())
//                     .build();

//             sesClient.sendRawEmail(request);

//             System.out.println("====== EMAIL FORWARDED TO GROUP ======");
//             System.out.println("Subject: " + subject);
//             System.out.println("Recipients: hojun121@gmail.com, qkrwoghwns@gmail.com");
//             System.out.println("======================================");

//             return ResponseEntity.ok("Email forwarded to group successfully");

//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.internalServerError().body("Failed to process and send email: " + e.getMessage());
//         }
//     }
// }

