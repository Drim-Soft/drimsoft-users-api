package com.usersapi.usersapi.service;

import com.usersapi.usersapi.model.UserDrimsoft;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserService userService;

    public AuthService(@Value("${supabase.url.authentication}") String supabaseUrl,
                       @Value("${supabase.anon.key}") String anonKey,
                       WebClient.Builder webClientBuilder,
                       UserService userService) {
        this.supabaseUrl = supabaseUrl;
        this.anonKey = anonKey;
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
}