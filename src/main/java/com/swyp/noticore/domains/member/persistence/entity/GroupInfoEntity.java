package com.swyp.noticore.domains.member.persistence.entity;

import com.swyp.noticore.global.entity.BaseTimeEntity;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Builder;


@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "group_info")
public class GroupInfoEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "groupInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberGroupEntity> memberGroups = new ArrayList<>();
}
