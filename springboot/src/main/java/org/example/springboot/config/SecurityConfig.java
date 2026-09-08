package org.example.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/",
        "/health",
        "/favicon.ico",
        "/api/user/auth",
        "/api/user/login",
        "/api/user/register",
        "/api/user/forget",
        "/api/user/forget/code",
        "/api/user/add",
        "/static/**",
        "/files/**",
        "/*.html",
        "/file-test.html"
    };

    private static final String[] PUBLIC_GET_PATHS = {
        "/api/game/mainline-config",
        "/game/mainline-config",
        "/api/acupuncture/train-game/**",
        "/api/acupuncture/copper-man/acupoints",
        "/api/acupuncture/body-map/acupoints",
        "/api/acupuncture/copper-man/daily-case",
        "/api/acupuncture/copper-content/stories/**",
        "/api/skill/names",
        "/api/skill/count"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()
                .requestMatchers(
                    "/api/admin/**",
                    "/api/user/admin/**",
                    "/api/dashboard/**",
                    "/api/acupuncture/illness/**",
                    "/api/acupuncture/xuewei/**",
                    "/api/acupuncture/zhenjiu-tools/**",
                    "/api/acupuncture/extracourse/**",
                    "/api/acupuncture/doctor-story/**",
                    "/api/acupuncture/jingluo/**",
                    "/api/acupuncture/origin-story/**"
                ).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) -> {
                    // 区分匿名访问与携带失效会话：前者返回403，后者返回401便于前端清理会话。
                    boolean hasAuthorization = request.getHeader("Authorization") != null;
                    response.sendError(hasAuthorization
                            ? HttpStatus.UNAUTHORIZED.value()
                            : HttpStatus.FORBIDDEN.value(),
                            hasAuthorization ? "登录已过期，请重新登录" : "无权限访问");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
