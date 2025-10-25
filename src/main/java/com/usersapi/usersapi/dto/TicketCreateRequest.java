package com.usersapi.usersapi.dto;

public class TicketCreateRequest {
    private Integer idplanifikauser;
    private String title;
    private String description;
    private Integer iddrimsoftuser; // optional
    private Integer idticketstatus; // optional

    public Integer getIdplanifikauser() {
        return idplanifikauser;
    }

    public void setIdplanifikauser(Integer idplanifikauser) {
        this.idplanifikauser = idplanifikauser;
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

    public Integer getIddrimsoftuser() {
        return iddrimsoftuser;
    }

    public void setIddrimsoftuser(Integer iddrimsoftuser) {
        this.iddrimsoftuser = iddrimsoftuser;
    }

    public Integer getIdticketstatus() {
        return idticketstatus;
    }

    public void setIdticketstatus(Integer idticketstatus) {
        this.idticketstatus = idticketstatus;
    }
}
