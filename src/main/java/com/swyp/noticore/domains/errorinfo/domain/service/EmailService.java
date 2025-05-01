package com.swyp.noticore.domains.errorinfo.domain.service;

import static com.swyp.noticore.global.response.code.CommonErrorCode.INTERNAL_SERVER_ERROR;

import com.swyp.noticore.domains.member.persistence.repository.MemberGroupRepository;
import com.swyp.noticore.global.exception.ApplicationException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
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
public class EmailService {

    private final MemberGroupRepository memberGroupRepository;

    private final SesClient sesClient = SesClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    public void sendEmailAlert(MimeMessage originalMessage, List<String> emailAddresses, String subject) {
        try {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage forwardMessage = new MimeMessage(session);
            forwardMessage.setFrom(new InternetAddress("no-reply@prod.aic.hanwhavision.cloud"));

            // 이메일 전송
            if (!emailAddresses.isEmpty()) {
                forwardMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(String.join(",", emailAddresses)));
                forwardMessage.setSubject("[FORWARD] " + subject);

                MimeMultipart multipart = new MimeMultipart();

                // 안내 메시지
                MimeBodyPart notePart = new MimeBodyPart();
                notePart.setText("※ 이 메일은 자동 전달된 장애 보고입니다.\n\n");

                // 원본 메일 첨부
                MimeBodyPart forwardPart = new MimeBodyPart();
                forwardPart.setContent(originalMessage, "message/rfc822");

                multipart.addBodyPart(notePart);
                multipart.addBodyPart(forwardPart);

                forwardMessage.setContent(multipart);
                forwardMessage.saveChanges();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                forwardMessage.writeTo(baos);
                byte[] rawMessageBytes = baos.toByteArray();

                // SES 전송
                SendRawEmailRequest sesRequest = SendRawEmailRequest.builder()
                    .rawMessage(RawMessage.builder().data(SdkBytes.fromByteArray(rawMessageBytes)).build())
                    .build();

                sesClient.sendRawEmail(sesRequest);

                log.info("====== FORWARDED EMAIL ======");
                log.info("Original Subject: {}", subject);
            } else {
                log.warn("No email recipients found for group.");
            }
        } catch (MessagingException | IOException e) {
            log.error("Failed to forward email.", e);
            throw ApplicationException.from(INTERNAL_SERVER_ERROR);
        }
    }
}
