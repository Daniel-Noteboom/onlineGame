package com.practice.onlineGame.exceptions;

public class AttackExceptionResponse {
    private String attack;

    public AttackExceptionResponse(String attack) {
        this.attack = attack;
    }

    public String getAttack() {
        return attack;
    }

    public void setAttack(String attack) {
        this.attack = attack;
    }
}
