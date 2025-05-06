package com.swyp.noticore.domains.member.application.dto.response;

public record MemberInfo(
    Long id,
    String name,
    String email,
    String phone,
    String slackUrl,
    Boolean smsNoti,
    Boolean oncallNoti,
    Boolean slackNoti
) {
}
