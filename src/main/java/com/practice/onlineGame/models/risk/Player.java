package com.practice.onlineGame.models.risk;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    protected Long id;
    private String name;
    @JoinTable(name = "risk_game_cards",
            joinColumns = {@JoinColumn(name = "card_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "player_id", referencedColumnName = "id")})
    @OrderColumn
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Card> cards;
    private boolean isOut;
    private boolean attackThisTurn;
    private String color;
    public Player() {

    }
    public Player(String name) {
        this.name = name;
        this.isOut = false;
        this.cards = new ArrayList<Card>();
        this.attackThisTurn = false;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut() {
        isOut = true;
    }

    public void setAttackThisTurn() {
        this.attackThisTurn = true;
    }

    public void unsetAttackThisTurn() {
        this.attackThisTurn = false;
    }

    public boolean attackThisTurn() {
        return attackThisTurn;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    @Override
    public String toString() {
        return name;
    }
}
