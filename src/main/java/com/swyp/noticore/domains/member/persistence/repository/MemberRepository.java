package com.swyp.noticore.domains.member.persistence.repository;

import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByEmail(String email);

    @Query("""
        SELECT m
        FROM MemberEntity m
        JOIN MemberGroupEntity mg ON m.id = mg.member.id
        JOIN GroupInfoEntity g ON mg.groupInfo.id = g.id
        WHERE g.name = :groupName
          AND m.name = :name
          AND m.email = :email
    """)
    Optional<MemberEntity> findByGroupNameAndNameAndEmail(
        @Param("groupName") String groupName,
        @Param("name") String name,
        @Param("email") String email
    );

    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM MemberEntity m
        JOIN MemberGroupEntity mg ON m.id = mg.member.id
        JOIN GroupInfoEntity g ON mg.groupInfo.id = g.id
        WHERE g.name = :groupName
          AND m.name = :name
          AND m.email = :email
    """)
    boolean existsByGroupNameAndNameAndEmail(
        @Param("groupName") String groupName,
        @Param("name") String name,
        @Param("email") String email
    );
}
