package com.usersapi.usersapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrole")
    private Integer IDRole;

    @Column(name = "name")
    private String name;

    // Getters and Setters
    public Integer getIdRole() { return IDRole; }
    public void setIdRole(Integer IDRole) { this.IDRole = IDRole; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
