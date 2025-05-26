package com.swyp.noticore.domains.incident.persistence.entity;

import com.swyp.noticore.domains.comment.persistence.entity.CommentEntity;
import com.swyp.noticore.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "incident_info")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class IncidentInfoEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "s3_uuid", nullable = false)
    private String s3Uuid;
    
    @Column(name = "raw_body", columnDefinition = "TEXT", nullable = true)
    private String rawBody;

    @Column(name = "title", nullable = false)
    private String title;

    private boolean completion;
    private LocalDateTime registrationTime;
    private LocalDateTime closingTime;
    
    @Builder.Default
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentGroupEntity> groups = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> commentEntities = new ArrayList<>();
}
