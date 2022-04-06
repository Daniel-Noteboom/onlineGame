package com.practice.onlineGame.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CardsException extends RuntimeException {

    public CardsException(String message) {
        super(message);
    }
}
