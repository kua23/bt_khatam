package com.app.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import static org.springframework.http.HttpMethod.*;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.app.product.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Security configuration for product-pricing-service
 * Implements RBAC with JWT authentication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configure(http)) // Enable CORS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - Swagger UI and API docs
                .requestMatchers(
                    "/swagger-ui/**", 
                    "/swagger-ui.html", 
                    "/v3/api-docs/**", 
                    "/api-docs/**",
                    "/api-docs.yaml",
                    "/health",
                    "/webjars/**"
                ).permitAll()
                // Product viewing - Accessible to all authenticated users
                .requestMatchers(GET, "/products/**", "/products/{id}", "/products/code/{code}")
                    .hasAnyRole("CUSTOMER", "MANAGER", "ADMIN")
                // Product management - Only MANAGER and ADMIN
                .requestMatchers(POST, "/products").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(PUT, "/products/{id}", "/products/{id}/status").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(DELETE, "/products/{id}").hasAnyRole("MANAGER", "ADMIN")
                // Product configuration - GET accessible to all, POST/PUT/DELETE only MANAGER/ADMIN
                .requestMatchers(GET, "/products/{productId}/charges/**").hasAnyRole("CUSTOMER", "MANAGER", "ADMIN")
                .requestMatchers(GET, "/products/{productId}/interest-rates/**").hasAnyRole("CUSTOMER", "MANAGER", "ADMIN")
                .requestMatchers(POST, "/products/{productId}/charges/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(POST, "/products/{productId}/interest-rates/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(PUT, "/products/{productId}/charges/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(DELETE, "/products/{productId}/charges/**").hasAnyRole("MANAGER", "ADMIN")
                // Other product config - MANAGER and ADMIN only
                .requestMatchers("/products/{productId}/roles/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/products/{productId}/balance-types/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/products/{productId}/transaction-types/**").hasAnyRole("MANAGER", "ADMIN")
                // Transaction-Balance relationships - GET for all, POST/PUT/DELETE for MANAGER/ADMIN
                .requestMatchers(GET, "/transaction-balance-relationships/**").hasAnyRole("CUSTOMER", "MANAGER", "ADMIN")
                .requestMatchers(POST, "/transaction-balance-relationships/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(PUT, "/transaction-balance-relationships/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(DELETE, "/transaction-balance-relationships/**").hasAnyRole("MANAGER", "ADMIN")
                // Charges direct management - GET for all, POST/PUT/DELETE for MANAGER/ADMIN
                .requestMatchers(GET, "/charges/**").hasAnyRole("CUSTOMER", "MANAGER", "ADMIN")
                .requestMatchers(PUT, "/charges/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(DELETE, "/charges/**").hasAnyRole("MANAGER", "ADMIN")
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
