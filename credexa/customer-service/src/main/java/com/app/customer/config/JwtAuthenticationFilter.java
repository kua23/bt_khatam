package com.app.customer.config;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.common.util.JwtUtil;
import com.app.customer.dto.AuthenticatedUser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Authentication Filter for customer-service
 * TEMPORARILY DISABLED for inter-service communication
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @SuppressWarnings("unused")
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                if (jwtUtil.validateToken(jwt)) {
                    String username = jwtUtil.extractUsername(jwt);
                    List<String> roles = jwtUtil.extractRoles(jwt);
                    
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    
                    // Create AuthenticatedUser from JWT claims
                    AuthenticatedUser user = AuthenticatedUser.builder()
                            .username(username)
                            .id(null) // We'll populate this from customer repository
                            .isAdmin(roles.contains("ROLE_ADMIN"))
                            .isManager(roles.contains("ROLE_CUSTOMER_MANAGER"))
                            .build();

                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Set authentication for user: {} with roles: {}", username, roles);
                }
            }
        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}
