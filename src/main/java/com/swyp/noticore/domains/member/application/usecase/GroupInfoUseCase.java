package com.swyp.noticore.domains.member.application.usecase;

import com.swyp.noticore.domains.member.application.dto.response.*;

import com.swyp.noticore.domains.member.domain.service.GroupMemberQueryService;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@UseCase
@RequiredArgsConstructor
public class GroupInfoUseCase {

    private final GroupMemberQueryService groupMemberQueryService;

    public List<GroupInfoResponse> getAllGroupsInfos() {
        return groupMemberQueryService.getAllGroupsInfos();
    }

    public GroupWithMembersResponse getGroupWithMembers(String groupName) {
        List<MemberInfo> members = groupMemberQueryService.getGroupMemberInfos(groupName);
        List<GroupMemberInfo> mapped = members.stream()
            .map(member -> new GroupMemberInfo(member.name(), member.email()))
            .toList();
        return new GroupWithMembersResponse(groupName, mapped);
    }

}
