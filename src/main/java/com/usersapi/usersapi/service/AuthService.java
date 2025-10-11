package com.usersapi.usersapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AuthService {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String anonKey;

    public AuthService(@Value("${supabase.url}") String supabaseUrl,
                       @Value("${supabase.anon.key}") String anonKey,
                       WebClient.Builder webClientBuilder) {
        this.supabaseUrl = supabaseUrl;
        this.anonKey = anonKey;
        this.webClient = webClientBuilder
                .baseUrl(supabaseUrl)
                .defaultHeader("apiKey", anonKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
                .build();
    }

    /**
     * Registra un usuario (proxy a Supabase)
     * POST {supabaseUrl}/auth/v1/signup
     * body: { "email": "...", "password": "..." }
     */
    public Mono<Map> signUp(String email, String password) {
        return webClient.post()
                .uri("/auth/v1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .bodyToMono(Map.class);
    }

    /**
     * Login (grant_type=password) - devuelve access_token y refresh_token
     * POST /auth/v1/token?grant_type=password
     */
    public Mono<Map> signIn(String email, String password) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/auth/v1/token")
                        .queryParam("grant_type", "password")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .bodyToMono(Map.class);
    }

    /**
     * Obtener info del usuario con token:
     * GET /auth/v1/user (Authorization: Bearer <access_token>)
     */
    public Mono<Map> getUser(String accessToken) {
        return webClient.get()
                .uri("/auth/v1/user")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Map.class);
    }
}