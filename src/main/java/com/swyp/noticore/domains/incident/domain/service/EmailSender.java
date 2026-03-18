package com.swyp.noticore.domains.incident.domain.service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

public interface EmailSender {

    void sendEmailAlert(MimeMessage originalMessage, List<String> emailAddresses, String subject, String noticeMessage);
}
