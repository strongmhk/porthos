package com.swyp.noticore.domains.incident.application.mapper;

import com.querydsl.core.Tuple;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentInfoResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

}

