package com.swyp.noticore.domains.incident.application.usecase;

import com.slack.api.webhook.Payload;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentDetailResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentInfoResponse;
import com.swyp.noticore.domains.incident.application.dto.response.MailContent;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentUpdateRequest;
import com.swyp.noticore.domains.incident.domain.service.*;
import com.swyp.noticore.domains.incident.utils.EmailNoticeFormatter;
import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import com.swyp.noticore.domains.member.application.mapper.MemberInfoMapper;
import com.swyp.noticore.domains.member.domain.service.GroupMemberQueryService;
import com.swyp.noticore.domains.incident.persistence.entity.IncidentInfoEntity;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import com.swyp.noticore.global.constants.NationNumber;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@UseCase
@RequiredArgsConstructor
public class IncidentInfoUseCase {

    private final GroupMemberQueryService groupMemberQueryService;
    private final EmlManagementService emlManagementService;
    private final IncidentInfoParsingService incidentInfoParsingService;
    private final IncidentCommandService incidentCommandService;
    private final IncidentQueryService incidentQueryService;
    private final NotificationLogCommandService notificationLogCommandService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final OncallService onCallService;
    private final SlackService slackService;
    private final com.swyp.noticore.domains.incident.domain.service.SlackMessageFormatter slackMessageFormatter;

    public void processAndForward(Map<String, String> payload) {
        // 1. S3에서 .eml 파일 다운로드 및 파싱
        InputStream inputStream = emlManagementService.getEmlFromS3(payload);
        String s3Key = payload.get("key");

        // 2. 제목 형식 검증 및 유효 그룹 검사 (내부에서 메일 반송 처리)
        MailContent mailContent = incidentInfoParsingService.parseAndValidate(inputStream);
        String subject = mailContent.subject();

        // 3. 제목에서 그룹명 파싱
        String groupSection = subject.replaceAll("(?i).*\\[emergency:([^\\]]+)\\].*", "$1").toLowerCase();
        List<String> parsedGroupNames = Arrays.stream(groupSection.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();

        // 4. 존재하는 그룹 필터링
        List<String> existingGroups = groupMemberQueryService.filterExistingGroupNames(parsedGroupNames);
        List<String> notFoundGroups = parsedGroupNames.stream()
                .filter(name -> !existingGroups.contains(name))
                .toList();

        // 5. 그룹별 멤버 정보 매핑
        Map<String, List<MemberInfo>> memberInfoByGroup = existingGroups.stream()
                .collect(Collectors.toMap(
                        groupName -> groupName,
                        groupMemberQueryService::getGroupMemberInfos,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        // 6. 자동 안내 메시지 생성
        String noticeMessage = EmailNoticeFormatter.formatNotice(memberInfoByGroup, notFoundGroups);

        // 7. incident_info + incident_group 저장 → incidentId 확보
        String title = subject.replaceAll("(?i).*\\[emergency:[^\\]]+\\]\\s*", "");
        Long incidentId = incidentCommandService.saveIncidentAndGroups(title, s3Key, existingGroups);

        // 8. 전체 수신 대상 집계
        List<MemberInfo> allMembers = memberInfoByGroup.values().stream()
                .flatMap(List::stream)
                .toList();

        // 9. 수신 대상별 notification_log 기록
        allMembers.forEach(member ->
                notificationLogCommandService.saveLog(incidentId, member.id())
        );

        // 10. Email 전송
        List<String> emailAddresses = MemberInfoMapper.mapToEmailAddresses(allMembers);
        emailService.sendEmailAlert(mailContent.originalMessage(), emailAddresses, subject, noticeMessage);

        // 11. SMS 전송
        MemberInfoMapper.mapToSmsRecipients(allMembers).stream()
                .map(this::formatKoreaPhoneNumber)
                .forEach(phone -> smsService.sendSmsAlert(subject, phone));

        // 12. OnCall 전송
        MemberInfoMapper.mapToOncallRecipients(allMembers).stream()
                .map(this::formatKoreaPhoneNumber)
                .forEach(phone -> onCallService.triggerOnCall(subject, phone));

        // 13. Slack 전송
        Payload slackPayload = slackMessageFormatter.formatGeneralErrorMessage(title);
        MemberInfoMapper.mapToSlackRecipients(allMembers).stream()
                .forEach(url -> {
                    try {
                        slackService.sendSlackAlert(slackPayload, url);
                    } catch (Exception e) {
                        log.error("Failed to send Slack alert to {}: {}", url, e.getMessage());
                    }
                });

    }

    public List<IncidentInfoResponse> getIncidentInfosByCompletion(boolean completion) {
        return incidentQueryService.getIncidentInfosByCompletion(completion);
    }

    public IncidentDetailResponse getIncidentDetail(Long incidentId) {
        return incidentQueryService.getIncidentDetail(incidentId);
    }

    public void verifyIncident(Long incidentId, Long memberId) {
        notificationLogCommandService.markAsVerified(incidentId, memberId);
    }

    private String formatKoreaPhoneNumber(String phoneNumber) {
        return NationNumber.KOREA.getValue() + phoneNumber.substring(1);
    }

    public void updateIncident(Long incidentId, IncidentUpdateRequest request) {
        IncidentInfoEntity incident = incidentCommandService.findById(incidentId);

        if (request.completion() != null) {
                boolean completed = request.completion();
                incident.setCompletion(completed);
                incident.setClosingTime(completed ? LocalDateTime.now() : null);
        }
    }
}
