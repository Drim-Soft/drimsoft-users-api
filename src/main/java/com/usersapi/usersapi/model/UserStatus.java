package com.usersapi.usersapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "userstatus")
public class UserStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduserstatus")
    private Integer IDUserStatus;

    @Column(name = "name")
    private String name;

    // Getters and Setters
    public Integer getIdUserStatus() { return IDUserStatus; }
    public void setIdUserStatus(Integer IDUserStatus) { this.IDUserStatus = IDUserStatus; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}