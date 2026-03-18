package com.swyp.noticore.domains.incident.application.usecase;

import com.swyp.noticore.domains.incident.application.dto.response.IncidentDetailResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentInfoResponse;
import com.swyp.noticore.domains.incident.application.dto.response.MailContent;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentUpdateRequest;
import com.swyp.noticore.domains.incident.application.event.NotificationEvent;
import com.swyp.noticore.domains.incident.domain.service.*;
import com.swyp.noticore.domains.incident.utils.EmailNoticeFormatter;
import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import com.swyp.noticore.domains.member.application.mapper.MemberInfoMapper;
import com.swyp.noticore.domains.member.domain.service.GroupMemberQueryService;
import com.swyp.noticore.domains.incident.persistence.entity.IncidentInfoEntity;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    public void processAndForward(Map<String, String> payload) {
        // 1. S3에서 .eml 파일 다운로드 및 파싱
        InputStream inputStream = emlManagementService.getEmlFromS3(payload);
        String s3Key = payload.get("key");

        // 2. 제목 형식 검증 및 유효 그룹 검사 (내부에서 메일 반송 처리)
        MailContent mailContent = incidentInfoParsingService.parseAndValidate(inputStream);
        String subject = mailContent.subject();
        String rawBody = mailContent.rawBody();

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

        // 6. incident_info + incident_group 저장 → incidentId 확보
        String title = subject.replaceAll("(?i).*\\[emergency:[^\\]]+\\]\\s*", "");
        Long incidentId = incidentCommandService.saveIncidentAndGroups(rawBody, title, s3Key, existingGroups);

        // 7. 자동 안내 메시지 생성
        String noticeMessage = EmailNoticeFormatter.formatNotice(memberInfoByGroup, notFoundGroups, incidentId);

        // 8. 전체 수신 대상 집계
        List<MemberInfo> allMembers = memberInfoByGroup.values().stream()
                .flatMap(List::stream)
                .toList();

        // 9. 수신 대상별 notification_log 기록
        allMembers.forEach(member ->
                notificationLogCommandService.saveLog(incidentId, member.id())
        );

        // 10. 알림 전송 이벤트 발행 (트랜잭션 커밋 후 비동기 처리)
        eventPublisher.publishEvent(new NotificationEvent(
                mailContent.originalMessage(),
                subject,
                title,
                noticeMessage,
                allMembers
        ));
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

    public void updateIncident(Long incidentId, IncidentUpdateRequest request) {
        IncidentInfoEntity incident = incidentCommandService.findById(incidentId);

        if (request.completion() != null) {
                boolean completed = request.completion();
                incident.setCompletion(completed);
                incident.setClosingTime(completed ? LocalDateTime.now() : null);
        }
    }
}
