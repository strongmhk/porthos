package com.swyp.noticore.domains.incident.utils;

import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmailNoticeFormatter {

    // 인스턴스화 방지
    private EmailNoticeFormatter() {}

    /**
     * 장애 알림 이메일 본문에 들어갈 안내 메시지 생성
     *
     * @param memberInfoByGroup 존재하는 그룹명 → 해당 그룹의 멤버 목록
     * @param notFoundGroups    DB에 존재하지 않는 그룹명 목록
     * @param incidentId    장애 ID
     * @return 안내 메시지 문자열
     */
    public static String formatNotice(Map<String, List<MemberInfo>> memberInfoByGroup, List<String> notFoundGroups, Long incidentId) {
        StringBuilder builder = new StringBuilder();
        builder.append("※ 이 메일은 자동 전달된 장애 보고입니다.\n\n");
        builder.append("※ 본 장애에 대한 확인 기록은 아래 링크 클릭을 기준으로 처리됩니다. 반드시 접속해주세요.\n\n");
        builder.append("→ 확인 링크: https://www.noticore.co.kr/verify/")
               .append(incidentId)
               .append("\n\n");

        // 존재하는 그룹이 있을 경우
        if (memberInfoByGroup.isEmpty()) {
            builder.append("자동 알림 전파 그룹 : 없음. (입력된 그룹명을 다시 확인해주세요).\n");
        } else {
            builder.append("자동 알림 전파 그룹 : ");
            String groupLines = memberInfoByGroup.entrySet().stream()
                .map(entry -> {
                    String groupName = entry.getKey();
                    String memberNames = entry.getValue().stream()
                        .map(MemberInfo::name)
                        .collect(Collectors.joining(", "));
                    return groupName + " ( " + memberNames + " )";
                })
                .collect(Collectors.joining(" , "));
            builder.append(groupLines).append("\n");
        }

        // 존재하지 않는 그룹 정보
        if (!notFoundGroups.isEmpty()) {
            builder.append("확인되지 않은 그룹 : ");
            builder.append(String.join(", ", notFoundGroups));
        }

        return builder.toString();
    }
}
