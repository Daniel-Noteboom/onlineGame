package com.practice.onlineGame.repositories;

import com.practice.onlineGame.models.risk.RiskGame;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskGameRepository extends CrudRepository<RiskGame, Long> {
    RiskGame findByTag(String tag);
}

