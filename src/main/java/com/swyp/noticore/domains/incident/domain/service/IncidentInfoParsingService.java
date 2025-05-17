package com.swyp.noticore.domains.incident.domain.service;

import static com.swyp.noticore.global.constants.SesConstants.FROM;
import static com.swyp.noticore.global.response.code.CommonErrorCode.BAD_REQUEST;
import static com.swyp.noticore.global.response.code.CommonErrorCode.INTERNAL_SERVER_ERROR;

import com.swyp.noticore.domains.incident.application.dto.response.MailContent;
import com.swyp.noticore.domains.member.persistence.repository.GroupInfoRepository;
import com.swyp.noticore.global.exception.ApplicationException;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Multipart;
import jakarta.mail.BodyPart;
import jakarta.mail.Part;
import org.jsoup.Jsoup;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
public class IncidentInfoParsingService {

    private final GroupInfoRepository groupInfoRepository;

    private final SesClient sesClient = SesClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    public MailContent parseAndValidate(InputStream inputStream) {
        try {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage originalMessage = new MimeMessage(session, inputStream);
            String subject = originalMessage.getSubject();

            // 제목 유효성 검사: [emergency: ...] 패턴 (대소문자 무시)
            if (subject == null || !subject.matches("(?i).*\\[emergency:[^\\]]+\\].*")) {
                sendErrorEmail(session, originalMessage);
                log.error("Invalid subject format: {}", subject);
                throw ApplicationException.from(BAD_REQUEST);
            }

            // 그룹명 추출 및 파싱
            String groupSection = subject.replaceAll("(?i).*\\[emergency:([^\\]]+)\\].*", "$1").toLowerCase();
            List<String> groupNames = Arrays.stream(groupSection.split(","))
                                            .map(String::trim)
                                            .filter(name -> !name.isEmpty())
                                            .toList();

            // 존재하는 그룹이 하나라도 있는지 확인
            List<String> existingGroups = groupInfoRepository.findNameByNameIn(groupNames);

            if (groupNames.isEmpty() || existingGroups.isEmpty()) {
                sendErrorEmail(session, originalMessage);
                log.warn("No valid groups found. Extracted groups: {}, Subject: {}", groupNames, subject);
                throw ApplicationException.from(BAD_REQUEST);
            }
            
            // 본문 추출
            String rawBody;
            try {
                rawBody = extractRawText(originalMessage);
            } catch (Exception e) {
                log.warn("본문 추출 실패. 빈 문자열로 대체", e);
                rawBody = "";
            }

            return MailContent.builder()
                    .originalMessage(originalMessage)
                    .subject(subject)
                    .rawBody(rawBody)
                    .build();

        } catch (MessagingException e) {
            log.error("Failed to parse email.", e);
            throw ApplicationException.from(INTERNAL_SERVER_ERROR);
        }
    }

    private void sendErrorEmail(Session session, MimeMessage originalMessage) {
        try {
            Address[] froms = originalMessage.getFrom();
            String sender = (froms != null && froms.length > 0) ? ((InternetAddress) froms[0]).getAddress() : null;

            if (!sender.isBlank() && sender.contains("@")) {
                MimeMessage errorReply = new MimeMessage(session);
                errorReply.setFrom(new InternetAddress(FROM));
                errorReply.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sender));
                errorReply.setSubject("[ERROR] 그룹 메일 전송 실패 안내");

                String body = String.join("\n",
                    "안녕하세요.",
                    "",
                    "보내주신 메일은 시스템에서 자동 처리되지 않았습니다.",
                    "",
                    "사유: 메일 제목 형식이 잘못되었거나( [Emergency:그룹명_1, 그룹명_2, ...] ) 존재하지 않는 그룹들입니다.",
                    "",
                    "정확한 형식 및 존재하는 그룹 이름으로 다시 시도해주세요.",
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

    private String extractRawText(MimeMessage message) throws Exception {
        String contentType = message.getContentType();
        log.info("[extractRawText] MIME Type: {}", contentType);

        Object content = message.getContent();
        return extractRecursive(content);
    }

    private String extractRecursive(Object part) throws Exception {
        if (part instanceof String content) {
            log.info("[extractRecursive] Found text content:\n{}", content);
            return content.strip();
        }

        if (part instanceof Multipart multipart) {
            log.info("[extractRecursive] Multipart found: {} parts", multipart.getCount());

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String contentType = bodyPart.getContentType();
                String disposition = bodyPart.getDisposition();

                log.info("[extractRecursive] Processing part #{}: type={}, disposition={}", i, contentType, disposition);

                if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
                    log.info("[extractRecursive] Skipped attachment part");
                    continue;
                }

                if (bodyPart.isMimeType("image/*")) {
                    log.info("[extractRecursive] Skipped image part");
                    continue;
                }

                Object content = bodyPart.getContent();

                if (bodyPart.isMimeType("text/plain")) {
                    log.info("[extractRecursive] Found text/plain content");
                    return ((String) content).strip();
                }

                if (bodyPart.isMimeType("text/html")) {
                    log.info("[extractRecursive] Found text/html content");
                    String html = (String) content;
                    return Jsoup.parse(html).text().strip();
                }

                if (bodyPart.isMimeType("multipart/*")) {
                    log.info("[extractRecursive] Nested multipart found");
                    String nested = extractRecursive(content);
                    if (!nested.isEmpty()) return nested;
                }
            }
        }

        log.warn("[extractRecursive] No suitable content found in part: {}", part.getClass().getName());
        return "";
    }

    private String preview(String content) {
        return content.length() > 500 ? content.substring(0, 500) + "..." : content;
    }
}
