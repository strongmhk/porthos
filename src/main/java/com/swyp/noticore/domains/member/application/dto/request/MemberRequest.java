package com.swyp.noticore.domains.member.application.dto.request;

import com.swyp.noticore.domains.member.domain.constant.Role;

public record MemberRequest(
    String groupName,
    String name,
    String email,
    String phone,
    String password,
    Role role,
    Boolean smsNoti,
    Boolean oncallNoti,
    Boolean slackNoti,
    String slackUrl
) {}