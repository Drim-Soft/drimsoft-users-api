package com.usersapi.usersapi.service;

import com.usersapi.usersapi.model.UserDrimsoft;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String anonKey;
    private final String serviceKey;
    private final UserService userService;

    public AuthService(@Value("${supabase.url}") String supabaseUrl,
                       @Value("${supabase.anon.key}") String anonKey,
                       @Value("${supabase.service.key}") String serviceKey,
                       WebClient.Builder webClientBuilder,
                       UserService userService) {
        this.supabaseUrl = supabaseUrl;
        this.anonKey = anonKey;
        this.serviceKey = serviceKey;
        this.userService = userService;
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
                .bodyToMono(Map.class)
                .map(this::addRoleInformation);
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
                .bodyToMono(Map.class)
                .map(this::addRoleInformation);
    }

    /**
     * Adds role information to the Supabase response based on the user's UID
     */
    private Map addRoleInformation(Map supabaseResponse) {
        Map<String, Object> response = new HashMap<>(supabaseResponse);
        
        try {
            // Extract the user information from the Supabase response
            Object userObj = supabaseResponse.get("user");
            if (userObj instanceof Map) {
                Map userMap = (Map) userObj;
                String uid = (String) userMap.get("id");
                
                if (uid != null) {
                    // Convert String UID to UUID
                    UUID supabaseUuid = UUID.fromString(uid);
                    
                    // Find the user in our database by Supabase UID
                    Optional<UserDrimsoft> userDrimsoft = userService.findBySupabaseUserId(supabaseUuid);
                    
                    if (userDrimsoft.isPresent()) {
                        UserDrimsoft user = userDrimsoft.get();
                        
                        // Add role information if available
                        if (user.getRole() != null) {
                            Map<String, Object> roleInfo = new HashMap<>();
                            roleInfo.put("id", user.getRole().getIdRole());
                            roleInfo.put("name", user.getRole().getName());
                            response.put("role", roleInfo);
                        }
                        
                        // Add user name if available
                        if (user.getName() != null) {
                            response.put("userName", user.getName());
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // Handle invalid UUID format
            System.err.println("Invalid UUID format: " + e.getMessage());
        } catch (Exception e) {
            // Handle other potential errors
            System.err.println("Error adding role information: " + e.getMessage());
        }
        
        return response;
    }

    public Mono<Map<String, Object>> getUserSupabase(String accessToken) {
        return webClient.get()
                .uri("/auth/v1/user")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> updateProfile(String accessToken, String name, String password) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasPassword = password != null && !password.isBlank();
        if (!hasName && !hasPassword) {
            return Mono.error(new IllegalArgumentException("Debe proporcionar 'name' o 'password' para actualizar"));
        }

        return getUserSupabase(accessToken)
                .flatMap((Map<String, Object> supabaseUser) -> {
                    String supabaseUserIdStr = (String) supabaseUser.get("id");
                    if (supabaseUserIdStr == null) {
                        return Mono.error(new RuntimeException("No se encontró el ID del usuario en Supabase"));
                    }

                    UUID supabaseUserId;
                    try {
                        supabaseUserId = UUID.fromString(supabaseUserIdStr);
                    } catch (IllegalArgumentException e) {
                        return Mono.error(new RuntimeException("ID de usuario Supabase inválido: " + supabaseUserIdStr));
                    }

                    Mono<Map<String, Object>> authUpdateMono;
                    if (hasName || hasPassword) {
                        Map<String, Object> payload = new HashMap<>();
                        if (hasPassword) {
                            payload.put("password", password);
                        }

                        Map<String, Object> data = new HashMap<>();
                        if (hasName) {
                            data.put("name", name);
                            data.put("full_name", name);
                        }

                        authUpdateMono = webClient.put()
                                .uri("/auth/v1/user")
                                .headers(h -> h.setBearerAuth(accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(payload)
                                .retrieve()
                                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                                        clientResponse.bodyToMono(String.class)
                                                .defaultIfEmpty("[No response body]")
                                                .flatMap(errorBody -> Mono.error(new RuntimeException("Supabase Auth Update Error (" + clientResponse.statusCode() + "): " + errorBody)))
                                )
                                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
                    } else {
                        authUpdateMono = Mono.just(Map.of("skipped", true));
                    }

                    Mono<Map<String, Object>> dbUpdateMono;
                    if (hasName) {
                        dbUpdateMono = Mono.fromCallable(() -> {
                                    return userService.findBySupabaseUserId(supabaseUserId)
                                            .map(dto -> {
                                                if (hasName) dto.setName(name);
                                                UserDrimsoft saved = userService.save(dto);
                                                Map<String, Object> result = new HashMap<>();
                                                result.put("iduser", saved.getIdUser());
                                                result.put("name", saved.getName());
                                                return result;
                                            })
                                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos de la aplicación"));
                                })
                                .onErrorResume(e -> Mono.error(new RuntimeException("Error al actualizar datos en la base de datos: " + e.getMessage())));
                    } else {
                        dbUpdateMono = Mono.just(Map.of("skipped", true));
                    }

                    return Mono.zip(authUpdateMono, dbUpdateMono)
                            .map(tuple -> Map.<String, Object>of(
                                    "auth", tuple.getT1(),
                                    "db", tuple.getT2(),
                                    "supabaseUserId", supabaseUserId
                            ));
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Error desconocido al actualizar el perfil";
                    return Mono.error(new RuntimeException(errorMessage));
                });
    }
}