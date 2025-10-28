package com.usersapi.usersapi.dto;

public class TicketAnswerRequest {
    private String answer;
    private Integer iddrimsoftuser; 

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getIddrimsoftuser() {
        return iddrimsoftuser;
    }

    public void setIddrimsoftuser(Integer iddrimsoftuser) {
        this.iddrimsoftuser = iddrimsoftuser;
    }
}
