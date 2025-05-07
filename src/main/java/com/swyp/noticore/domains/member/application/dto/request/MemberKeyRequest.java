package com.swyp.noticore.domains.member.application.dto.request;

public record MemberKeyRequest(
    String groupName,
    String name,
    String email
) {}