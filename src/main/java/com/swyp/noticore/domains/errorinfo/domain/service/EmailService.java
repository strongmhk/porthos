package com.swyp.noticore.domains.errorinfo.domain.service;

import com.swyp.noticore.domains.errorinfo.model.NotificationMethod;
import com.swyp.noticore.domains.errorinfo.model.NotificationStatus;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import com.swyp.noticore.domains.errorinfo.domain.service.NotificationLogCommandService;
import com.swyp.noticore.global.exception.ApplicationException;
import jakarta.mail.*;
import jakarta.mail.internet.*;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static com.swyp.noticore.global.response.code.CommonErrorCode.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {

    private final SesClient sesClient = SesClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    private final NotificationLogCommandService notificationLogCommandService;

    /**
     * 장애 메일 자동 전달 (다수 수신자에게 한 번에 전송하고, 개별 수신자 로그 기록)
     *
     * @param incidentId      관련 장애 ID
     * @param originalMessage 원본 이메일 메시지
     * @param recipients      수신 대상 MemberEntity 목록
     * @param subject         이메일 제목
     * @param noticeMessage   본문 상단에 첨부할 안내 메시지
     */
    public void sendEmailAlert(Long incidentId, MimeMessage originalMessage, List<MemberEntity> recipients, String subject, String noticeMessage) {
        try {
            // 수신자 이메일 추출
            List<String> emailAddresses = recipients.stream()
                .map(MemberEntity::getEmail)
                .toList();

            // SES로 전송할 새로운 MIME 메시지 구성
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage forwardMessage = new MimeMessage(session);
            forwardMessage.setFrom(new InternetAddress("no-reply@noticore.co.kr"));
            forwardMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(String.join(",", emailAddresses)));
            forwardMessage.setSubject("[FORWARD] " + subject);

            MimeMultipart multipart = new MimeMultipart();

            // 안내 메시지 Part 추가
            MimeBodyPart notePart = new MimeBodyPart();
            notePart.setText(noticeMessage);
            multipart.addBodyPart(notePart);

            // 원본 메일 첨부
            MimeBodyPart forwardPart = new MimeBodyPart();
            forwardPart.setContent(originalMessage, "message/rfc822");
            multipart.addBodyPart(forwardPart);

            forwardMessage.setContent(multipart);
            forwardMessage.saveChanges();

            // 바이트 배열로 변환 후 SES 전송
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            forwardMessage.writeTo(baos);
            byte[] rawMessageBytes = baos.toByteArray();

            SendRawEmailRequest sesRequest = SendRawEmailRequest.builder()
                .rawMessage(RawMessage.builder().data(SdkBytes.fromByteArray(rawMessageBytes)).build())
                .build();

            sesClient.sendRawEmail(sesRequest);

            // 수신자 개별 로그 저장 (모두 성공 처리)
            for (MemberEntity member : recipients) {
                notificationLogCommandService.save(
                    incidentId,
                    member,
                    NotificationMethod.EMAIL,
                    NotificationStatus.SUCCESS
                );
            }

            log.info("[EMAIL] Sent to {} recipients, subject: {}", recipients.size(), subject);

        } catch (MessagingException | IOException e) {
            log.error("[EMAIL] Failed to forward incident email", e);

            // 실패한 경우, 모든 수신자 로그를 FAIL로 기록
            for (MemberEntity member : recipients) {
                notificationLogCommandService.save(
                    incidentId,
                    member,
                    NotificationMethod.EMAIL,
                    NotificationStatus.FAIL
                );
            }

            throw ApplicationException.from(INTERNAL_SERVER_ERROR);
        }
    }
}
