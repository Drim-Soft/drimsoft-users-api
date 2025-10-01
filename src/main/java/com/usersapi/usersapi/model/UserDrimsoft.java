package com.usersapi.usersapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "userdrimsoft")  // en minúscula y sin comillas
public class UserDrimsoft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")   // también en minúscula
    private Integer idUser;

    @ManyToOne
    @JoinColumn(name = "iduserstatus", referencedColumnName = "iduserstatus")
    private UserStatus status;


    @ManyToOne
    @JoinColumn(name = "idrole")
    private Role role;

    @OneToOne
    @JoinColumn(name = "idauthentication")
    private Authentication authentication;

    @Column(name = "name")
    private String name;


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

    public Authentication getAuthentication() { 
        return authentication; 
    }
    public void setAuthentication(Authentication authentication) { 
        this.authentication = authentication; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }
}
