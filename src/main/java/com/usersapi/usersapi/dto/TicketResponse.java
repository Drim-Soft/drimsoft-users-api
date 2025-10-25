package com.usersapi.usersapi.dto;

public class TicketResponse {
    private Integer idtickets;
    private Integer idplanifikauser;
    private Integer idticketstatus;
    private String ticketstatusname;
    private String title;
    private String description;
    private String answer;
    private Integer iddrimsoftuser;
    private String drimsoftusername;

    public Integer getIdtickets() { return idtickets; }
    public void setIdtickets(Integer idtickets) { this.idtickets = idtickets; }

    public Integer getIdplanifikauser() { return idplanifikauser; }
    public void setIdplanifikauser(Integer idplanifikauser) { this.idplanifikauser = idplanifikauser; }

    public Integer getIdticketstatus() { return idticketstatus; }
    public void setIdticketstatus(Integer idticketstatus) { this.idticketstatus = idticketstatus; }

    public String getTicketstatusname() { return ticketstatusname; }
    public void setTicketstatusname(String ticketstatusname) { this.ticketstatusname = ticketstatusname; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Integer getIddrimsoftuser() { return iddrimsoftuser; }
    public void setIddrimsoftuser(Integer iddrimsoftuser) { this.iddrimsoftuser = iddrimsoftuser; }

    public String getDrimsoftusername() { return drimsoftusername; }
    public void setDrimsoftusername(String drimsoftusername) { this.drimsoftusername = drimsoftusername; }
}
