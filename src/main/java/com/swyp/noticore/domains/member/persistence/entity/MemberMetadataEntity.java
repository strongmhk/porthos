package com.swyp.noticore.domains.member.persistence.entity;

import com.swyp.noticore.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "member_metadata")
public class MemberMetadataEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String slackUrl;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Boolean slackNoti;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Boolean smsNoti;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Boolean oncallNoti;
}
