package com.practice.onlineGame.models.risk;


import com.practice.onlineGame.models.Game;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Scanner;

@Entity
public class RiskGame extends Game {
    private static final String FILE_LOCATION = "src/main/resources/static/standard_board_one_line.txt";
    private Phase phase;
    private Date createAt;
    private Date updateAt;
    @OneToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    @JoinColumn(name="board_id",nullable = false)
    private Board board;
    @NotNull(message= "Number of players may not be empty")
    @Column(updatable = false)
    private Integer numPlayers;
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

    public RiskGame() throws FileNotFoundException {
        this.phase = Phase.PREGAME;
        this.board = new Board(new Scanner(new File(FILE_LOCATION)).nextLine());
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
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
