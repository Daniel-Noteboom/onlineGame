package com.practice.onlineGame.models.risk;


import com.practice.onlineGame.models.Game;

import javax.persistence.*;
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

    @OneToMany(cascade=CascadeType.ALL)
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
    private static final int MINIMUM_PLAYERS = 3;
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

    public Board getBoard() {
        return board;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
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
        players.add(p);
    }

    public void removePlayer(Player p) {
        players.remove(p);
    }

    @PrePersist
    protected void onCreate(){
        this.createAt = new Date();
    }

    @PreUpdate
    protected void onUpdate(){
        this.updateAt = new Date();
    }

    private Player getOccupant(String country) {
        return board.getOccupant(country);
    }

    //Get Troop Count  for the country
    public int getTroopCount(String country) {
        return board.getTroopCount(country);
    }


    /**
     *     Creates a randomly populated board with the appropriate number of troops. The first player has priority.
     *     Each country is randomly populated according to the rules of Risk. No country can have more than one troop
     *     until all countries have been populated with at least one troop and troops are added one at at time beginning
     *     with the starting player. Clears the board if there is already troops there
     */

    public void startGame() {
        startingPlayerIndex = (int) (Math.random() * players.size());
        currentPlayerIndex = startingPlayerIndex;
        randomlyPopulateBoard();
    }
    private void randomlyPopulateBoard() {
        board.clearTroops();
        int numberTroopsAdd = STARTING_TROOPS[players.size() - MINIMUM_PLAYERS];
        int countriesOccupied = 0;
        //Randomly populate the countries with one troop each, starting with the first player
        while (!(countriesOccupied == board.numberCountries())) {

            Country currentCountry = board.randomUnoccupiedCountry(countriesOccupied);

            currentCountry.changeOccupant(players.get(currentPlayerIndex), 1);

            countriesOccupied++;
            //Check to see if we have completed a full round of adding troops
            if(isFullTurnCycle()) {
                numberTroopsAdd--;
            }
            nextPlayer();


        }

        while (numberTroopsAdd != 0) {
            board.increaseTroops(board.randomCountry(players.get(currentPlayerIndex)), 1);
            if(isFullTurnCycle()) {
                numberTroopsAdd--;
            }
            nextPlayer();

        }

        if(phase != Phase.DRAFT) {
            changePhase();
        }
    }

    //Returns the current number of reinforcement troops that remain (will return 0 if it is not DRAFT phase)
    public int getCurrentReinforcementTroopsNumber() {
        return currentReinforceTroopsNumber;
    }

    /** Returns and sets the number of troops that the current player can reinforce with
     * If the game phase is not correct 0 is returned **/
    private int setInitialReinforceTroopNumber() {
        if(!(phase == Phase.DRAFT)) {
            return 0;
        }
        int extraTroops = 0;

        if(firstTurn) {
            extraTroops = FIRST_TURN_TROOPS[players.size() - MINIMUM_PLAYERS][currentPlayerIndex];
        }
        currentReinforceTroopsNumber = Math.max(board.countriesOccupied(players.get(currentPlayerIndex)).size() / COUNTRY_TROOP_DIVISOR,
                MINIMUM_TROOPS_ADDED) + extraTroops + board.troopsFromContinents(players.get(currentPlayerIndex));
        return currentReinforceTroopsNumber;
    }


    //Helper method that handles errors for reinforceTroops (refer to that method for what errors it flags)
    private String reinforceTroopsErrorHandling(int troops, String country) {
        String errors = "";
        if(!(phase == Phase.DRAFT || (phase == Phase.ATTACK && currentReinforceTroopsNumber > 0))) {
            errors += "ERROR: Current phase: " + phase + " ";
            errors += "Expected phase: " + Phase.DRAFT + " \n";
        } else if(troops > currentReinforceTroopsNumber) {
            errors += "ERROR: Too many troops requested for draft ";
            errors += "Requested troops: " + troops + " ";
            errors += "Actual troops: " + currentReinforceTroopsNumber + " \n";
        } else if (needTurnInCards) {
            errors += "ERROR: You must turn in cards before reinforcing" + " \n";
        }
        if(board.getOccupant(country) != players.get(currentPlayerIndex)) {
            errors += "ERROR: Current player doesn't have control over " + country + " ";
            errors += "Current player: " + players.get(currentPlayerIndex) + " ";
            errors += "Actual occupant: " + board.getOccupant(country) + " \n";
        }
        return errors;
    }
    /**
     * Reinforce troops for the given country with the amount of troops specified. The country must be occupied by the current player,
     * The current phase must be draft, and the current player must have enough troops to reinforce that country with the specified amount of
     * troops.
     * @param troops Amount of troops to reinforce with
     * @param country Country specified
     * @return True if successful, false if one or more errors was present
     */
    public String reinforceTroops(int troops, String country) {
        String errors = reinforceTroopsErrorHandling(troops, country);

        if(errors.isEmpty()) {
            board.increaseTroops(country, troops);
            currentReinforceTroopsNumber -= troops;
            if (currentReinforceTroopsNumber == 0 && phase == Phase.DRAFT) {
                changePhase();
            }
        }
        return errors;
    }

    //Refer to comments for attack
    public String attack(String attackCountry, int numberAttackDice, String defendCountry) {
        int defendDice = Math.min(2, getTroopCount(defendCountry));
        return attack(attackCountry, rollDice(numberAttackDice), defendCountry, rollDice(defendDice));
    }

    /**
     * Performs on attack by attackCountry on defendCountry using the dice, attackDice and defendDice respectively. This method
     * will update the current troops in each country if there are no errors in the attack. The attacker takes over the defend
     * country if there are no more troops left. If the attacker has control over every country, the game is over.
     * @param attackCountry The country that is attacking
     * @param attackDice The dice that the attacker has rolled
     * @param defendCountry The country that is defending
     * @param defendDice The dice that the defender has rolled
     * @return Whether the attack was successful or not (if there was errors with teh attack returns false). If one of these
     * conditions is not met, this method will return false
     *
     *     Conditions:
     *     1.  The length of attackDice must be a number from 1-3 and <= (attackCountry troops - 1) and attackCountry troops must be
     *     greater than one
     *     2.  attackCountry and defendCountry must be different players
     *     3.  attackCountry and defendCountry must be adjacent countries
     *     4. The board must contain these countries.
     *     5. The length of defend dice must be two if the defender has at least two troops, and one otherwise
     */
    public String attack(String attackCountry, List<Integer> attackDice, String defendCountry, List<Integer> defendDice) {
        String errors = attackErrorHandling(attackCountry, attackDice, defendCountry, defendDice);
        if(!errors.isEmpty()) {
            return errors;
        }
        currentAttackCountry = attackCountry;
        List<Integer> sortedAttackDice = new ArrayList<Integer>(attackDice);
        List<Integer> sortedDefendDice = new ArrayList<Integer>(defendDice);
        Collections.sort(sortedAttackDice);
        Collections.sort(sortedDefendDice);

        //Whoever has the lowest highest die loses a troop
        if (sortedAttackDice.get(sortedAttackDice.size() - 1) > sortedDefendDice.get(sortedDefendDice.size() - 1)) {
            board.reduceTroops(defendCountry, 1);
        } else {
            board.reduceTroops(attackCountry, 1);
        }

        //If there are two dice in play by both attack/defender, whoever has the lowest second highest die loses a troop
        if (sortedAttackDice.size() > 1 && sortedDefendDice.size() > 1 && sortedAttackDice.get(sortedAttackDice.size() - 2) >
                sortedDefendDice.get(sortedDefendDice.size() - 2)) {
            board.reduceTroops(defendCountry, 1);
        } else if (sortedAttackDice.size() > 1 && sortedDefendDice.size() > 1) {
            board.reduceTroops(attackCountry, 1);
        }

        //Take over the country if there are no more troops
        if(board.getTroopCount(defendCountry) == 0) {
            players.get(currentPlayerIndex).setAttackThisTurn();
            Player occupantDefeated = getOccupant(defendCountry);
            board.changeOccupant(defendCountry, players.get(currentPlayerIndex), 0);
            if(board.playerFinished(occupantDefeated)) {
                occupantDefeated.setOut();
                for(Card card: occupantDefeated.getCards()) {
                    players.get(currentPlayerIndex).addCard(card);
                }
                if(players.get(currentPlayerIndex).getCards().size() >= MAX_CARDS) {
                    needTurnInCards = true;
                }
            }
            if(isGameOver()) {
                phase = Phase.ENDGAME;
            } else {
                currentDefeatedCountry = defendCountry;
                currentVictorCountry = attackCountry;
                minimumTroopsDefeatedCountry = attackDice.size();
            }
        }
        return errors;
    }

    //Handles errors for the attack method. For specifics on what is allowed/not allowed refer to the attack method
    private String attackErrorHandling(String attackCountry, List<Integer> attackDice, String defendCountry,
                                        List<Integer> defendDice) {
        String errors = "";
        //Refer to error messages for explanation of if statement
        if (phase != Phase.ATTACK) {
            errors += "Phase is not correct. It should be attack phase but is actually " + phase;
        } else if (attackDice.size() < 1 || attackDice.size() > MAXIMUM_ATTACK || attackDice.size() >= getTroopCount(attackCountry)) {
            errors += "You can have 1-3 attack dice and the number of dice has to " +
                    "be less than the number of troops in the country";
        } else if (getOccupant(attackCountry) != players.get(currentPlayerIndex)) {
            errors += "You can only attack if it's your turn";
        } else if (defendDice.size() != Math.min(getTroopCount(defendCountry), MAXIMUM_DEFENSE)) {
            errors += "Defender must defend with either one or two dice (one dice if defender only has one troop)";
        } else if (getOccupant(attackCountry) == getOccupant(defendCountry)) {
            errors += "You are not allowed to attack yourself";
        } else if (!board.isBordering(attackCountry, defendCountry))  {
            errors += "You must attack an adjacent country";
        } else if (!board.containsCountry(attackCountry) || !board.containsCountry(defendCountry)) {
            errors += "Board must contain the countries that are attacking";
        } else if (needTurnInCards) {
            errors += "You need to turn in cards before attacking since you have more than " + MAX_CARDS + " cards";
        }
        return errors;
    }
    /** Helper method that returns whether all players have completed an entire cycle. This does not determine whether the final player
     in the cycle has completed their moves. The method should be called at the appropriate time (when the current player
     has finished their turn.**/
    private boolean isFullTurnCycle() {
        return currentPlayerIndex == (startingPlayerIndex == 0 ? players.size() - 1 : startingPlayerIndex - 1);
    }

    private void changePhase() {
        if(phase == Phase.PREGAME) {
            phase = Phase.DRAFT;
            setInitialReinforceTroopNumber();
        } else if(phase == Phase.DRAFT) {
            phase = Phase.ATTACK;
        } else if(phase == Phase.ATTACK) {
            phase = Phase.FORTIFY;
        } else if(phase == Phase.FORTIFY) {
            if(firstTurn && isFullTurnCycle()) {
                firstTurn = false;
            }
            phase = Phase.DRAFT;
            nextPlayer();
            setInitialReinforceTroopNumber();
        }
    }


    //Helper method Changes board state so that is now the next player's turn.
    private void nextPlayer() {
        currentPlayerIndex = currentPlayerIndex == players.size() - 1 ? 0 : currentPlayerIndex + 1;
        if(players.get(currentPlayerIndex).isOut()) {
            nextPlayer();
        }
    }

    //Determines whether the game is over (only one player remains)
    private boolean isGameOver() {
        int numberOut = 0;
        for(Player p: players) {
            if (p.isOut()) {
                numberOut++;
            }
        }
        return numberOut == players.size() - 1;
    }

    /**
     If it is outside those bounds then an empty List is returned
     Returns an List<Integer> with random numbers of 1-DICE_SIDES **/

    /**
     * Returns random dice rolls for "numberDice" times. The randomizer is set to return a random
     *      * Number from 1-DICE_SIDES
     * @param numberDice The number of dice to be rolled (must be a number from 0 to 1000)
     * @return The random dice result with a size of "numberDice". If numberDice is not correct an empty list is returned.
     */
    public static List<Integer> rollDice(int numberDice) {
        if (numberDice > 1000 || numberDice < 1) {
            return new ArrayList<Integer>();
        }
        List<Integer> diceRolls = new ArrayList<Integer>();
        for(int i = 0; i < numberDice; i++) {
            // Produces a random number from 1 to DICE_SIDES
            diceRolls.add((int) (Math.random() * DICE_SIDES + 1));
        }
        return diceRolls;
    }
}
