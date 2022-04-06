package com.practice.onlineGame.exceptions;

public class FortifyExceptionResponse {

    private String fortify;

    public FortifyExceptionResponse(String fortify) {
        this.fortify = fortify;
    }

    public String getFortify() {
        return fortify;
    }

    public void setFortify(String fortify) {
        this.fortify = fortify;
    }
}
