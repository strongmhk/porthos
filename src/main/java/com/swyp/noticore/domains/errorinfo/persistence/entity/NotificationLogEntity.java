package com.swyp.noticore.domains.errorinfo.persistence.entity;

import com.swyp.noticore.global.entity.BaseTimeEntity;
import com.swyp.noticore.domains.errorinfo.model.NotificationMethod;
import com.swyp.noticore.domains.errorinfo.model.NotificationStatus;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import lombok.Builder;

@Entity
@Table(name = "notification_log")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class NotificationLogEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentInfoEntity incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
}
