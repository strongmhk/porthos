package com.swyp.noticore.domains.member.persistence.repository;

import com.swyp.noticore.domains.member.application.dto.response.GroupInfoResponse;
import java.util.List;

public interface MemberGroupQueryDslRepository {

    List<GroupInfoResponse> findAllGroupInfos();
}
