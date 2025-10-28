package com.usersapi.usersapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ticketstatus")
public class TicketStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idticketstatus")
    private Integer idTicketStatus;

    @Column(name = "name")
    private String name;

    public Integer getIdTicketStatus() {
        return idTicketStatus;
    }

    public void setIdTicketStatus(Integer idTicketStatus) {
        this.idTicketStatus = idTicketStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
