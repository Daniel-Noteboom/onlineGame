package com.practice.onlineGame.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class GameTagException extends RuntimeException {

    public GameTagException(String message) {
        super(message);
    }
}
