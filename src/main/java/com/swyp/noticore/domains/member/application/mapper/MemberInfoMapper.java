package com.swyp.noticore.domains.member.application.mapper;

import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import java.util.List;
import org.springframework.util.StringUtils;

public class MemberInfoMapper {

    public static List<String> mapToEmailAddresses(List<MemberInfo> groupMemberInfos) {
        return groupMemberInfos.stream()
            .filter(memberInfo -> StringUtils.hasText(memberInfo.email()))  // email 주소가 null, "", " "인 경우 필터링
            .map(MemberInfo::email)
            .toList();
    }

    public static List<String> mapToSmsRecipients(List<MemberInfo> groupMemberInfos) {
        return groupMemberInfos.stream()
            .filter(memberInfo -> memberInfo.smsNoti() && StringUtils.hasText(memberInfo.phone()))
            .map(MemberInfo::phone)
            .toList();
    }

    public static List<String> mapToOncallRecipients(List<MemberInfo> groupMemberInfos) {
        return groupMemberInfos.stream()
            .filter(memberInfo -> memberInfo.oncallNoti() && StringUtils.hasText(memberInfo.phone()))
            .map(MemberInfo::phone)
            .toList();
    }

    public static List<String> mapToSlackRecipients(List<MemberInfo> groupMemberInfos) {
        return groupMemberInfos.stream()
            .filter(memberInfo -> memberInfo.slackNoti() && StringUtils.hasText(memberInfo.slackUrl()))
            .map(MemberInfo::slackUrl)
            .toList();
    }
}
