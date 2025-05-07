package com.swyp.noticore.global.config.security.matcher;

import static com.swyp.noticore.domains.member.domain.constant.Role.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.DELETE;

import com.swyp.noticore.domains.member.domain.constant.Role;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class RequestMatcherHolder {

    private static final List<RequestInfo> REQUEST_INFO_LIST = List.of(

        // static resources
        new RequestInfo(GET, "/*.ico", null),
        new RequestInfo(GET, "/resources/**", null),
        new RequestInfo(GET, "/css/**", null),
        new RequestInfo(GET, "/js/**", null),
        new RequestInfo(GET, "/img/**", null),

        // error-info
        new RequestInfo(GET, "/api/test/**",null),
        new RequestInfo(POST, "/api/test/**",null),

        // auth
        new RequestInfo(POST, "/api/auth/login", null),
        new RequestInfo(POST, "/api/auth/logout", null),
        new RequestInfo(POST, "/api/auth/refresh", null),

        // swagger
        new RequestInfo(GET, "/api/test-token",null),
        new RequestInfo(GET, "/swagger-ui/**",null),
        new RequestInfo(GET, "/swagger-ui/index.html",null),
        new RequestInfo(GET, "/v3/api-docs/**",null),
        new RequestInfo(GET, "/swagger-resources/**",null),
        new RequestInfo(GET, "/webjars/**",null),
        new RequestInfo(GET, "/favicon.ico",null),

        // error-info
        new RequestInfo(GET, "/api/error-info/**",null),
        new RequestInfo(POST, "/api/error-info/**",null),

        // member
        new RequestInfo(POST, "/api/member", null),
        new RequestInfo(POST, "/api/member/get", null),
        new RequestInfo(PUT, "/api/member", null),
        new RequestInfo(DELETE, "/api/member", null),

        // 빌드 에러 방지를 위해 각 권한에 대한 RequestInfo가 최소 1개씩은 리스트에 있어야함
        // user
        new RequestInfo(GET, "/api/members/**", USER),
        new RequestInfo(GET, "/api/admin/**", ADMIN),
        new RequestInfo(GET, "/api/super/**", SUPER_ADMIN)
    );
    private final ConcurrentHashMap<String, RequestMatcher> reqMatcherCacheMap = new ConcurrentHashMap<>();

    /**
     * 최소 권한이 주어진 요청에 대한 RequestMatcher 반환
     * @param minRole 최소 권한 (Nullable)
     * @return 생성된 RequestMatcher
     */
    public RequestMatcher getRequestMatchersByMinPermission(@Nullable Role minRole) {
        var key = getKeyByRole(minRole);
        return reqMatcherCacheMap.computeIfAbsent(key, k ->
            new OrRequestMatcher(REQUEST_INFO_LIST.stream()
                .filter(reqInfo -> Objects.equals(reqInfo.minRole, minRole))
                .map(reqInfo -> new AntPathRequestMatcher(reqInfo.pattern(),
                    reqInfo.method().name()))
                .toArray(AntPathRequestMatcher[]::new)));
    }

    private String getKeyByRole(@Nullable Role minRole) {
        return minRole == null ? "VISITOR" : minRole.name();
    }

    private record RequestInfo(HttpMethod method, String pattern, Role minRole) {

    }
}