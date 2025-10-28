package com.usersapi.usersapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ticketsupport")
public class TicketSupport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtickets")
    private Integer idTickets;

    @Column(name = "idplanifikauser")
    private Integer idPlanifikaUser;

    @ManyToOne
    @JoinColumn(name = "idticketstatus", referencedColumnName = "idticketstatus")
    private TicketStatus status;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "answer")
    private String answer;

    @ManyToOne
    @JoinColumn(name = "iddrimsoftuser", referencedColumnName = "iduser")
    private UserDrimsoft drimsoftUser;

    public Integer getIdTickets() {
        return idTickets;
    }

    public void setIdTickets(Integer idTickets) {
        this.idTickets = idTickets;
    }

    public Integer getIdPlanifikaUser() {
        return idPlanifikaUser;
    }

    public void setIdPlanifikaUser(Integer idPlanifikaUser) {
        this.idPlanifikaUser = idPlanifikaUser;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public UserDrimsoft getDrimsoftUser() {
        return drimsoftUser;
    }

    public void setDrimsoftUser(UserDrimsoft drimsoftUser) {
        this.drimsoftUser = drimsoftUser;
    }
}
