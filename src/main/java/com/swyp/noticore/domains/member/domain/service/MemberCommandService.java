package com.swyp.noticore.domains.member.domain.service;

import com.swyp.noticore.domains.auth.utils.PasswordEncoderUtil;
import com.swyp.noticore.domains.member.application.dto.request.MemberKeyRequest;
import com.swyp.noticore.domains.member.application.dto.request.MemberRequest;
import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import com.swyp.noticore.domains.member.persistence.entity.MemberGroupEntity;
import com.swyp.noticore.domains.member.persistence.entity.MemberMetadataEntity;
import com.swyp.noticore.domains.member.persistence.repository.MemberGroupRepository;
import com.swyp.noticore.domains.member.persistence.repository.MemberMetadataRepository;
import com.swyp.noticore.domains.member.persistence.repository.MemberRepository;
import com.swyp.noticore.domains.member.persistence.entity.GroupInfoEntity;
import com.swyp.noticore.domains.member.persistence.repository.GroupInfoRepository;
import com.swyp.noticore.domains.incident.persistence.repository.IncidentInfoRepository;
import com.swyp.noticore.domains.incident.persistence.entity.IncidentInfoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final MemberMetadataRepository memberMetadataRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final IncidentInfoRepository incidentInfoRepository;

    public void insert(MemberRequest request) {
        GroupInfoEntity group = groupInfoRepository.findByName(request.groupName())
        .orElseGet(() -> {
            log.info("존재하지 않아 새 그룹 생성: {}", request.groupName());
            GroupInfoEntity newGroup = GroupInfoEntity.builder()
                .name(request.groupName())
                .build();
            return groupInfoRepository.save(newGroup);
        });

        boolean exists = memberRepository.existsByGroupNameAndNameAndEmail(
            request.groupName(), request.name(), request.email());

        if (exists) throw new IllegalArgumentException("이미 등록된 멤버입니다.");

        MemberMetadataEntity metadata = MemberMetadataEntity.builder()
            .slackUrl(request.slackUrl())
            .slackNoti(Boolean.TRUE.equals(request.slackNoti()))
            .smsNoti(Boolean.TRUE.equals(request.smsNoti()))
            .oncallNoti(Boolean.TRUE.equals(request.oncallNoti()))
            .build();

        memberMetadataRepository.save(metadata);

        String rawPassword = request.password();
        String encodedPassword = PasswordEncoderUtil.encodePassword(rawPassword);

        MemberEntity member = MemberEntity.builder()
            .name(request.name())
            .email(request.email())
            .password(encodedPassword)
            .phone(request.phone())
            .role(request.role())
            .memberMetadata(metadata)
            .build();

        memberRepository.save(member);

        MemberGroupEntity mapping = MemberGroupEntity.builder()
            .member(member)
            .groupInfo(group)
            .build();

        memberGroupRepository.save(mapping);
    }

    public MemberInfo findMember(MemberKeyRequest request) {
        MemberEntity member = memberRepository.findByGroupNameAndNameAndEmail(
                request.groupName(), request.name(), request.email())
            .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        MemberMetadataEntity meta = member.getMemberMetadata();

        return new MemberInfo(
            member.getId(),
            member.getName(),
            member.getEmail(),
            member.getPhone(),
            meta.getSlackUrl(),
            meta.getSmsNoti(),
            meta.getOncallNoti(),
            meta.getSlackNoti()
        );
    }

    public void updateMember(MemberRequest request) {
        MemberEntity member = memberRepository.findByGroupNameAndNameAndEmail(
                request.groupName(), request.name(), request.email())
            .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        MemberMetadataEntity meta = member.getMemberMetadata();

        String rawPassword = request.password();

        if (request.phone() != null) member.setPhone(request.phone());
        if (rawPassword != null) {
            String encodedPassword = PasswordEncoderUtil.encodePassword(rawPassword);
            member.setPassword(encodedPassword); 
        }
        if (request.role() != null) member.setRole(request.role());

        if (request.smsNoti() != null) meta.setSmsNoti(request.smsNoti());
        if (request.oncallNoti() != null) meta.setOncallNoti(request.oncallNoti());
        if (request.slackNoti() != null) meta.setSlackNoti(request.slackNoti());
        if (request.slackUrl() != null) meta.setSlackUrl(request.slackUrl());
    }

    public void deleteMember(MemberKeyRequest request) {
        MemberEntity member = memberRepository.findByGroupNameAndNameAndEmail(
            request.groupName(), request.name(), request.email()
        ).orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        List<GroupInfoEntity> groups = member.getMemberGroups().stream()
            .map(MemberGroupEntity::getGroupInfo)
            .distinct()
            .toList();

        memberRepository.delete(member); // orphanRemoval 적용됨

        for (GroupInfoEntity group : groups) {
            long memberCount = memberGroupRepository.countByGroupInfoId(group.getId());

            if (memberCount == 0) {
                log.info("그룹에 남은 멤버가 없어 그룹 삭제: {}", group.getName());
                groupInfoRepository.delete(group);
            }
        }

        // 추가: group 없는 orphan incident 삭제
        List<IncidentInfoEntity> orphanIncidents = incidentInfoRepository.findAllWithNoGroups();
        if (!orphanIncidents.isEmpty()) {
            log.info("그룹 연결이 끊긴 장애 {}건 삭제", orphanIncidents.size());
            incidentInfoRepository.deleteAll(orphanIncidents);
        }
    }
}
