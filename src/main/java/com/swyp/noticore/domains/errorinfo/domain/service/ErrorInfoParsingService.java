package com.swyp.noticore.domains.errorinfo.domain.service;

import static com.swyp.noticore.global.response.code.CommonErrorCode.BAD_REQUEST;
import static com.swyp.noticore.global.response.code.CommonErrorCode.INTERNAL_SERVER_ERROR;

import com.swyp.noticore.domains.errorinfo.application.dto.response.MailContent;
import com.swyp.noticore.domains.member.persistence.repository.GroupInfoRepository;
import com.swyp.noticore.global.exception.ApplicationException;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
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

    private final GroupInfoRepository groupInfoRepository;

    private final SesClient sesClient = SesClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    public MailContent parseAndValidate(InputStream inputStream) {
        try {
            // 원본 메일 파싱
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage originalMessage = new MimeMessage(session, inputStream);
            String subject = originalMessage.getSubject();

            // 그룹명 추출
            String groupName = subject.replaceAll(".*\\[GROUP:([^\\]]+)\\].*", "$1");

            // 제목 유효성 검사
            boolean isExistGroup = groupInfoRepository.exitsByName(groupName);

            if (subject.isBlank() || !subject.matches(".*\\[GROUP:[^\\]]+\\].*") || !isExistGroup) {
                sendErrorEmail(session, originalMessage);
                log.error("subject : {}, Invalid email subject format.", subject);
                throw ApplicationException.from(BAD_REQUEST);
            }

            return MailContent.builder()
                .originalMessage(originalMessage)
                .subject(subject)
                .build();

        } catch (MessagingException e) {
            log.error("Failed to parse error email.", e);
            throw ApplicationException.from(INTERNAL_SERVER_ERROR);
        }
    }

    private void sendErrorEmail(Session session, MimeMessage originalMessage) {
        try {
            Address[] froms = originalMessage.getFrom();
            String sender = (froms != null && froms.length > 0) ? ((InternetAddress) froms[0]).getAddress() : null;

            if (!sender.isBlank() && sender.contains("@")) {
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

                log.info("Sent error reply to: {}", sender);
            } else {
                log.warn("Sender address not found.");
            }
        } catch (IOException | MessagingException e) {
            log.error("Failed to send error email.", e);
            throw ApplicationException.from(INTERNAL_SERVER_ERROR);
        }
    }

}
