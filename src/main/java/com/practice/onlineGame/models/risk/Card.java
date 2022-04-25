package com.practice.onlineGame.models.risk;

import javax.persistence.*;
import java.util.*;
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    protected Long id;

    private int currentIndex;
    public enum Type {
        INFANTRY,
        CALVARY,
        ARTILLERY,
        ALL
    }

    private static final Map<Type, Integer> CARD_TROOPS_VALUE;
    static {
        Map<Type, Integer> tempMap = new HashMap<>();
        tempMap.put(Type.INFANTRY, 4);
        tempMap.put(Type.CALVARY, 6);
        tempMap.put(Type.ARTILLERY, 8);
        tempMap.put(Type.ALL, 10);

        CARD_TROOPS_VALUE = Collections.unmodifiableMap(tempMap);
    }

    public static int getValue(Type type) {
        return CARD_TROOPS_VALUE.get(type);
    }

    public static Type[] cardTypes() {
        Set<Type> setTypes = new HashSet<>(CARD_TROOPS_VALUE.keySet());
        setTypes.remove(Type.ALL);
        Type[] types = new Type[setTypes.size()];
        setTypes.toArray(types);
        return types;
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

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
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
