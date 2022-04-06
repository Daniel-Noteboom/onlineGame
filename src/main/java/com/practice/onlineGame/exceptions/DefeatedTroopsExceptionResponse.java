package com.practice.onlineGame.exceptions;

public class DefeatedTroopsExceptionResponse {
    private String defeatedTroops;

    public DefeatedTroopsExceptionResponse(String defeatedTroops) {
        this.defeatedTroops = defeatedTroops;
    }

    public String getDefeatedTroops() {
        return defeatedTroops;
    }

    public void setDefeatedTroops(String defeatedTroops) {
        this.defeatedTroops = defeatedTroops;
    }
}
