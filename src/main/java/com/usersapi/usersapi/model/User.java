package com.usersapi.usersapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "\"User\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IDUser\"")
    private Integer IDUser;

    @ManyToOne
    @JoinColumn(name = "\"IDUserStatus\"")
    private UserStatus status;

    @ManyToOne
    @JoinColumn(name = "\"IDRole\"")
    private Role role;

    @OneToOne
    @JoinColumn(name = "\"IDAuthentication\"")
    private Authentication authentication;

    @Column(name = "name")
    private String name;

    // Getters and Setters
    public Integer getIdUser() { return IDUser; }
    public void setIdUser(Integer IDUser) { this.IDUser = IDUser; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Authentication getAuthentication() { return authentication; }
    public void setAuthentication(Authentication authentication) { this.authentication = authentication; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
