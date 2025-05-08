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

        // incident 정보 + groupNames 조회
        List<Tuple> incidentGroupTuples = jpaQueryFactory
            .select(
                incident.id,
                incident.title,
                incident.registrationTime,
                incident.closingTime,
                group.name
            )
            .from(incident)
            .leftJoin(incident.groups, incidentGroup)
            .leftJoin(incidentGroup.groupInfo, group)
            .where(completionEq(completion))
            .fetch();

        // incident별 totalMemberCount
        List<Tuple> memberCountTuples = jpaQueryFactory
            .select(
                incident.id,
                member.id.countDistinct()
            )
            .from(incident)
            .join(incident.groups, incidentGroup)
            .join(incidentGroup.groupInfo, group)
            .join(memberGroup).on(memberGroup.groupInfo.id.eq(group.id))
            .join(memberGroup.member, member)
            .where(completionEq(completion))
            .groupBy(incident.id)
            .fetch();

        // incident별 verifiedCount
        List<Tuple> verifiedCountTuples = jpaQueryFactory
            .select(
                log.incident.id,
                log.member.id.count()
            )
            .from(log)
            .where(log.isVerified.isTrue())
            .groupBy(log.incident.id)
            .fetch();

        return IncidentInfoMapper.mapFrom(
            incidentGroupTuples,
            memberCountTuples,
            verifiedCountTuples
        );
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
                incident.id,
                incident.s3Uuid,
                incident.title,
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
