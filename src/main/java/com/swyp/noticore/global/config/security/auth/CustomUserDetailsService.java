package com.swyp.noticore.global.config.security.auth;

import com.swyp.noticore.domains.member.domain.service.MemberGetService;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberGetService memberGetService;

    @Override
    public CustomUserDetails loadUserByUsername(String email) {
        MemberEntity member = memberGetService.loadMemberByEmail(email);
        return new CustomUserDetails(member);
    }
}