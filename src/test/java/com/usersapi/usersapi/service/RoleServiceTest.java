package com.usersapi.usersapi.service;

import com.usersapi.usersapi.model.Role;
import com.usersapi.usersapi.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de RoleService")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role testRole1;
    private Role testRole2;

    @BeforeEach
    void setUp() {
        testRole1 = new Role();
        testRole1.setIdRole(1);
        testRole1.setName("Admin");

        testRole2 = new Role();
        testRole2.setIdRole(2);
        testRole2.setName("User");
    }

    @Test
    @DisplayName("Debería retornar todos los roles")
    void testFindAll() {
        // Arrange
        List<Role> roles = Arrays.asList(testRole1, testRole2);
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        List<Role> result = roleService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Admin", result.get(0).getName());
        assertEquals("User", result.get(1).getName());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay roles")
    void testFindAllEmpty() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Role> result = roleService.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería encontrar un rol por ID")
    void testFindById() {
        // Arrange
        when(roleRepository.findById(1)).thenReturn(Optional.of(testRole1));

        // Act
        Optional<Role> result = roleService.findById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRole1.getIdRole(), result.get().getIdRole());
        assertEquals(testRole1.getName(), result.get().getName());
        verify(roleRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Debería retornar Optional vacío cuando el rol no existe")
    void testFindByIdNotFound() {
        // Arrange
        when(roleRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<Role> result = roleService.findById(999);

        // Assert
        assertFalse(result.isPresent());
        verify(roleRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Debería encontrar múltiples roles correctamente")
    void testFindAllMultipleRoles() {
        // Arrange
        Role role3 = new Role();
        role3.setIdRole(3);
        role3.setName("Guest");
        
        List<Role> roles = Arrays.asList(testRole1, testRole2, role3);
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        List<Role> result = roleService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Admin", result.get(0).getName());
        assertEquals("User", result.get(1).getName());
        assertEquals("Guest", result.get(2).getName());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería manejar correctamente roles con nombres nulos")
    void testFindAllWithNullNames() {
        // Arrange
        Role roleWithNullName = new Role();
        roleWithNullName.setIdRole(3);
        roleWithNullName.setName(null);
        
        List<Role> roles = Arrays.asList(testRole1, roleWithNullName);
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        List<Role> result = roleService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0).getName());
        assertNull(result.get(1).getName());
        verify(roleRepository, times(1)).findAll();
    }
}

