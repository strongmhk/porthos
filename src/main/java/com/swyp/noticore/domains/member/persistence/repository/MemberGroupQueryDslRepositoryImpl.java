package com.swyp.noticore.domains.member.persistence.repository;

import static com.swyp.noticore.domains.member.persistence.entity.QGroupInfoEntity.*;
import static com.swyp.noticore.domains.member.persistence.entity.QMemberEntity.*;
import static com.swyp.noticore.domains.member.persistence.entity.QMemberGroupEntity.*;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swyp.noticore.domains.member.application.dto.response.GroupInfoResponse;
import com.swyp.noticore.domains.member.application.mapper.GroupInfoMapper;
import com.swyp.noticore.domains.member.persistence.entity.QGroupInfoEntity;
import com.swyp.noticore.domains.member.persistence.entity.QMemberEntity;
import com.swyp.noticore.domains.member.persistence.entity.QMemberGroupEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberGroupQueryDslRepositoryImpl implements MemberGroupQueryDslRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GroupInfoResponse> findAllGroupInfos() {
        QGroupInfoEntity group = groupInfoEntity;
        QMemberGroupEntity memberGroup = memberGroupEntity;
        QMemberEntity member = memberEntity;

        List<Tuple> result = jpaQueryFactory
            .select(group.id, group.name, member.name)
            .from(memberGroup)
            .join(memberGroup.groupInfo, group)
            .join(memberGroup.member, member)
            .fetch();

        return GroupInfoMapper.mapFromTuples(result, group, member);
    }
}
