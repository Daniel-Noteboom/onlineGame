package com.practice.onlineGame.models.risk;

import javax.persistence.*;
import java.util.*;
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    protected Long id;

    public enum Type {
        INFANTRY,
        CALVARY,
        ARTILLERY,
        ALL
    }

    public static final int COUNTRY_TROOPS_NUMBER = 2;

    private Type type;


    private String country;

    public Card() {
    }

    public Card(Type type, String country) {
        this.type = type;
        this.country = country;
    }

    public Type getType() {
        return type;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        if(country != null) {
            return type + " (" + country + ")";
        } else {
            return type + "";
        }
    }
}
