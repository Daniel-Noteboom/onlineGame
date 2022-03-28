package com.practice.onlineGame.services;

import com.practice.onlineGame.exceptions.GameTagException;
import com.practice.onlineGame.models.risk.RiskGame;
import com.practice.onlineGame.repositories.RiskGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RiskGameService {

    private final RiskGameRepository riskGameRepository;

    public RiskGame createRiskGame(RiskGame game) {
        return riskGameRepository.save(game);
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
}
