package com.practice.onlineGame.services;

import com.practice.onlineGame.exceptions.*;
import com.practice.onlineGame.models.risk.Player;
import com.practice.onlineGame.models.risk.RiskGame;
import com.practice.onlineGame.repositories.RiskGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskGameService {

    private final RiskGameRepository riskGameRepository;
    public RiskGame createRiskGame(RiskGame game) {
        return save(game);
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
        RiskGame game = findByTag(tag);
        String errors = game.reinforceTroops(troops, country);
        if(!errors.isEmpty()) {
            throw new ReinforcementException(errors);
        }
        save(game);
        return game;
    }

    public RiskGame attack(String tag, String attackCountry, int numberDice, String defendCountry) {
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
        RiskGame game = findByTag(tag);
        String errors = game.fortifyTroops(fromCountry, toCountry, numberTroops);
        if(!errors.isEmpty()) {
            throw new FortifyException(errors);
        }
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
        String errors = game.turnInCards(game.turnInCards());
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

}
