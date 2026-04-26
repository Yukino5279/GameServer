package com.sbeam.gameserver.config;

import com.sbeam.gameserver.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                //令服务器不存任何 Session 记录
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        //白名单
                        .requestMatchers(
                                "/api/players/login",
                                "/api/players/register",
                                "/api/players/register/verification-code",
                                "/api/players/verification-code",
                                "/api/players/refresh-token",
                                // --- Swagger/OpenAPI 专属放行路径 ---
                                "/v3/api-docs/**",         // 接口文档的 JSON 数据
                                "/swagger-ui/**",          // Swagger UI 的静态资源 (JS/CSS)
                                "/swagger-ui.html",        // Swagger UI 的入口 HTML
                                "/webjars/**"              // Swagger 依赖的一些网页资源
                        ).permitAll()
                        //除开白名单所有接口必须经过authenticated()验证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}