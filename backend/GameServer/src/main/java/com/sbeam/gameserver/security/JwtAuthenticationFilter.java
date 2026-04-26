package com.sbeam.gameserver.security;

import com.sbeam.gameserver.entity.Player;
import com.sbeam.gameserver.repository.PlayerRepository;
import com.sbeam.gameserver.service.JwtService;
import com.sbeam.gameserver.service.JwtTokenStoreService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final PlayerRepository playerRepository;
    private final JwtTokenStoreService jwtTokenStoreService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   PlayerRepository playerRepository,
                                   JwtTokenStoreService jwtTokenStoreService) {
        this.jwtService = jwtService;
        this.playerRepository = playerRepository;
        this.jwtTokenStoreService = jwtTokenStoreService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        //直接放行未带token的请求
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        //获取token
        String token = authHeader.substring(7);
        try {
            String email = jwtService.extractEmail(token);
            Player player = playerRepository.findByEmail(email).orElse(null);
            String tokenInRedis = jwtTokenStoreService.getAccessToken(email);

            if (player != null
                    && jwtService.isAccessTokenValid(token, email)  //票据过期状态、签名一致
                    && token.equals(tokenInRedis)                   //票据和 Redis 里存的一致
                    && SecurityContextHolder.getContext().getAuthentication() == null) {    //是否已登录

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // token 非法时不抛出异常，交给 Spring Security 做未登录处理
        }

        filterChain.doFilter(request, response);
    }
}