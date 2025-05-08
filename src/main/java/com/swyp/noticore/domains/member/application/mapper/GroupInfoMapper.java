package com.swyp.noticore.domains.member.application.mapper;

import com.querydsl.core.Tuple;
import com.swyp.noticore.domains.member.application.dto.response.GroupInfoResponse;
import com.swyp.noticore.domains.member.persistence.entity.QGroupInfoEntity;
import com.swyp.noticore.domains.member.persistence.entity.QMemberEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupInfoMapper {

    public static List<GroupInfoResponse> mapFromTuples(List<Tuple> tuples, QGroupInfoEntity group, QMemberEntity member) {

        Map<Long, GroupInfoResponse> map = new LinkedHashMap<>();

        for (Tuple t : tuples) {
            Long groupId = t.get(group.id);
            String groupName = t.get(group.name);
            String memberName = t.get(member.name);

            GroupInfoResponse dto = map.computeIfAbsent(groupId,
                id -> GroupInfoResponse.builder()
                    .id(id)
                    .name(groupName)
                    .members(new ArrayList<>())
                    .build()
            );
            dto.getMembers().add(memberName);
        }

        return new ArrayList<>(map.values());
    }
}
