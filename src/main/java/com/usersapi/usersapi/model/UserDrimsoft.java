package com.usersapi.usersapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "userdrimsoft")
public class UserDrimsoft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Integer idUser;

    @ManyToOne
    @JoinColumn(name = "iduserstatus", referencedColumnName = "iduserstatus")
    private UserStatus status;


    @ManyToOne
    @JoinColumn(name = "idrole")
    private Role role;


    @Column(name = "name")
    private String name;

    @Column(name = "supabaseuserid")
    private java.util.UUID supabaseUserID;

    // Getters y Setters
    public Integer getIdUser() {
        return idUser;
    }
    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public UserStatus getStatus() {
        return status;
    }
    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public java.util.UUID getSupabaseUserID() {
        return supabaseUserID;
    }

    public void setSupabaseUserID(java.util.UUID supabaseUserID) {
        this.supabaseUserID = supabaseUserID;
    }
}