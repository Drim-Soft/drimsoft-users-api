package com.usersapi.usersapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "authentication")
public class Authentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idauthentication")
    private Integer IDAuthentication;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    // Getters and Setters
    public Integer getIdAuthentication() { return IDAuthentication; }
    public void setIdAuthentication(Integer IDAuthentication) { this.IDAuthentication = IDAuthentication; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

