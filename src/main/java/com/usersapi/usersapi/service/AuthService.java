package com.usersapi.usersapi.service;

import com.usersapi.usersapi.model.Role;
import com.usersapi.usersapi.model.UserDrimsoft;
import com.usersapi.usersapi.model.UserStatus;
import com.usersapi.usersapi.repository.RoleRepository;
import com.usersapi.usersapi.repository.UserRepository;
import com.usersapi.usersapi.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String anonKey;

    private final UserRepository userDrimsoftRepository;
    private final UserStatusRepository userStatusRepository;
    private final RoleRepository roleRepository;

    public AuthService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.anon.key}") String anonKey,
            WebClient.Builder webClientBuilder,
            UserRepository userDrimsoftRepository,
            UserStatusRepository userStatusRepository,
            RoleRepository roleRepository) {

        this.supabaseUrl = supabaseUrl;
        this.anonKey = anonKey;
        this.userDrimsoftRepository = userDrimsoftRepository;
        this.userStatusRepository = userStatusRepository;
        this.roleRepository = roleRepository;

        this.webClient = webClientBuilder
                .baseUrl(supabaseUrl)
                .defaultHeader("apiKey", anonKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
                .build();
    }

    /**
     * Registra usuario en Supabase Auth y en la DB local
     */
    public Mono<Map> signUp(String email, String password) {
        return webClient.post()
                .uri("/auth/v1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    try {
                        // 1️⃣ Obtener el user.id del JSON
                        Map userData = (Map) response.get("user");
                        String supabaseUserIdStr = (String) userData.get("id");
                        UUID supabaseUserId = UUID.fromString(supabaseUserIdStr);

                        // 2️⃣ Crear registro en tu tabla UserDrimsoft
                        UserDrimsoft newUser = new UserDrimsoft();
                        newUser.setSupabaseUserId(supabaseUserId);
                        newUser.setName(email.split("@")[0]); // ejemplo: usar parte del correo como nombre

                        // Buscar Role y Status por defecto (ejemplo: 1)
                        UserStatus defaultStatus = userStatusRepository.findById(1)
                                .orElseThrow(() -> new RuntimeException("Status ID 1 no encontrado"));
                        Role defaultRole = roleRepository.findById(1)
                                .orElseThrow(() -> new RuntimeException("Role ID 1 no encontrado"));

                        newUser.setStatus(defaultStatus);
                        newUser.setRole(defaultRole);

                        userDrimsoftRepository.save(newUser);
                        return Mono.just(response);

                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Error creando usuario local: " + e.getMessage()));
                    }
                });
    }

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

    public Mono<Map> getUser(String accessToken) {
        return webClient.get()
                .uri("/auth/v1/user")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Map.class);
    }
}