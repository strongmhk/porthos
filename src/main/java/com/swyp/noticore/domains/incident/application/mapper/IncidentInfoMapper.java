package com.swyp.noticore.domains.incident.application.mapper;

import com.querydsl.core.Tuple;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentDetailResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentGroupMemberResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentGroupResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentInfoResponse;
import com.swyp.noticore.domains.incident.persistence.entity.*;
import com.swyp.noticore.global.constants.S3Constants;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IncidentInfoMapper {

    public static List<IncidentInfoResponse> mapNestedIncidentResponse(List<Tuple> results) {
        Map<Long, IncidentInfoResponse> incidentMap = new LinkedHashMap<>();

        for (Tuple tuple : results) {
            // index 기준: 0:incidentId, 1:title, 2:regTime, 3:closeTime, 4:groupId, 5:groupName, 6:memberId, 7:memberName, 8:isVerified
            Long incidentId = tuple.get(0, Long.class);
            String title = tuple.get(1, String.class);
            LocalDateTime registrationTime = tuple.get(2, LocalDateTime.class);
            LocalDateTime closingTime = tuple.get(3, LocalDateTime.class);
            Long groupId = tuple.get(4, Long.class);
            String groupName = tuple.get(5, String.class);
            Long memberId = tuple.get(6, Long.class);
            String memberName = tuple.get(7, String.class);
            Boolean isVerified = tuple.get(8, Boolean.class);

            // Incident 생성 또는 조회
            IncidentInfoResponse incident = incidentMap.computeIfAbsent(incidentId, id -> IncidentInfoResponse.builder()
                .id(id)
                .title(title)
                .registrationTime(registrationTime)
                .closingTime(closingTime)
                .groups(new ArrayList<>())
                .build());

            // Group 찾기 또는 생성
            List<IncidentGroupResponse> groups = incident.getGroups();
            IncidentGroupResponse group = groups.stream()
                .filter(g -> g.id().equals(groupId))
                .findFirst()
                .orElseGet(() -> {
                    IncidentGroupResponse g = IncidentGroupResponse.builder()
                        .id(groupId)
                        .name(groupName)
                        .members(new ArrayList<>())
                        .build();
                    groups.add(g);
                    return g;
                });

            // 중복 멤버 방지 후 추가
            if (group.members().stream().noneMatch(m -> m.id().equals(memberId))) {
                group.members().add(new IncidentGroupMemberResponse(
                    memberId,
                    memberName,
                    Boolean.TRUE.equals(isVerified)
                ));
            }
        }

        return new ArrayList<>(incidentMap.values());
    }

    public static IncidentDetailResponse mapToDetailResponse(List<Tuple> tuples) {
        Tuple first = tuples.get(0);
        Long incidentId = first.get(0, Long.class);
        String s3Uuid = first.get(1, String.class);
        String title = first.get(2, String.class);
        LocalDateTime registrationTime = first.get(3, LocalDateTime.class);
        LocalDateTime closingTime = first.get(4, LocalDateTime.class);
        String bucket = S3Constants.EML_BUCKET;

        Map<Long, IncidentGroupResponse> groupMap = new LinkedHashMap<>();

        for (Tuple tuple : tuples) {
            Long groupId = tuple.get(5, Long.class);
            String groupName = tuple.get(6, String.class);
            Long memberId = tuple.get(7, Long.class);
            String memberName = tuple.get(8, String.class);
            Boolean isVerified = Boolean.TRUE.equals(tuple.get(9, Boolean.class));

            groupMap
                .computeIfAbsent(groupId, id -> IncidentGroupResponse.builder()
                    .id(id)
                    .name(groupName)
                    .members(new ArrayList<>())
                    .build()
                )
                .members()
                .add(new IncidentGroupMemberResponse(memberId, memberName, isVerified));
        }

        return IncidentDetailResponse
            .builder()
            .incidentId(incidentId)
            .s3Uuid(s3Uuid)
            .title(title)
            .registrationTime(registrationTime)
            .closingTime(closingTime)
            .bucket(bucket)
            .groups(new ArrayList<>(groupMap.values()))
            .build();
    }
}

