package com.swyp.noticore.domains.errorinfo.application.dto.response;

import jakarta.mail.internet.MimeMessage;
import lombok.Builder;

@Builder
public record MailContent(
    MimeMessage originalMessage,
    String subject,
    String sender,
    String body
) {

}
