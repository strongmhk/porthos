package com.swyp.noticore.domains.member.persistence.repository;

import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import com.swyp.noticore.domains.member.persistence.entity.MemberGroupEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberGroupRepository extends JpaRepository<MemberGroupEntity, Long>, MemberGroupQueryDslRepository {

    @Query("""
        SELECT new com.swyp.noticore.domains.member.application.dto.response.MemberInfo(
            m.id,
            m.name,
            m.email,
            m.phone,
            mm.slackUrl,
            mm.smsNoti,
            mm.oncallNoti,
            mm.slackNoti
        )
        FROM MemberGroupEntity mg
        JOIN mg.member m
        JOIN m.memberMetadata mm
        JOIN mg.groupInfo gi
        WHERE gi.name = :groupName
    """)
    List<MemberInfo> findMemberInfoByGroupName(@Param("groupName") String groupName);

    long countByGroupInfoId(Long groupInfoId);
}
