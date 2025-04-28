package com.swyp.noticore.domains.errorinfo.application.usecase;

import com.swyp.noticore.domains.errorinfo.application.dto.response.MailContent;
import com.swyp.noticore.domains.errorinfo.domain.service.EmailService;
import com.swyp.noticore.domains.errorinfo.domain.service.EmlDownloadService;
import com.swyp.noticore.domains.errorinfo.domain.service.ErrorInfoParsingService;
import com.swyp.noticore.domains.errorinfo.domain.service.OncallService;
import com.swyp.noticore.domains.errorinfo.domain.service.SmsService;
import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import com.swyp.noticore.domains.member.application.mapper.MemberInfoMapper;
import com.swyp.noticore.domains.member.domain.service.GroupMemberService;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import com.swyp.noticore.global.constants.NationNumber;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@UseCase
@RequiredArgsConstructor
public class ErrorInfoUseCase {

    private final GroupMemberService groupMemberService;
    private final EmlDownloadService emlDownloadService;
    private final ErrorInfoParsingService errorInfoParsingService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final OncallService onCallService;

    public void processAndForward(Map<String, String> payload) {
        InputStream inputStream = emlDownloadService.download(payload);
        MailContent mailContent = errorInfoParsingService.parseAndValidate(inputStream);

        String subject = mailContent.subject();
        String groupName = subject.replaceAll(".*\\[GROUP:([^\\]]+)\\].*", "$1");

        List<MemberInfo> groupMemberInfos = groupMemberService.getGroupMemberInfos(groupName);

        // 1. 이메일 전송
        List<String> emailAddresses = MemberInfoMapper.mapToEmailAddresses(groupMemberInfos);

        emailService.sendEmailAlert(mailContent.originalMessage(), emailAddresses, subject);

        // 2. SMS 전송
        List<String> smsRecipients = MemberInfoMapper.mapToSmsRecipients(groupMemberInfos);

        if (!smsRecipients.isEmpty()) {
            for (String smsRecipient : smsRecipients) {
                smsService.sendSmsAlert(subject, formatKoreaPhoneNumber(smsRecipient));
            }
        }

        // 3. OnCall 전송
        List<String> oncallRecipients = MemberInfoMapper.mapToOncallRecipients(groupMemberInfos);

        if (!oncallRecipients.isEmpty()) {
            for (String oncallRecipient : oncallRecipients) {
                onCallService.triggerOnCall(subject, formatKoreaPhoneNumber(oncallRecipient));

            }
        }

        // 4. Slack 전송
        List<String> slackRecipients = MemberInfoMapper.mapToSlackRecipients(groupMemberInfos);

/*        if (!slackRecipients.isEmpty()) {
            for (String slackRecipient : slackRecipients) {
                slackService.sendSlackAlert(data, slackRecipient);
            }
        }*/

        // TODO: 장애 정보 DB Insert
    }

    private String formatKoreaPhoneNumber(String phoneNumber) {
        return NationNumber.KOREA.getValue() + phoneNumber.substring(1);
    }
}
