package com.practice.onlineGame.models.risk;

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
    @OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private List<Card> cards;
    private boolean out;
    private boolean attackThisTurn;

    public Player() {

    }
    public Player(String name) {
        this.name = name;
        this.cards = new ArrayList<Card>();
        this.out = false;
        this.attackThisTurn = false;
    }

    public String getName() {
        return name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public boolean isOut() {
        return out;
    }

    public void setOut() {
        out = true;
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
