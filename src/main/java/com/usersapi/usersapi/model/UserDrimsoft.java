package com.usersapi.usersapi.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "userdrimsoft")
public class UserDrimsoft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Integer idUser;

    // Este campo enlaza con el usuario en Supabase Auth
    @Column(name = "supabaseuserid", columnDefinition = "uuid")
    private UUID supabaseUserId;

    @ManyToOne
    @JoinColumn(name = "iduserstatus", referencedColumnName = "iduserstatus")
    private UserStatus status;

    @ManyToOne
    @JoinColumn(name = "idrole", referencedColumnName = "idrole")
    private Role role;

    @Column(name = "name")
    private String name;

    // ======= Getters y Setters =======
    public Integer getIdUser() {
        return idUser;
    }
    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public UUID getSupabaseUserId() {
        return supabaseUserId;
    }
    public void setSupabaseUserId(UUID supabaseUserId) {
        this.supabaseUserId = supabaseUserId;
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
}
