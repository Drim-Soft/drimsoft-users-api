package com.usersapi.usersapi.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.usersapi.usersapi.model.User;
import com.usersapi.usersapi.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping
    public List<User> getAllUsers() { return userService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return userService.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createUser(@RequestBody User user) { return userService.save(user); }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        return userService.findById(id).map(user -> {
            user.setName(userDetails.getName());
            return ResponseEntity.ok(userService.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Eliminación logica FALTA DEFINIR LOS STATUS
    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable Integer id) {
        return userService.findById(id).map(user -> {
            User updatedUser = userService.updateStatus(id, 3); 
            return ResponseEntity.ok(updatedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/roles/{roleId}")
    public ResponseEntity<User> assignRole(@PathVariable Integer id, @PathVariable Integer roleId) {
        return ResponseEntity.ok(userService.updateRole(id, roleId));
    }

    @PutMapping("/{id}/status/{statusId}")
    public ResponseEntity<User> updateUserStatus(@PathVariable Integer id, @PathVariable Integer statusId) {
        return ResponseEntity.ok(userService.updateStatus(id, statusId));
    }
}
