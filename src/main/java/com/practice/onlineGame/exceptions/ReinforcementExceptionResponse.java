package com.practice.onlineGame.exceptions;

public class ReinforcementExceptionResponse {
    private String reinforcement;

    public String getReinforcement() {
        return reinforcement;
    }

    public void setReinforcement(String reinforcement) {
        this.reinforcement = reinforcement;
    }

    public ReinforcementExceptionResponse(String reinforcement) {
        this.reinforcement = reinforcement;
    }
}
