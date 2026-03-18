package com.swyp.noticore.domains.incident.application.event;

import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

public record NotificationEvent(
        MimeMessage originalMessage,
        String subject,
        String title,
        String noticeMessage,
        List<MemberInfo> allMembers
) {}
