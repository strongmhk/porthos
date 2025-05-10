package com.swyp.noticore.global.config.security;

import static com.swyp.noticore.domains.member.domain.constant.Role.ADMIN;
import static com.swyp.noticore.domains.member.domain.constant.Role.SUPER_ADMIN;
import static com.swyp.noticore.domains.member.domain.constant.Role.USER;

import com.swyp.noticore.global.config.security.filter.JwtAuthenticationFilter;
import com.swyp.noticore.global.config.security.handler.CustomAccessDeniedHandler;
import com.swyp.noticore.global.config.security.handler.CustomAuthenticationEntryPoint;
import com.swyp.noticore.global.config.security.jwt.JwtUtils;
import com.swyp.noticore.global.config.security.matcher.RequestMatcherHolder;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RequestMatcherHolder requestMatcherHolder;
    private final JwtUtils jwtUtils;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    protected SecurityFilterChain config(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(corsConfigurer -> corsConfigurer
                .configurationSource(corsConfigurationSource())
            )
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(requestMatcherHolder.getRequestMatchersByMinPermission(null)).permitAll()
                .requestMatchers(requestMatcherHolder.getRequestMatchersByMinPermission(USER))
                .hasAnyAuthority(USER.name(), ADMIN.name(), SUPER_ADMIN.name())
                .requestMatchers(requestMatcherHolder.getRequestMatchersByMinPermission(ADMIN))
                .hasAnyAuthority(ADMIN.name(), SUPER_ADMIN.name())
                .requestMatchers(requestMatcherHolder.getRequestMatchersByMinPermission(SUPER_ADMIN))
                .hasAnyAuthority(SUPER_ADMIN.name())
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtils, requestMatcherHolder), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // configuration.setAllowedOrigins(Arrays.asList(
        //     "http://localhost:3000",
        //     "http://localhost:8080",
        //     "http://localhost:5173"
        // ));
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setAllowedMethods(Arrays.asList(
            "GET",
            "POST",
            "DELETE",
            "PATCH",
            "OPTIONS"
        ));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
