package com.usersapi.usersapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.usersapi.usersapi.model.UserDrimsoft;
import com.usersapi.usersapi.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDrimsoft> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDrimsoft> getUserById(@PathVariable Integer id) {
        return userService.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public UserDrimsoft createUser(@RequestBody UserDrimsoft user) {
        return userService.save(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDrimsoft> updateUser(@PathVariable Integer id, @RequestBody UserDrimsoft userDetails) {
        return userService.findById(id).map(user -> {
            // Solo actualizar si se proporciona
            if (userDetails.getName() != null) {
                user.setName(userDetails.getName());
            }

            // Solo actualizar rol si se proporciona
            if (userDetails.getRole() != null) {
                user.setRole(userDetails.getRole());
            }

            // Solo actualizar estado si se proporciona
            if (userDetails.getStatus() != null) {
                user.setStatus(userDetails.getStatus());
            }

            return ResponseEntity.ok(userService.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Eliminación logica FALTA DEFINIR LOS STATUS
    @DeleteMapping("/{id}")
    public ResponseEntity<UserDrimsoft> deleteUser(@PathVariable Integer id) {
        return userService.findById(id).map(user -> {
            UserDrimsoft updatedUser = userService.updateStatus(id, 3);
            return ResponseEntity.ok(updatedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/roles/{roleId}")
    public ResponseEntity<UserDrimsoft> assignRole(@PathVariable Integer id, @PathVariable Integer roleId) {
        return ResponseEntity.ok(userService.updateRole(id, roleId));
    }

    @PutMapping("/{id}/status/{statusId}")
    public ResponseEntity<UserDrimsoft> updateUserStatus(@PathVariable Integer id, @PathVariable Integer statusId) {
        return ResponseEntity.ok(userService.updateStatus(id, statusId));
    }
}