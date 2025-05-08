package com.swyp.noticore.domains.incident.persistence.entity;

import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import com.swyp.noticore.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notification_log")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NotificationLogEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_info_id", nullable = false)
    private IncidentInfoEntity incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
