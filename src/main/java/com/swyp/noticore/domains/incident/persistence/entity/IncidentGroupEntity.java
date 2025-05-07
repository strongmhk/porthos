package com.swyp.noticore.domains.incident.persistence.entity;

import com.swyp.noticore.domains.member.persistence.entity.GroupInfoEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "incident_group",
       uniqueConstraints = @UniqueConstraint(columnNames = {"incident_info_id", "group_info_id"})
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class IncidentGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_info_id", nullable = false)
    private IncidentInfoEntity incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_info_id", nullable = false)
    private GroupInfoEntity groupInfo;
}
