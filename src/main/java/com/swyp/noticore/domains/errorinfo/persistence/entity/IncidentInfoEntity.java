package com.swyp.noticore.domains.errorinfo.persistence.entity;

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

    private LocalDateTime completion;
    private LocalDateTime registrationTime;
    private LocalDateTime closingTime;
    
    @Builder.Default
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentGroupEntity> groups = new ArrayList<>();
}
