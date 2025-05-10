package com.swyp.noticore.domains.member.application.mapper;

import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import java.util.List;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    private static final String SLACK_WEBHOOK_PATTERN =
        "^https://hooks\\.slack\\.com/services/[A-Z0-9]+/[A-Z0-9]+/[a-zA-Z0-9]+$";

    private static boolean isValidSlackWebhookUrl(String url) {
        return url != null && url.trim().matches(SLACK_WEBHOOK_PATTERN);
    }

    public static List<String> mapToSlackRecipients(List<MemberInfo> groupMemberInfos) {
        return groupMemberInfos.stream()
            .filter(memberInfo -> {
                if (!memberInfo.slackNoti()) return false;

                String url = memberInfo.slackUrl();
                if (!StringUtils.hasText(url)) return false;

                if (!isValidSlackWebhookUrl(url)) {
                    log.warn("Invalid Slack URL for member '{}': {}", memberInfo.name(), url);
                    return false;
                }

                return true;
            })
            .map(MemberInfo::slackUrl)
            .toList();
    }

}
