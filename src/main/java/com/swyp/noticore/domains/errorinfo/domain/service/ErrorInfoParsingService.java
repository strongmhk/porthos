package com.swyp.noticore.domains.errorinfo.domain.service;

import static com.swyp.noticore.global.response.code.CommonErrorCode.INTERNAL_SERVER_ERROR;

import com.swyp.noticore.global.exception.ApplicationException;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ErrorInfoParsingService {

    private final SesClient sesClient = SesClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    public void parseErrorInfoAndForward(InputStream inputStream) {

        // 원본 메일 파싱
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage originalMessage = null;
        try {
            originalMessage = new MimeMessage(session, inputStream);
            String subject = originalMessage.getSubject();

            // String groupName = subject.replaceAll(".*\\[GROUP:([^\\]]+)\\].*", "$1"); => 그룹 명 추출

            // 이메일 제목 형식 검사
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

                    log.error("Invalid format. Sent error reply to: {}", sender);
                } else {
                    log.error("Invalid format. Sender address not found.");
                }
                return;
            }

            // Forward 메시지 생성
            MimeMessage forwardMessage = new MimeMessage(session);
            forwardMessage.setFrom(new InternetAddress("no-reply@prod.aic.hanwhavision.cloud"));
            forwardMessage.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("moonswok022@gmail.com,rrim33@gmail.com,yhkang2003@gmail.com,injnamek@gmail.com,kim6562166086@gmail.com,kitty14904@gmail.com,hojun121@gmail.com"));
            forwardMessage.setSubject("[FORWARD] " + subject);

            MimeMultipart multipart = new MimeMultipart();

            // Part 1: 안내 메시지
            MimeBodyPart notePart = new MimeBodyPart();
            notePart.setText("※ 이 메일은 자동 전달된 장애 보고입니다.\n\n");

            // Part 2: 원본 메일 통째로 첨부
            MimeBodyPart forwardPart = new MimeBodyPart();
            forwardPart.setContent(originalMessage, "message/rfc822");

            multipart.addBodyPart(notePart);
            multipart.addBodyPart(forwardPart);

            forwardMessage.setContent(multipart);
            forwardMessage.saveChanges();

            // SES 전송
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            forwardMessage.writeTo(baos);
            byte[] rawMessageBytes = baos.toByteArray();

            SendRawEmailRequest sesRequest = SendRawEmailRequest.builder()
                .rawMessage(RawMessage.builder()
                    .data(SdkBytes.fromByteArray(rawMessageBytes))
                    .build())
                .build();

            sesClient.sendRawEmail(sesRequest);

            log.info("====== FORWARDED EMAIL ======");
            log.info("Original Subject: {}", subject);
            log.info("Recipients: hojun121@gmail.com, qkrwoghwns@gmail.com");
            log.info("==============================");
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            throw ApplicationException.from(INTERNAL_SERVER_ERROR);
        }
    }
}
