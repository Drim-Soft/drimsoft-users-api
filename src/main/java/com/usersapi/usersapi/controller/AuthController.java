package com.usersapi.usersapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usersapi.usersapi.dto.AuthRequest;
import com.usersapi.usersapi.dto.LoginRequest;
import com.usersapi.usersapi.dto.LoginResponse;
import com.usersapi.usersapi.model.User;
import com.usersapi.usersapi.repository.AuthenticationRepository;
import com.usersapi.usersapi.repository.UserRepository;
import com.usersapi.usersapi.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationRepository authenticationRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    

    public AuthController(AuthenticationRepository authenticationRepository, UserRepository userRepository,
            AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationRepository = authenticationRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(token);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return authenticationRepository
                .findByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword())
                .map(auth -> {
                    User user = userRepository.findById(auth.getIdAuthentication()).orElseThrow();
                    return ResponseEntity.ok(
                            new LoginResponse(user.getIdUser(), user.getName(), user.getRole().getName())
                    );
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}

