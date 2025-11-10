package com.usersapi.usersapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${supabase.jwt.secret}")
    private String supabaseJwtSecret;

    // Configuración CORS para permitir peticiones desde el frontend
    // (localhost:3000)

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache preflight requests for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // JWT Decoder para validar tokens firmados por Supabase
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] secretBytes = supabaseJwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(secretBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    // Conversor de roles desde claims del JWT

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        defaultGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            if (jwt.getClaim("role") != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("role").toUpperCase()));
            }

            if (jwt.getClaim("user_role") != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("user_role").toUpperCase()));
            }

            Object rolesObj = jwt.getClaim("roles");
            if (rolesObj instanceof List<?> roles) {
                for (Object role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()));
                }
            }

            authorities.addAll(defaultGrantedAuthoritiesConverter.convert(jwt));
            return authorities;
        });

        return converter;
    }

    // Filtro de seguridad principal

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/public/**", "/auth/**", "/actuator/**", "/api/v1/actuator/**")
                        .permitAll()
                        .requestMatchers("/tickets/**", "/ticket-status/**").permitAll()
                        .requestMatchers("/users/**", "/roles/**", "/user-status/**").authenticated()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }
}
