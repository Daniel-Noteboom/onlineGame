package com.practice.onlineGame.models.risk;

import java.util.*;

public class CardTypes {
    private static final Map<Card.Type, Integer> CARD_TROOPS_VALUE;
    static {
        Map<Card.Type, Integer> tempMap = new HashMap<>();
        tempMap.put(Card.Type.INFANTRY, 4);
        tempMap.put(Card.Type.CALVARY, 6);
        tempMap.put(Card.Type.ARTILLERY, 8);
        tempMap.put(Card.Type.ALL, 10);

        CARD_TROOPS_VALUE = Collections.unmodifiableMap(tempMap);
    }

      public static int getValue(Card.Type type) {
        return CARD_TROOPS_VALUE.get(type);
      }

    public static Card.Type[] cardTypes() {
        Set<Card.Type> setTypes = new HashSet<>(CARD_TROOPS_VALUE.keySet());
        setTypes.remove(Card.Type.ALL);
        Card.Type[] types = new Card.Type[setTypes.size()];
        setTypes.toArray(types);
        return types;
    }
}
