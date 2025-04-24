package com.swyp.noticore.domains.member.domain.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@RequiredArgsConstructor
public enum Role {

    USER("사용자"),
    ADMIN("관리자"),
    SUPER_ADMIN("최고관리자");

    private final String description;

    @JsonCreator // Json -> Object, 역직렬화 수행하는 메서드
    public static Role from(String param) {
        for (Role role : Role.values()) {
            if (role.name().equals(param)) {
                return role;
            }
        }
        return null;
    }
}