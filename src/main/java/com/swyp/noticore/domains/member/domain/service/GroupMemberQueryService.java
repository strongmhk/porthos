package com.swyp.noticore.domains.member.domain.service;

import com.swyp.noticore.domains.member.application.dto.response.GroupInfoResponse;
import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import com.swyp.noticore.domains.member.persistence.repository.GroupInfoRepository;
import com.swyp.noticore.domains.member.persistence.repository.MemberGroupRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class GroupMemberQueryService {

    private final MemberGroupRepository memberGroupRepository;
    private final GroupInfoRepository groupInfoRepository;

    public List<MemberInfo> getGroupMemberInfos(String groupName) {
        return memberGroupRepository.findMemberInfoByGroupName(groupName);
    }
    public List<String> filterExistingGroupNames(List<String> groupNames) {
        return groupInfoRepository.findNameByNameIn(groupNames);
    }

    public List<GroupInfoResponse> getAllGroupsInfos() {
        return memberGroupRepository.findAllGroupInfos();
    }
}
