package com.practice.onlineGame.services;

import com.practice.onlineGame.models.risk.RiskGame;
import com.practice.onlineGame.repositories.RiskGameRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class RiskGameService {

    private final RiskGameRepository riskGameRepository;

    public RiskGame createRiskGame(RiskGame game) {
        return riskGameRepository.save(game);
    }

    @Autowired
    public RiskGameService(RiskGameRepository riskGameRepository) {
        this.riskGameRepository = riskGameRepository;
    }
}
