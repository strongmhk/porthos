package com.swyp.noticore.domains.member.application.dto.response;

import java.util.List;

public record GroupWithMembersResponse(
    String name,
    List<GroupMemberInfo> members
) {}
