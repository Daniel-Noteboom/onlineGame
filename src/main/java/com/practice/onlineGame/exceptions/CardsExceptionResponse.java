package com.practice.onlineGame.exceptions;

public class CardsExceptionResponse {
    private String cards;

    public CardsExceptionResponse(String cards) {
        this.cards = cards;
    }

    public String getCards() {
        return cards;
    }

    public void setCards(String cards) {
        this.cards = cards;
    }
}
