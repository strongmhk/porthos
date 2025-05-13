package com.swyp.noticore.domains.incident.application.usecase;

import com.swyp.noticore.domains.incident.domain.service.NotificationLogQueryService;
import com.swyp.noticore.domains.incident.domain.service.OncallService;
import com.swyp.noticore.domains.incident.domain.service.SmsService;
import com.swyp.noticore.domains.incident.persistence.entity.NotificationLogEntity;
import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import com.swyp.noticore.global.constants.NationNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Transactional
@UseCase
@RequiredArgsConstructor
public class ResendNotificationUseCase {

    private final NotificationLogQueryService notificationLogQueryService;
    private final OncallService oncallService;
    private final SmsService smsService;

    public void resendNotification (Long incidentId, MemberInfo member, String subject) {
        AtomicInteger count = new AtomicInteger(0);
        // 비동기 처리
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        scheduledExecutorService.scheduleAtFixedRate(() -> {
                try {
                    NotificationLogEntity notiLog = notificationLogQueryService.getNotificationLog(incidentId, member.id());
                    String phone = formatKoreaPhoneNumber(member.phone());

                    if (count.get() >= 3 || notiLog.isVerified()) {
                        log.info("[[Member: {}] 알림 재전송을 마칩니다.", member.id());
                        scheduledExecutorService.shutdown();
                        return;
                    }

                    if (notiLog.isVerified()) {
                        if(member.oncallNoti()) {
                            oncallService.triggerOnCall(subject, phone);
                            log.info("[Member: {}] OnCall 알림을 재전송하였습니다. ({}번째)", member.id(), count.get()+1);
                        }

                        if(member.smsNoti()) {
                            smsService.sendSmsAlert(subject, phone);
                            log.info("[Member: {}] SMS 알림을 재전송하였습니다. ({}번째)", member.id(), count.get()+1);
                        }
                    }

                    count.incrementAndGet();

                } catch (Exception e) {
                    log.error("Member {} 알림 전송 중 에러 발생: {}", member.id(), e.getMessage());
                }
        },0,5, TimeUnit.MINUTES);
    }

    private String formatKoreaPhoneNumber(String phoneNumber) {
        return NationNumber.KOREA.getValue() + phoneNumber.substring(1);
    }
}
