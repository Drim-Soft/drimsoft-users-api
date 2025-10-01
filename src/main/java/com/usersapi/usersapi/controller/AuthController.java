package com.usersapi.usersapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.usersapi.usersapi.dto.LoginRequest;
import com.usersapi.usersapi.dto.LoginResponse;
import com.usersapi.usersapi.model.UserDrimsoft;
import com.usersapi.usersapi.repository.AuthenticationRepository;
import com.usersapi.usersapi.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationRepository authenticationRepository;
    private final UserRepository userRepository;

    public AuthController(AuthenticationRepository authenticationRepository, UserRepository userRepository) {
        this.authenticationRepository = authenticationRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return authenticationRepository
                .findByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword())
                .map(auth -> {
                    UserDrimsoft user = userRepository.findById(auth.getIdAuthentication()).orElseThrow();
                    return ResponseEntity.ok(
                            new LoginResponse(user.getIdUser(), user.getName(), user.getRole().getName())
                    );
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}

