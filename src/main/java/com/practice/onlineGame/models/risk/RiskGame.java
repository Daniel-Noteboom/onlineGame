package com.practice.onlineGame.models.risk;


import com.practice.onlineGame.models.Game;

import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

@Entity
public class RiskGame extends Game {
    private Phase phase;
    private Date createAt;
    private Date updateAt;
    public enum Phase {
        DRAFT,
        ATTACK,
        FORTIFY,
        PREGAME,
        ENDGAME
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    public RiskGame() {
        this.phase = Phase.PREGAME;
    }

    @PrePersist
    protected void onCreate(){
        this.createAt = new Date();
    }

    @PreUpdate
    protected void onUpdate(){
        this.updateAt = new Date();
    }
}
