package com.practice.onlineGame.models.risk;


import com.practice.onlineGame.models.Game;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Entity
public class RiskGame extends Game {
    public static final int CARD_TYPE_ALL_COUNT = 2;
    public static final int MAX_CARDS = 5;
    public static final int CARD_TURN_IN_SIZE = 3;
    public static final int BONUS_TROOPS_COUNTRY = 2;
    private boolean needTurnInCards = false;
    private boolean turnInCardsTest = false;
    //Keeps track of the different cards.

    @OneToMany
    @JoinTable(name = "risk_card_deck",
            joinColumns = {@JoinColumn(name = "card_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "game_id", referencedColumnName = "id")})
    @OrderColumn
    private List<Card> deck = new ArrayList<>();

    @OneToMany
    @JoinTable(name = "risk_card_discard",
            joinColumns = {@JoinColumn(name = "card_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "game_id", referencedColumnName = "id")})
    @OrderColumn
    private List<Card> discardPile = new ArrayList<>();

    private static final String FILE_LOCATION = "src/main/resources/static/standard_board_one_line.txt";
    public enum Phase {
        DRAFT,
        ATTACK,
        FORTIFY,
        PREGAME,
        ENDGAME
    }
    private Phase phase;
    private Date createAt;
    private Date updateAt;
    @OneToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    @JoinColumn(name="board_id",nullable = false)
    private Board board;
    @NotNull(message= "Number of players may not be empty")
    @Column(updatable = false)
    @Min(3)
    @Max(6)
    private Integer numPlayers;
    @OneToMany
    @JoinTable(name = "risk_game_players",
            joinColumns = {@JoinColumn(name = "player_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "game_id", referencedColumnName = "id")})
    @OrderColumn
    private List<Player> players;
    private int startingPlayerIndex;

    //Keeps track of which players turn it is
    private int currentPlayerIndex;

    //Keeps track of how many troops are left in the draft phase
    private int currentReinforceTroopsNumber;

    //Need to keep track of whether it is the first turn (first turn means first turn for all players)
    private boolean firstTurn = true;

    //Keeps track of post-attack situation
    private int minimumTroopsDefeatedCountry = 0;
    private String currentDefeatedCountry;
    private String currentVictorCountry;

    //Keep track of current attack situation
    private String currentAttackCountry;

    //Variables dealing with adding troops on first turn
    private static final int[][] FIRST_TURN_TROOPS = {{0,0,1}, {0,0,0,1}, {0,0,0,1,2}, {0,0,0,1,2,3}};
    private static final int MINIMUM_TROOPS_ADDED = 3;
    private static final int COUNTRY_TROOP_DIVISOR = 3;
    private static final int[] STARTING_TROOPS = {35, 30, 25, 20};

    //Variables dealing with dice number
    private static final int MAXIMUM_ATTACK = 3;
    private static final int MAXIMUM_DEFENSE = 2;
    public static final int DICE_SIDES = 6;

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
        players = new ArrayList<>();
    }

    public void addPlayer(Player p) {
        numPlayers++;
        players.add(p);
    }

    public void removePlayer(Player p) {
        numPlayers--;
        players.remove(p);
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
