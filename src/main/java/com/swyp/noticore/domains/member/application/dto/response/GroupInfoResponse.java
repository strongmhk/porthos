package com.swyp.noticore.domains.member.application.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoResponse {
    private Long id;
    private String name;
    private List<String> members = new ArrayList<>();
}
