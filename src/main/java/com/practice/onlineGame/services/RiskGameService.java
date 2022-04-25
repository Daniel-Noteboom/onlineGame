package com.practice.onlineGame.services;

import com.practice.onlineGame.exceptions.*;
import com.practice.onlineGame.models.risk.Player;
import com.practice.onlineGame.models.risk.RiskGame;
import com.practice.onlineGame.repositories.RiskGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class RiskGameService {

    private final RiskGameRepository riskGameRepository;
    public RiskGame createRiskGame(RiskGame game) {
        try {
            return save(game);
        } catch(Exception e) {
            throw new GameTagException("Game tag '" + game.getTag() + "' already exists");
        }
    }

    @Autowired
    public RiskGameService(RiskGameRepository riskGameRepository) {
        this.riskGameRepository = riskGameRepository;
    }

    public RiskGame findByTag(String tag) {
        RiskGame game = riskGameRepository.findByTag(tag);

        if(game == null) {
            throw new GameTagException("Game tag '" + tag + "' does not exist");
        }
        return game;
    }

    public RiskGame addPlayer(String tag, String playerName) {
        RiskGame game = findByTag(tag);
        game.addPlayer(new Player(playerName));
        save(game);
        return game;
    }
    public RiskGame startRiskGame(String tag) {
        RiskGame game = findByTag(tag);
        game.startGame();
        save(game);
        return game;
    }

    public RiskGame save(RiskGame game) {
        return riskGameRepository.save(game);
    }
    public RiskGame reinforceTroops(String tag, String country, int troops) {
        country = sanitizeCountry(country);
        RiskGame game = findByTag(tag);
        String errors = game.reinforceTroops(troops, country);
        if(!errors.isEmpty()) {
            throw new ReinforcementException(errors);
        }
        save(game);
        return game;
    }

    private String sanitizeCountry(String country) {
        return Arrays.stream(country.split("_"))
                .reduce("", (s1, s2) -> s1 + " " + s2.substring(0,1).toUpperCase() + s2.substring(1)).substring(1);
    }
    public RiskGame attack(String tag, String attackCountry, int numberDice, String defendCountry) {
        attackCountry = sanitizeCountry(attackCountry);
        defendCountry = sanitizeCountry(defendCountry);
        RiskGame game = findByTag(tag);
        String errors = game.attack(attackCountry, numberDice, defendCountry);
        if(!errors.isEmpty()) {
            throw new AttackException(errors);
        }
        save(game);
        return game;
    }

    public RiskGame endAttack(String tag) {
        RiskGame game = findByTag(tag);
        String errors = game.endAttackPhase();
        if(!errors.isEmpty()) {
            throw new AttackException(errors);
        }
        save(game);
        return game;
    }

    public RiskGame fortify(String tag, String fromCountry, int numberTroops, String toCountry) {
        fromCountry = sanitizeCountry(fromCountry);
        toCountry = sanitizeCountry(toCountry);
        RiskGame game = findByTag(tag);
        String errors = game.fortifyTroops(fromCountry, toCountry, numberTroops);
        if(!errors.isEmpty()) {
            throw new FortifyException(errors);
        }
        save(game);
        return game;
    }

    public RiskGame endFortify(String tag) {
        RiskGame game = findByTag(tag);
        game.endFortifyPhase();
        save(game);
        return game;
    }

    public RiskGame setTroopsDefeatedCountry(String tag, int numberTroops) {
        RiskGame game = findByTag(tag);
        String errors = game.setTroopsDefeatedCountry(numberTroops);
        if(!errors.isEmpty()) {
            throw new DefeatedTroopsException(errors);
        }
        save(game);
        return game;
    }

    public RiskGame turnInCards(String tag) {
        RiskGame game = findByTag(tag);
        String errors = game.turnInCards(game.possibleTurnInCards());
        if(!errors.isEmpty()) {
            throw new CardsException(errors);
        }
        save(game);
        return game;
    }

    public RiskGame turnInCards(String tag, List<Integer> cards) {
        RiskGame game = findByTag(tag);
        String errors = game.turnInCards(cards);
        if(!errors.isEmpty()) {
            throw new CardsException(errors);
        }
        save(game);
        return game;
    }

    public Set<String> getOpposingCountries(String tag, String country) {
        RiskGame game = findByTag(tag);
        return game.getOpposingCountries(sanitizeCountry(country));
    }

    public Set<String> fortifyPossibilities(String tag, String country) {
        RiskGame game = findByTag(tag);
        return game.fortifyPossibilities(sanitizeCountry(country));
    }
}
