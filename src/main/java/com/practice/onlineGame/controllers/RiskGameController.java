package com.practice.onlineGame.controllers;

import com.practice.onlineGame.models.risk.RiskGame;
import com.practice.onlineGame.repositories.RiskGameRepository;
import com.practice.onlineGame.services.ErrorCheckService;
import com.practice.onlineGame.services.RiskGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RequestMapping("/game/risk")
@RestController
@CrossOrigin
public class RiskGameController {
    private ErrorCheckService errorCheck;
    private RiskGameService riskGameService;

    @PostMapping("")
    public ResponseEntity<?> createNewGame(@Valid @RequestBody RiskGame game, BindingResult result) {
        ResponseEntity<?> errorMap = errorCheck.errorCheck(result);
        if(errorMap != null) return errorMap;

        game = riskGameService.createRiskGame(game);
        return new ResponseEntity<RiskGame>(game, HttpStatus.CREATED);

    }

    @Autowired
    public RiskGameController(RiskGameRepository riskGameRepository) {
        this.errorCheck = new ErrorCheckService();
        this.riskGameService = new RiskGameService(riskGameRepository);
    }
}
