package com.swyp.noticore.domains.member.presentation;

import com.swyp.noticore.domains.member.application.dto.response.GroupInfoResponse;
import com.swyp.noticore.domains.member.application.dto.response.GroupWithMembersResponse;
import com.swyp.noticore.domains.member.application.usecase.GroupInfoUseCase;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RequestMapping("/api/groups")
@RequiredArgsConstructor
@RestController
public class GroupInfoController {

    private final GroupInfoUseCase groupInfoUseCase;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllGroups() {
        List<GroupInfoResponse> allGroupsInfos = groupInfoUseCase.getAllGroupsInfos();
        return ResponseEntity.ok(Map.of("groups", allGroupsInfos));
    }

    @GetMapping("/{groupName}")
    public ResponseEntity<GroupWithMembersResponse> getGroupWithMembers(@PathVariable String groupName) {
        GroupWithMembersResponse groupInfo = groupInfoUseCase.getGroupWithMembers(groupName);
        return ResponseEntity.ok(groupInfo);
    }

}
