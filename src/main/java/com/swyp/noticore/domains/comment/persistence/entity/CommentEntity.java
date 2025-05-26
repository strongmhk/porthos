package com.swyp.noticore.domains.comment.persistence.entity;

import com.swyp.noticore.domains.incident.persistence.entity.IncidentInfoEntity;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import com.swyp.noticore.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "comment")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class CommentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_info_id", nullable = false)
    private IncidentInfoEntity incidentInfoEntity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;
}
