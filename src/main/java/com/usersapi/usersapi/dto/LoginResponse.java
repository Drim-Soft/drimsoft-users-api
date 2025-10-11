package com.usersapi.usersapi.dto;

public class LoginResponse {
    private Integer userId;
    private String name;
    private String role;

    public LoginResponse(Integer userId, String name, String role) {
        this.userId = userId;
        this.name = name;
        this.role = role;
    }

    // Getters
    public Integer getUserId() { return userId; }
    public String getName() { return name; }
    public String getRole() { return role; }
}
