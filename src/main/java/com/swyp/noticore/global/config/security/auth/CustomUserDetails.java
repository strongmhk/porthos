package com.swyp.noticore.global.config.security.auth;

import static com.swyp.noticore.domains.member.domain.constant.Role.ADMIN;
import static com.swyp.noticore.domains.member.domain.constant.Role.SUPER_ADMIN;
import static com.swyp.noticore.domains.member.domain.constant.Role.USER;

import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final transient MemberEntity member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (SUPER_ADMIN.equals(member.getRole())) {
            return Collections.singletonList(new SimpleGrantedAuthority(SUPER_ADMIN.name()));
        }
        if (ADMIN.equals(member.getRole())) {
            return Collections.singletonList(new SimpleGrantedAuthority(ADMIN.name()));
        }
        return Collections.singletonList(new SimpleGrantedAuthority(USER.name()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
