package com.utms.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF'i devre dışı bırak (H2 ve API testleri için genelde istenir)
                .csrf(csrf -> csrf.disable())

                // 2. H2 Console için Frame izinlerini ayarla
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                // 3. İstek İzinleri (Sıralama Önemlidir!)
                .authorizeHttpRequests(auth -> auth
                        // Swagger ve H2-Console için herkese izin ver
                        .requestMatchers(
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        // Rol bazlı kısıtlamalar
                        .requestMatchers("/applications/submit", "/applications/student/**").hasRole("STUDENT")
                        .requestMatchers("/applications/oidb/**", "/registrar/**").hasRole("OIDB")
                        .requestMatchers("/faculty/**").hasRole("FACULTY")
                        .requestMatchers("/ygk/**").hasRole("YGK")

                        // Geri kalan tüm istekler için kimlik doğrulaması şart
                        .anyRequest().authenticated()
                )

                // 4. Basic Auth (Postman veya Browser testi için)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}