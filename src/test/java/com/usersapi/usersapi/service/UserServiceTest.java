package com.usersapi.usersapi.service;

import com.usersapi.usersapi.model.Role;
import com.usersapi.usersapi.model.UserDrimsoft;
import com.usersapi.usersapi.model.UserStatus;
import com.usersapi.usersapi.repository.RoleRepository;
import com.usersapi.usersapi.repository.UserRepository;
import com.usersapi.usersapi.repository.UserStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserStatusRepository userStatusRepository;

    @InjectMocks
    private UserService userService;

    private UserDrimsoft testUser;
    private Role testRole;
    private UserStatus testStatus;
    private UUID testSupabaseUserId;

    @BeforeEach
    void setUp() {
        testSupabaseUserId = UUID.randomUUID();
        
        testRole = new Role();
        testRole.setIdRole(1);
        testRole.setName("Admin");

        testStatus = new UserStatus();
        testStatus.setIdUserStatus(1);
        testStatus.setName("Active");

        testUser = new UserDrimsoft();
        testUser.setIdUser(1);
        testUser.setName("Test User");
        testUser.setRole(testRole);
        testUser.setStatus(testStatus);
        testUser.setSupabaseUserId(testSupabaseUserId);
    }

    @Test
    @DisplayName("Debería retornar todos los usuarios")
    void testFindAll() {
        // Arrange
        List<UserDrimsoft> users = Arrays.asList(testUser, new UserDrimsoft());
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDrimsoft> result = userService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay usuarios")
    void testFindAllEmpty() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<UserDrimsoft> result = userService.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería encontrar un usuario por ID")
    void testFindById() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserDrimsoft> result = userService.findById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getIdUser(), result.get().getIdUser());
        assertEquals(testUser.getName(), result.get().getName());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Debería retornar Optional vacío cuando el usuario no existe")
    void testFindByIdNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<UserDrimsoft> result = userService.findById(999);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Debería guardar un usuario correctamente")
    void testSave() {
        // Arrange
        UserDrimsoft newUser = new UserDrimsoft();
        newUser.setName("New User");
        when(userRepository.save(any(UserDrimsoft.class))).thenReturn(testUser);

        // Act
        UserDrimsoft result = userService.save(newUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getIdUser(), result.getIdUser());
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    @DisplayName("Debería eliminar un usuario por ID")
    void testDelete() {
        // Arrange
        doNothing().when(userRepository).deleteById(1);

        // Act
        userService.delete(1);

        // Assert
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Debería actualizar el rol de un usuario")
    void testUpdateRole() {
        // Arrange
        Role newRole = new Role();
        newRole.setIdRole(2);
        newRole.setName("User");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(2)).thenReturn(Optional.of(newRole));
        when(userRepository.save(any(UserDrimsoft.class))).thenAnswer(invocation -> {
            UserDrimsoft user = invocation.getArgument(0);
            user.setRole(newRole);
            return user;
        });

        // Act
        UserDrimsoft result = userService.updateRole(1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(newRole.getIdRole(), result.getRole().getIdRole());
        assertEquals(newRole.getName(), result.getRole().getName());
        verify(userRepository, times(1)).findById(1);
        verify(roleRepository, times(1)).findById(2);
        verify(userRepository, times(1)).save(any(UserDrimsoft.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el usuario no existe al actualizar rol")
    void testUpdateRoleUserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.updateRole(999, 1));
        verify(userRepository, times(1)).findById(999);
        verify(roleRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el rol no existe al actualizar rol")
    void testUpdateRoleRoleNotFound() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.updateRole(1, 999));
        verify(userRepository, times(1)).findById(1);
        verify(roleRepository, times(1)).findById(999);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería actualizar el estado de un usuario")
    void testUpdateStatus() {
        // Arrange
        UserStatus newStatus = new UserStatus();
        newStatus.setIdUserStatus(2);
        newStatus.setName("Inactive");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userStatusRepository.findById(2)).thenReturn(Optional.of(newStatus));
        when(userRepository.save(any(UserDrimsoft.class))).thenAnswer(invocation -> {
            UserDrimsoft user = invocation.getArgument(0);
            user.setStatus(newStatus);
            return user;
        });

        // Act
        UserDrimsoft result = userService.updateStatus(1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus.getIdUserStatus(), result.getStatus().getIdUserStatus());
        assertEquals(newStatus.getName(), result.getStatus().getName());
        verify(userRepository, times(1)).findById(1);
        verify(userStatusRepository, times(1)).findById(2);
        verify(userRepository, times(1)).save(any(UserDrimsoft.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el usuario no existe al actualizar estado")
    void testUpdateStatusUserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.updateStatus(999, 1));
        verify(userRepository, times(1)).findById(999);
        verify(userStatusRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el estado no existe al actualizar estado")
    void testUpdateStatusStatusNotFound() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userStatusRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.updateStatus(1, 999));
        verify(userRepository, times(1)).findById(1);
        verify(userStatusRepository, times(1)).findById(999);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería encontrar un usuario por Supabase User ID")
    void testFindBySupabaseUserId() {
        // Arrange
        when(userRepository.findBySupabaseUserId(testSupabaseUserId)).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserDrimsoft> result = userService.findBySupabaseUserId(testSupabaseUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getSupabaseUserId(), result.get().getSupabaseUserId());
        verify(userRepository, times(1)).findBySupabaseUserId(testSupabaseUserId);
    }

    @Test
    @DisplayName("Debería retornar Optional vacío cuando no se encuentra usuario por Supabase User ID")
    void testFindBySupabaseUserIdNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findBySupabaseUserId(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Optional<UserDrimsoft> result = userService.findBySupabaseUserId(nonExistentId);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findBySupabaseUserId(nonExistentId);
    }
}

