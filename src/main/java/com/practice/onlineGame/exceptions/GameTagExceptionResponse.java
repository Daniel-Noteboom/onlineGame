package com.practice.onlineGame.exceptions;

public class GameTagExceptionResponse {

    private String gameTag;

    public GameTagExceptionResponse(String gameTag) {
        this.gameTag = gameTag;
    }

    public String getGameTag() {
        return gameTag;
    }

    public void setGameTag(String gameTag) {
        this.gameTag = gameTag;
    }
}
