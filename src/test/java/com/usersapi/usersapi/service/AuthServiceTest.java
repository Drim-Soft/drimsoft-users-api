package com.usersapi.usersapi.service;

import com.usersapi.usersapi.model.Role;
import com.usersapi.usersapi.model.UserDrimsoft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de AuthService")
class AuthServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RequestBodySpec requestBodySpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RequestHeadersSpec requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    @Mock
    private UserService userService;

    private AuthService authService;
    private String supabaseUrl = "https://test.supabase.co";
    private String anonKey = "test-anon-key";
    private UserDrimsoft testUser;
    private Role testRole;
    private UUID testSupabaseUserId;

    @BeforeEach
    void setUp() {
        testSupabaseUserId = UUID.randomUUID();
        
        testRole = new Role();
        testRole.setIdRole(1);
        testRole.setName("Admin");

        testUser = new UserDrimsoft();
        testUser.setIdUser(1);
        testUser.setName("Test User");
        testUser.setRole(testRole);
        testUser.setSupabaseUserId(testSupabaseUserId);

        // Configurar mocks de WebClient
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        // Stubbing para GET (solo usado en getUser) - lenient porque no todos los tests lo usan
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        // Stubbing para uri() con String (usado en signUp)
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        // Stubbing para uri() con Function (usado en signIn) - usando lenient para evitar ambigüedad
        lenient().doReturn(requestBodySpec).when(requestBodyUriSpec).uri(any(java.util.function.Function.class));
        // Stubbing para GET uri() (solo usado en getUser) - lenient porque no todos los tests lo usan
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        // Stubbing para headers() (solo usado en getUser) - lenient porque no todos los tests lo usan
        lenient().when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(new HashMap<>()));

        // Crear instancia de AuthService
        authService = new AuthService(supabaseUrl, anonKey, webClientBuilder, userService);
    }

    @Test
    @DisplayName("Debería registrar un usuario correctamente (signUp)")
    void testSignUp() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        Map<String, Object> response = new HashMap<>();
        response.put("user", new HashMap<>());
        response.put("access_token", "token123");

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> monoResponse = Mono.just(response);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) monoResponse);

        // Act
        Mono<Map> result = authService.signUp(email, password);

        // Assert
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertNotNull(map);
                    assertTrue(map.containsKey("user"));
                })
                .verifyComplete();

        verify(webClient, times(1)).post();
        verify(requestBodyUriSpec, times(1)).uri("/auth/v1/signup");
        verify(requestBodySpec, times(1)).contentType(any());
        verify(requestBodySpec, times(1)).bodyValue(any());
        verify(requestHeadersSpec, times(1)).retrieve();
    }

    @Test
    @DisplayName("Debería hacer login correctamente (signIn)")
    void testSignIn() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        Map<String, Object> supabaseResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", testSupabaseUserId.toString());
        supabaseResponse.put("user", userMap);
        supabaseResponse.put("access_token", "token123");

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> monoResponse = Mono.just(supabaseResponse);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) monoResponse);
        when(userService.findBySupabaseUserId(testSupabaseUserId)).thenReturn(Optional.of(testUser));

        // Act
        Mono<Map> result = authService.signIn(email, password);

        // Assert
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertNotNull(map);
                    assertTrue(map.containsKey("user"));
                    assertTrue(map.containsKey("role"));
                    assertTrue(map.containsKey("userName"));
                })
                .verifyComplete();

        verify(webClient, times(1)).post();
        verify(userService, times(1)).findBySupabaseUserId(testSupabaseUserId);
    }

    @Test
    @DisplayName("Debería hacer login sin información de rol cuando el usuario no existe en BD")
    void testSignInUserNotFoundInDatabase() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        Map<String, Object> supabaseResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", testSupabaseUserId.toString());
        supabaseResponse.put("user", userMap);
        supabaseResponse.put("access_token", "token123");

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> monoResponse = Mono.just(supabaseResponse);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) monoResponse);
        when(userService.findBySupabaseUserId(testSupabaseUserId)).thenReturn(Optional.empty());

        // Act
        Mono<Map> result = authService.signIn(email, password);

        // Assert
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertNotNull(map);
                    assertTrue(map.containsKey("user"));
                    assertFalse(map.containsKey("role"));
                    assertFalse(map.containsKey("userName"));
                })
                .verifyComplete();

        verify(userService, times(1)).findBySupabaseUserId(testSupabaseUserId);
    }

    @Test
    @DisplayName("Debería obtener información del usuario con token (getUser)")
    void testGetUser() {
        // Arrange
        String accessToken = "valid-token";
        Map<String, Object> supabaseResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", testSupabaseUserId.toString());
        supabaseResponse.put("user", userMap);

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> monoResponse = Mono.just(supabaseResponse);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) monoResponse);
        when(userService.findBySupabaseUserId(testSupabaseUserId)).thenReturn(Optional.of(testUser));

        // Act
        Mono<Map> result = authService.getUser(accessToken);

        // Assert
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertNotNull(map);
                    assertTrue(map.containsKey("user"));
                    assertTrue(map.containsKey("role"));
                    assertTrue(map.containsKey("userName"));
                })
                .verifyComplete();

        verify(webClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/auth/v1/user");
        verify(userService, times(1)).findBySupabaseUserId(testSupabaseUserId);
    }

    @Test
    @DisplayName("Debería manejar UUID inválido en addRoleInformation")
    void testAddRoleInformationInvalidUUID() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        Map<String, Object> supabaseResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", "invalid-uuid-format");
        supabaseResponse.put("user", userMap);

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> monoResponse = Mono.just(supabaseResponse);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) monoResponse);

        // Act
        Mono<Map> result = authService.signIn(email, password);

        // Assert
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertNotNull(map);
                    assertTrue(map.containsKey("user"));
                    // No debería tener role ni userName debido al UUID inválido
                })
                .verifyComplete();

        verify(userService, never()).findBySupabaseUserId(any());
    }

    @Test
    @DisplayName("Debería manejar respuesta sin usuario en addRoleInformation")
    void testAddRoleInformationNoUser() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        Map<String, Object> supabaseResponse = new HashMap<>();
        supabaseResponse.put("access_token", "token123");
        // No incluye "user"

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> monoResponse = Mono.just(supabaseResponse);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) monoResponse);

        // Act
        Mono<Map> result = authService.signIn(email, password);

        // Assert
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertNotNull(map);
                    assertFalse(map.containsKey("user"));
                })
                .verifyComplete();

        verify(userService, never()).findBySupabaseUserId(any());
    }

    @Test
    @DisplayName("Debería manejar usuario sin rol en addRoleInformation")
    void testAddRoleInformationUserWithoutRole() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        Map<String, Object> supabaseResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", testSupabaseUserId.toString());
        supabaseResponse.put("user", userMap);

        UserDrimsoft userWithoutRole = new UserDrimsoft();
        userWithoutRole.setIdUser(1);
        userWithoutRole.setName("User Without Role");
        userWithoutRole.setSupabaseUserId(testSupabaseUserId);
        userWithoutRole.setRole(null);

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> monoResponse = Mono.just(supabaseResponse);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) monoResponse);
        when(userService.findBySupabaseUserId(testSupabaseUserId)).thenReturn(Optional.of(userWithoutRole));

        // Act
        Mono<Map> result = authService.signIn(email, password);

        // Assert
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertNotNull(map);
                    assertTrue(map.containsKey("user"));
                    assertFalse(map.containsKey("role"));
                    assertTrue(map.containsKey("userName"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar errores de WebClient en signUp")
    void testSignUpError() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        RuntimeException error = new RuntimeException("Network error");

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> errorMono = Mono.error(error);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) errorMono);

        // Act
        Mono<Map> result = authService.signUp(email, password);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar errores de WebClient en signIn")
    void testSignInError() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        RuntimeException error = new RuntimeException("Network error");

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> errorMono = Mono.error(error);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) errorMono);

        // Act
        Mono<Map> result = authService.signIn(email, password);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar errores de WebClient en getUser")
    void testGetUserError() {
        // Arrange
        String accessToken = "valid-token";
        RuntimeException error = new RuntimeException("Unauthorized");

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> errorMono = Mono.error(error);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) errorMono);

        // Act
        Mono<Map> result = authService.getUser(accessToken);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería agregar información de rol correctamente cuando el usuario tiene rol")
    void testAddRoleInformationWithRole() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        Map<String, Object> supabaseResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", testSupabaseUserId.toString());
        supabaseResponse.put("user", userMap);
        supabaseResponse.put("access_token", "token123");

        @SuppressWarnings("unchecked")
        Mono<Map<String, Object>> monoResponse = Mono.just(supabaseResponse);
        when(responseSpec.bodyToMono(Map.class)).thenReturn((Mono) monoResponse);
        when(userService.findBySupabaseUserId(testSupabaseUserId)).thenReturn(Optional.of(testUser));

        // Act
        Mono<Map> result = authService.signIn(email, password);

        // Assert
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertNotNull(map);
                    assertTrue(map.containsKey("role"));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> roleInfo = (Map<String, Object>) map.get("role");
                    assertEquals(testRole.getIdRole(), roleInfo.get("id"));
                    assertEquals(testRole.getName(), roleInfo.get("name"));
                    assertEquals(testUser.getName(), map.get("userName"));
                })
                .verifyComplete();
    }
}

