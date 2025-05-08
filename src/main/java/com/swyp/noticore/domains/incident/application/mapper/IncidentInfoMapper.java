package com.swyp.noticore.domains.incident.application.mapper;

import com.querydsl.core.Tuple;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentDetailResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentGroupMemberResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentGroupResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentInfoResponse;
import com.swyp.noticore.global.constants.S3Constants;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IncidentInfoMapper {

    public static List<IncidentInfoResponse> mapFrom(
        List<Tuple> groupTuples,
        List<Tuple> totalMemberTuples,
        List<Tuple> verifiedTuples
    ) {
        Map<Long, IncidentInfoResponse.IncidentInfoResponseBuilder> map = new HashMap<>();

        // group 정보와 기본 incident 데이터 초기화
        for (Tuple t : groupTuples) {
            Long incidentId = t.get(0, Long.class);
            String title = t.get(1, String.class);
            LocalDateTime registrationTime = t.get(2, LocalDateTime.class);
            LocalDateTime closeTime = t.get(3, LocalDateTime.class);
            String groupName = t.get(4, String.class);

            // builder 생성 또는 가져오기
            IncidentInfoResponse.IncidentInfoResponseBuilder builder = map.computeIfAbsent(incidentId, id ->
                IncidentInfoResponse.builder()
                    .id(id)
                    .title(title)
                    .registrationTime(registrationTime)
                    .closingTime(closeTime)
                    .groupNames(new ArrayList<>())
            );

            // 중복 방지 후 group name 추가
            List<String> groupNames = builder.build().getGroupNames();
            if (groupName != null && !groupNames.contains(groupName)) {
                groupNames.add(groupName);
            }
        }

        // totalMemberCount 설정
        for (Tuple t : totalMemberTuples) {
            Long incidentId = t.get(0, Long.class);
            Long count = t.get(1, Long.class);

            IncidentInfoResponse.IncidentInfoResponseBuilder builder = map.get(incidentId);
            if (builder != null) {
                builder.totalMemberCount(count);
            }
        }

        // verifiedCount 설정
        for (Tuple t : verifiedTuples) {
            Long incidentId = t.get(0, Long.class);
            Long count = t.get(1, Long.class);

            IncidentInfoResponse.IncidentInfoResponseBuilder builder = map.get(incidentId);
            if (builder != null) {
                builder.verifiedCount(count);
            }
        }

        // 최종 build
        return map.values().stream()
            .map(IncidentInfoResponse.IncidentInfoResponseBuilder::build)
            .collect(Collectors.toList());
    }

    public static IncidentDetailResponse mapToDetailResponse(List<Tuple> tuples) {

        Tuple first = tuples.get(0);
        Long incidentId = first.get(0, Long.class);
        String s3Uuid = first.get(1, String.class);
        String title = first.get(2, String.class);
        String bucket = S3Constants.EML_BUCKET;

        Map<Long, IncidentGroupResponse> groupMap = new LinkedHashMap<>();

        for (Tuple tuple : tuples) {
            Long groupId = tuple.get(3, Long.class);
            String groupName = tuple.get(4, String.class);
            Long memberId = tuple.get(5, Long.class);
            String memberName = tuple.get(6, String.class);
            Boolean isVerified = Boolean.TRUE.equals(tuple.get(7, Boolean.class));

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
            .bucket(bucket)
            .s3Uuid(s3Uuid)
            .title(title)
            .groups(new ArrayList<>(groupMap.values()))
            .build();
    }
}

