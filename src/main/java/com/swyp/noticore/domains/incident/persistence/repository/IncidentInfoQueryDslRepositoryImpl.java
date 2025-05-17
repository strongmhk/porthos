package com.swyp.noticore.domains.incident.persistence.repository;

import static com.swyp.noticore.domains.incident.persistence.entity.QIncidentGroupEntity.incidentGroupEntity;
import static com.swyp.noticore.domains.incident.persistence.entity.QIncidentInfoEntity.incidentInfoEntity;
import static com.swyp.noticore.domains.incident.persistence.entity.QNotificationLogEntity.notificationLogEntity;
import static com.swyp.noticore.domains.member.persistence.entity.QGroupInfoEntity.groupInfoEntity;
import static com.swyp.noticore.domains.member.persistence.entity.QMemberEntity.memberEntity;
import static com.swyp.noticore.domains.member.persistence.entity.QMemberGroupEntity.memberGroupEntity;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentDetailResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentInfoResponse;
import com.swyp.noticore.domains.incident.application.mapper.IncidentInfoMapper;
import com.swyp.noticore.domains.incident.persistence.entity.QIncidentGroupEntity;
import com.swyp.noticore.domains.incident.persistence.entity.QIncidentInfoEntity;
import com.swyp.noticore.domains.incident.persistence.entity.QNotificationLogEntity;
import com.swyp.noticore.domains.member.persistence.entity.QGroupInfoEntity;
import com.swyp.noticore.domains.member.persistence.entity.QMemberEntity;
import com.swyp.noticore.domains.member.persistence.entity.QMemberGroupEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IncidentInfoQueryDslRepositoryImpl implements IncidentInfoQueryDslRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<IncidentInfoResponse> findIncidentInfosByCompletion(boolean completion) {
        QIncidentInfoEntity incident = incidentInfoEntity;
        QIncidentGroupEntity incidentGroup = incidentGroupEntity;
        QGroupInfoEntity group = groupInfoEntity;
        QNotificationLogEntity log = notificationLogEntity;
        QMemberEntity member = memberEntity;
        QMemberGroupEntity memberGroup = memberGroupEntity;

        List<Tuple> results = jpaQueryFactory
            .select(
                incident.id,
                incident.title,
                incident.registrationTime,
                incident.closingTime,
                group.id,
                group.name,
                member.id,
                member.name,
                log.isVerified
            )
            .from(incident)
            .join(incident.groups, incidentGroup)
            .join(incidentGroup.groupInfo, group)
            .join(group.memberGroups, memberGroup)
            .join(memberGroup.member, member)
            .leftJoin(log)
                .on(log.incident.eq(incident).and(log.member.eq(member)))
            .where(completionEq(completion))
            .fetch();

        return IncidentInfoMapper.mapNestedIncidentResponse(results);
    }

    @Override
    public IncidentDetailResponse findIncidentDetailById(Long incidentId) {
        QIncidentInfoEntity incident = incidentInfoEntity;
        QIncidentGroupEntity incidentGroup = incidentGroupEntity;
        QGroupInfoEntity group = groupInfoEntity;
        QMemberGroupEntity memberGroup = memberGroupEntity;
        QMemberEntity member = memberEntity;
        QNotificationLogEntity log = notificationLogEntity;

        List<Tuple> results = jpaQueryFactory
            .select(
                incident.id,                // 0
                incident.s3Uuid,            // 1
                incident.rawBody,           // 2
                incident.title,             // 3
                incident.registrationTime,  // 4 
                incident.closingTime,       // 5 
                group.id,                   // 6
                group.name,                 // 7
                member.id,                  // 8
                member.name,                // 9
                log.isVerified              // 10
            )
            .from(incident)
            .join(incident.groups, incidentGroup)
            .join(incidentGroup.groupInfo, group)
            .join(group.memberGroups, memberGroup)
            .join(memberGroup.member, member)
            .leftJoin(log)
                .on(log.incident.eq(incident).and(log.member.eq(member)))
            .where(incidentIdEq(incidentId))
            .fetch();

        return IncidentInfoMapper.mapToDetailResponse(results);
    }

    private static BooleanExpression incidentIdEq(Long incidentId) {
        return incidentInfoEntity.id.eq(incidentId);
    }

    private static BooleanExpression completionEq(boolean completion) {
        return incidentInfoEntity.completion.eq(completion);
    }
}
