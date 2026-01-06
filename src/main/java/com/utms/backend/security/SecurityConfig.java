package com.utms.backend.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CSRF kapalı (Swagger + H2)
                .csrf(csrf -> csrf.disable())

                // H2 Console frame izinleri
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin())
                )

                // JWT filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // URL yetkilendirme
                .authorizeHttpRequests(auth -> auth

                        // Swagger + H2 herkese açık
                        .requestMatchers(
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        // Auth
                        .requestMatchers("/auth/**").permitAll()

                        // Rol bazlı endpointler
                        .requestMatchers("/applications/submit", "/applications/student/**")
                        .hasRole("STUDENT")

                        .requestMatchers("/applications/oidb/**", "/oidb/**")
                        .hasRole("OIDB")

                        .requestMatchers("/faculty/**").hasRole("FACULTY")
                        .requestMatchers("/ygk/**").hasRole("YGK")
                        .requestMatchers("/ydyo/**").hasRole("YDYO")

                        // Her şey authentication ister
                        .anyRequest().authenticated()
                );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
