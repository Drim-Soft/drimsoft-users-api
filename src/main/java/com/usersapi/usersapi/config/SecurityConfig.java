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

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${supabase.jwt.secret}")
    private String supabaseJwtSecret;

    /**
     * JwtDecoder que valida tokens HMAC usando el secreto de Supabase.
     * Verifica firma y "exp" por defecto.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Supabase usa HMAC SHA-256 para firmar los JWT (JWT_SECRET)
        byte[] secretBytes = supabaseJwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(secretBytes, "HmacSHA256");

        // Validadores adicionales (issuer/audience) si los quieres activar:
        // jwtDecoder.setJwtValidator(...);

        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    /**
     * Converter para convertir claims del JWT a GrantedAuthorities de Spring.
     * Ajusta el claimName ("role", "user_role", "app_metadata") según tu token.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // No dependemos del prefijo "SCOPE_" (a menos que uses scopes)
        defaultGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // prefix para roles

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // 1) roles en claim "role" o "user_role"
            if (jwt.getClaim("role") != null) {
                String role = jwt.getClaimAsString("role");
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }
            if (jwt.getClaim("user_role") != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("user_role").toUpperCase()));
            }

            // 2) custom claim que contenga una lista de roles: "roles" -> ["admin","editor"]
            Object rolesObj = jwt.getClaim("roles");
            if (rolesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) rolesObj;
                authorities.addAll(
                        roles.stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                                .toList()
                );
            }

            // 3) fallback: usar scope -> SCOPE_* mapping
            Collection<SimpleGrantedAuthority> scopeAuthorities = defaultGrantedAuthoritiesConverter.convert(jwt)
                    .stream()
                    .map(g -> new SimpleGrantedAuthority(g.getAuthority()))
                    .toList();
            authorities.addAll(scopeAuthorities);

            return authorities;
        });

        return converter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // API REST; si usas cookies, reevalúa
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/public/**", "/auth/**", "/actuator/health").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }
}