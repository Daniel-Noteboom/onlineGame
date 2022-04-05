package com.practice.onlineGame.services;

import com.practice.onlineGame.exceptions.AttackException;
import com.practice.onlineGame.exceptions.GameTagException;
import com.practice.onlineGame.exceptions.ReinforcementException;
import com.practice.onlineGame.models.risk.Player;
import com.practice.onlineGame.models.risk.RiskGame;
import com.practice.onlineGame.repositories.RiskGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
