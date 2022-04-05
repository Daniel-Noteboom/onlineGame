package com.practice.onlineGame.controllers;

import com.practice.onlineGame.models.risk.Player;
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

    //Happy path-will add error path later
    @GetMapping("/{tag}")
    public ResponseEntity<?> getGameById(@PathVariable String tag) {

        RiskGame game = riskGameService.findByTag(tag);

        return new ResponseEntity<RiskGame>(game,HttpStatus.OK);
    }

    @PostMapping("/{tag}/add_player/{player_name}")
    public ResponseEntity<?> addPlayer(@PathVariable String tag, @PathVariable String player_name) {
        RiskGame game = riskGameService.addPlayer(tag, player_name);
        return new ResponseEntity<RiskGame>(game,HttpStatus.OK);
    }
    //Happy path
    @PostMapping("/{tag}/start_game")
    public ResponseEntity<?> startGame(@PathVariable String tag) {

        RiskGame game = riskGameService.startRiskGame(tag);
        return new ResponseEntity<RiskGame>(game,HttpStatus.OK);
    }

    @PostMapping("/{tag}/reinforce_troops/{country}/{number_troops}")
    public ResponseEntity<?> reinforceTroops(@PathVariable String tag, @PathVariable String country,
                                       @PathVariable Integer number_troops) {
        RiskGame game = riskGameService.reinforceTroops(tag, country, number_troops);
        return new ResponseEntity<RiskGame>(game,HttpStatus.OK);
    }

    @PostMapping("/{tag}/attack/{attack_country}/{number_troops}/{defend_country}")
    public ResponseEntity<?> reinforceTroops(@PathVariable String tag, @PathVariable String attack_country,
                                             @PathVariable Integer number_troops, @PathVariable String defend_country) {
        RiskGame game = riskGameService.attack(tag, attack_country, number_troops, defend_country);
        return new ResponseEntity<RiskGame>(game,HttpStatus.OK);
    }
    @Autowired
    public RiskGameController(RiskGameRepository riskGameRepository) {
        this.errorCheck = new ErrorCheckService();
        this.riskGameService = new RiskGameService(riskGameRepository);
    }


}
