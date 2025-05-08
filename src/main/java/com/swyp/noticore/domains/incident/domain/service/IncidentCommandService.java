package com.swyp.noticore.domains.incident.domain.service;

import com.swyp.noticore.domains.incident.persistence.entity.IncidentGroupEntity;
import com.swyp.noticore.domains.incident.persistence.entity.IncidentInfoEntity;
import com.swyp.noticore.domains.incident.persistence.repository.IncidentGroupRepository;
import com.swyp.noticore.domains.incident.persistence.repository.IncidentInfoRepository;
import com.swyp.noticore.domains.member.persistence.repository.GroupInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class IncidentCommandService {

    private final IncidentInfoRepository incidentInfoRepository;
    private final IncidentGroupRepository incidentGroupRepository;
    private final GroupInfoRepository groupInfoRepository;

    /**
     * 장애 및 관련 그룹 저장
     *
     * @param s3Key       S3 UUID (eml 파일 키)
     * @param groupNames  유효 그룹명 리스트
     * @return 저장된 incident_id
     */
    public Long saveIncidentAndGroups(String title, String s3Key, List<String> groupNames) {
        // 1. Incident 저장
        IncidentInfoEntity incident = IncidentInfoEntity.builder()
                .title(title)
                .s3Uuid(s3Key)
                .completion(false)
                .registrationTime(LocalDateTime.now())
                .build();

        IncidentInfoEntity savedIncident = incidentInfoRepository.save(incident);

        // 2. group 이름으로 ID 조회 후 연결 테이블에 저장
        List<IncidentGroupEntity> groupEntities = groupInfoRepository.findByNameIn(groupNames).stream()
                .map(group -> IncidentGroupEntity.builder()
                        .incident(savedIncident)
                        .groupInfo(group)
                        .build())
                .collect(Collectors.toList());

        incidentGroupRepository.saveAll(groupEntities);

        log.info("Incident saved. ID={}, Groups={}", savedIncident.getId(), groupNames);
        return savedIncident.getId();
    }
    
    @Transactional(readOnly = true)
    public IncidentInfoEntity findById(Long id) {
         return incidentInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 장애를 찾을 수 없습니다. id=" + id));
    }
}
