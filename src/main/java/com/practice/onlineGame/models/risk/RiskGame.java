package com.practice.onlineGame.models.risk;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private boolean canTurnInCards = false;
    private boolean turnInCardsTest = false;

    private static final String[] COLORS = {"red", "yellow", "blue", "green", "orange", "purple"};
    //Keeps track of the different cards.

    @OneToMany(cascade=CascadeType.ALL)
    @JoinTable(name = "risk_card_deck",
            joinColumns = {@JoinColumn(name = "card_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "game_id", referencedColumnName = "id")})
    @OrderColumn
    private List<Card> deck = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
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
    @JsonIgnore
    private int currentPlayerIndex;

    private String currentPlayerName;

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public void setCurrentPlayerName(String currentPlayerName) {
        this.currentPlayerName = currentPlayerName;
    }

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
        createDeck();
    }

    public void addPlayer(Player p) {
        if(phase == Phase.PREGAME) {
            p.setColor(COLORS[players.size()]);
            players.add(p);
        }
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
    //Returns bordering countries with different occupant of concerned country
    public Set<String> getOpposingCountries(String country) {
        return board.opposingCountries(country);
    }

    /**
     * Calculates all the fortify possibilities for the current country
     *
     * @param country The name of the country to find the reinforcement possibilities for
     * @return Set<String> that contains the reinforcement possibilities as a Set of the country names
     */
    public Set<String> fortifyPossibilities(String country) {
        if(board.getOccupant(country) != players.get(currentPlayerIndex) || phase != Phase.FORTIFY) {
            return new HashSet<String>();
        }
        return board.connectedCountries(country);

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
        currentPlayerName = players.get(currentPlayerIndex).getName();
        randomlyPopulateBoard();
    }

    public List<Player> getPlayers() {
        return players;
    }

    /**
     *     Creates a randomly populated board with the appropriate number of troops. The first player has priority.
     *     Each country is randomly populated according to the rules of Risk. No country can have more than one troop
     *     until all countries have been populated with at least one troop and troops are added one at at time beginning
     *     with the starting player. Clears the board if there is already troops there
     */
    public void randomlyPopulateBoard() {
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
                for(Card card: occupantDefeated.getCards()) {
                    players.get(currentPlayerIndex).addCard(card);
                }
                occupantDefeated.setOut();
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

    /**
     * End the current attack phase. Only works if the game is in the right state
     * @return Whether the change of phase was successful
     */
    public String endAttackPhase() {
        String errors = errorHandlingEndAttackPhase();
        if(errors.isEmpty()) {
            changePhase();
        }
        return errors;
    }

    //Helper method that tracks errors for endAttack (refer to that method for more details)
    private String errorHandlingEndAttackPhase() {
        String errors = "";
        if(phase != Phase.ATTACK) {
            errors += "You must be in the attack phase to end the attack phase";
        } else if(minimumTroopsDefeatedCountry != 0) {
            errors += "You still need to set troops for " + currentDefeatedCountry + " before you can end attack phase";
        }
        return errors;
    }


    //Since a player can choose not to participate in the fortify phase, they may call this method to skip this phase.
    public void endFortifyPhase() {
        if(phase == Phase.FORTIFY) {
            addAttackCards();
            players.get(currentPlayerIndex).unsetAttackThisTurn();
            changePhase();
        }
    }

    //Handles errors for the fortify troops method
    private String fortifyTroopsErrorHandling(String countryFrom, String countryTo, int troops) {
        String errors = "";
        if(!(phase == Phase.FORTIFY)) {
            errors += "ERROR: Current phase: " + phase + " ";
            errors += "Expected phase: " + Phase.FORTIFY;
        } else if(troops >= board.getTroopCount(countryFrom)) {
            errors += "ERROR: Too many troops requested for reinforcements ";
            errors += "Requested troops: " + troops + " ";
            errors += "Actual troops: " + currentReinforceTroopsNumber;
        } else if(board.getOccupant(countryFrom) != players.get(currentPlayerIndex)) {
            errors += "ERROR: Current player doesn't have control over " + countryFrom + " ";
            errors += "Current player" + players.get(currentPlayerIndex) + " ";
            errors += "Actual occupant + " + board.getOccupant(countryFrom);
        } else if (board.getOccupant(countryTo) != players.get(currentPlayerIndex)) {
            errors += "ERROR: Current player doesn't have control over " + countryTo + " ";
            errors += "Current player" + players.get(currentPlayerIndex) + " ";
            errors += "Actual occupant + " + board.getOccupant(countryTo);
        } else if(!board.connectedCountries(countryFrom).contains(countryTo)) {
            errors += "ERROR: Country " + countryTo + " Is not connected to " + countryFrom;
        }
        return errors;
    }

    /**
     * Fortify int troops from country "start" to country "end". Does not fortify if info provided is incorrect
     * @param countryFrom The country you're moving troops from
     * @param countryTo The country you're moving troops to
     * @param troops the amount of troops you're moving
     * @return Whether it was successful (there needs to be enough troops and the countries need to
     * match the right occupant
     */
    public String fortifyTroops(String countryFrom, String countryTo, int troops) {
        String errors = fortifyTroopsErrorHandling(countryFrom, countryTo, troops);
        if(errors.isEmpty()) {
            board.reduceTroops(countryFrom, troops);
            board.increaseTroops(countryTo, troops);
            endFortifyPhase();
        }
        return errors;
    }

    /**
     * Helper method that creates the deck for the game. Creates deck with the number of countries and some additional
     * cards that are of all type category (number is et by the CARD_TYPE_ALL_COUNT field). Each country that is not ALL
     * is represented as a part of the card and that country may get additional troops if the current player owns that country
     * (only one country allowed per turn in of cards)
     */
    private void createDeck() {
        Set<String> setCountryNames = board.countryNames();

        List<String> countries = new ArrayList<>(setCountryNames);

        List<Card> unshuffledCards = new ArrayList<Card>();
        int countriesSize = countries.size();
        for(int i = 0; i < countriesSize; i++) {
            unshuffledCards.add(new Card(Card.cardTypes()[i % Card.cardTypes().length],
                    countries.remove((int) (Math.random() * countries.size()))));
        }
        for (int i = 0; i < CARD_TYPE_ALL_COUNT; i++) {
            unshuffledCards.add(new Card(Card.Type.ALL, null));
        }
        for(int i = 0; i < setCountryNames.size() + CARD_TYPE_ALL_COUNT; i++) {
            deck.add(unshuffledCards.remove((int) (Math.random() * unshuffledCards.size())));
        }
    }
    private Card getNextCard() {
        if(deck.size() == 0) {
            shuffleDeck();
        }
        return deck.remove(0);
    }
    /**
     * Shuffles deck is the current deck is empty. Cards in the discard pile are randomly placed back into the deck.
     * @return True if the shuffle was successful (there aren't any cards left in deck), false otherwise
     */
    private boolean shuffleDeck() {
        if(deck.size() != 0) {
            System.err.println("You should not shuffle for a new deck unless deck is empty");
            return false;
        }
        int discardPileSize = discardPile.size();
        for(int i = 0; i < discardPileSize; i++) {
            deck.add(discardPile.remove((int) (Math.random() * discardPile.size())));
        }
        return true;
    }
    private void addAttackCards() {
        if(players.get(currentPlayerIndex).attackThisTurn()) {
            players.get(currentPlayerIndex).addCard(getNextCard());
        }
    }

    /**
     * Moves the correct number of troops from attack country to defeated country. Does not wor kif the phase is not correct,
     * this attack did not just finish or if the number of troops is not at least as large as the amount of dice the attacker
     * used to attack the country and is less than the number of troops the attacker has in the attacking country
     * @param troops The number of troops to move.
     * @return Whether the attack was successful or not
     */
    public String setTroopsDefeatedCountry(int troops) {
        String errors = errorHandlingSetTroopsDefeatedCountry(troops);
        if(errors.isEmpty()) {
            board.increaseTroops(currentDefeatedCountry, troops);
            board.reduceTroops(currentVictorCountry, troops);
            minimumTroopsDefeatedCountry = 0;
            currentVictorCountry = null;
            currentDefeatedCountry = null;
            setNeedTurnInCards();
        }
        return errors;
    }

    //Helper method that handles errors setTroopsDefeatedCountry. Refer to that method for details about the errors
    private String errorHandlingSetTroopsDefeatedCountry(int troops) {
        String errors = "";
        if(phase != Phase.ATTACK) {
            errors += "You are not in attack phase so there is no troops to set for a defeated country";
        } else if(minimumTroopsDefeatedCountry == 0) {
            errors += "There is no current defeated country";
        } else if(troops < minimumTroopsDefeatedCountry || troops >= getTroopCount(currentVictorCountry)) {
            errors += "The number of troops requested is " + troops + " ";
            errors += "The number of troops must be at least " + minimumTroopsDefeatedCountry + " ";
            errors += "The number of troops must be less than " + getTroopCount(currentVictorCountry);
        } else if(needTurnInCards) {
            errors += "You still need to turn in cards since you have at least " + MAX_CARDS;
        }
        return errors;
    }

    /**
     * Determines whether you can turn in cards without actually turning them in
     * @param indexes The indexes of the cards in the current players hand to turn in
     * @return true if possible to turn in these cards, false otherwise
     */
    public boolean canTurnInCards(List<Integer> indexes) {
        turnInCardsTest = true;
        String errors = turnInCards(indexes);
        turnInCardsTest = false;
        return errors.isEmpty();
    }

    /**
     * Determines if there are cards that can be turned in, and if there is, returns the indexes of these cards. Assumes that
     * you are looking for cards for the current given player, and does not check to make sure that you are in the right phase to
     * turn in cards, but simply looks at the available cards in the deck
     * @return Sample cards that can be turned in, or an empty list if it is not possible to turn in cards.
     */
    public List<Integer> possibleTurnInCards() {
        Map<Card.Type, List<Integer>> cardTypeCount = new HashMap<>();
        List<Card> cards = players.get(currentPlayerIndex).getCards();
        for(int i = 0; i < cards.size(); i++) {
            if(!cardTypeCount.containsKey(cards.get(i).getType())) {
                List<Integer> indexList = new ArrayList<>();
                indexList.add(i);
                cardTypeCount.put(cards.get(i).getType(), indexList);
            } else {
                cardTypeCount.get(cards.get(i).getType()).add(i);
            }
        }
        int allTypeCount = 0;

        if(cardTypeCount.containsKey(Card.Type.ALL)) {
            allTypeCount = cardTypeCount.get(Card.Type.ALL).size();
        }

        List<Integer> returnIndexList = new ArrayList<>();

        //You have at least one set of cards that are all different, so grab one of each (using as little ALL cards as possible)
        if(cardTypeCount.keySet().size() >= CARD_TURN_IN_SIZE || (cardTypeCount.keySet().size() + (allTypeCount - 1)) >= CARD_TURN_IN_SIZE) {
            for(Card.Type type: cardTypeCount.keySet()) {
                if (type != Card.Type.ALL) {
                    returnIndexList.add(cardTypeCount.get(type).get(0));
                }
            }
            int allIndex = 0;
            while(returnIndexList.size() != CARD_TURN_IN_SIZE) {
                returnIndexList.add(cardTypeCount.get(Card.Type.ALL).get(allIndex));
                allIndex++;
            }
            return returnIndexList;
        }
        //Get three of a kind, not using all card if possible)
        for(Card.Type type: cardTypeCount.keySet()) {
            if(cardTypeCount.get(type).size() + allTypeCount >= CARD_TURN_IN_SIZE) {
                returnIndexList = cardTypeCount.get(type).subList(0, Math.min(CARD_TURN_IN_SIZE, cardTypeCount.get(type).size()));
            }
            if(returnIndexList.size() != CARD_TURN_IN_SIZE) {
                int allIndex = 0;
                while (returnIndexList.size() != CARD_TURN_IN_SIZE && allTypeCount > allIndex) {
                    returnIndexList.add(cardTypeCount.get(Card.Type.ALL).get(allIndex));
                    allIndex++;
                }
            }
            if(returnIndexList.size() == CARD_TURN_IN_SIZE) {
                return returnIndexList;
            }
        }
        return new ArrayList<>();
    }
    /**
     * Turns in cards for the given player. Updates reinforcements and troops for corresponding country if successful
     * as well as removing cards from players hand and placing in discard pile. Also updates whether the player continues
     * to need to turn in cards.
     * @param turnInCardsIndexes The suggested indicies of cards to be turned in for (assume current player is turning in cards)
     * @return true if turning in cards is successful
     * False if it is not by below conditions
     * 1. Player doesn't have enough cards
     * 2. Not right amount of cards that player is wanting to turn in for the given phase
     * 3. Wrong phase for turning in cards
     * 4. Indices are repeated for the desired turninCards
     * 5. Cards can't be turned in (not all same or all different)
     *
     */
    public String turnInCards(List<Integer> turnInCardsIndexes) {
        String errors = turnInCardsErrorHandling(turnInCardsIndexes);
        if(!errors.isEmpty()) {
            return errors;
        }
        Set<Card.Type> typeSeen = new HashSet<>();
        boolean allDifferent = true;
        boolean allSame = false;
        //Create a copy of the players cards
        List<Card> playerCards = new ArrayList<>(players.get(currentPlayerIndex).getCards());
        String firstCountryOccupied = null;
        List<Card> turnInCards = new ArrayList<>();
        int allTypeCardsSeen = 0;
        for(int i: turnInCardsIndexes) {
            if(i >= playerCards.size()) {
                errors += "ERROR: Player does not have that many cards ";
                errors += "Desired card number: " + (i + 1) + " ";
                errors += "Actual number cards: " + playerCards.size();
                return errors;
            }
            Card currentCard = playerCards.get(i);
            turnInCards.add(currentCard);
            if(firstCountryOccupied == null && currentCard.getType() != Card.Type.ALL && board.getOccupant(currentCard.getCountry()).equals(players.get(currentPlayerIndex))) {
                firstCountryOccupied = currentCard.getCountry();
            }
            if(currentCard.getType() != Card.Type.ALL && typeSeen.contains(currentCard.getType())) {
                allDifferent = false;
            }
            if(currentCard.getType() == Card.Type.ALL) {
                allTypeCardsSeen++;
            }
            typeSeen.add(currentCard.getType());
        }
        if((typeSeen.size() == 1 && !typeSeen.contains(Card.Type.ALL)) ||
                (typeSeen.size() == 2 && typeSeen.contains(Card.Type.ALL) && !allDifferent)) {
            allSame = true;
        }
        if(!(allDifferent || allSame)) {
            errors += "ERROR: Cards must either all be the same or all be different ";
            errors += "Types seen: " + typeSeen;
            return errors;
        }
        //Finish if testing cards
        if(turnInCardsTest) {
            return errors;
        }
        if(allDifferent) {
            currentReinforceTroopsNumber += Card.getValue(Card.Type.ALL);
        } else {
            for(Card.Type type: typeSeen) {
                if(type != Card.Type.ALL) {
                    currentReinforceTroopsNumber += Card.getValue(type);
                    break;
                }
            }
        }
        if(firstCountryOccupied != null) {
            board.increaseTroops(firstCountryOccupied, BONUS_TROOPS_COUNTRY);
        }
        for(Card c: turnInCards) {
            discardPile.add(c);
            players.get(currentPlayerIndex).removeCard(c);
        }
        if(players.get(currentPlayerIndex).getCards().size() < MAX_CARDS && needTurnInCards) {
            needTurnInCards = false;
        }
        setCanTurnInCards();
        if(phase == Phase.ATTACK && !needTurnInCards) {
            phase = Phase.DRAFT;
        }
        //Manually change phase to draft so that player can reinforce troops
        return errors;
    }

    /**
     * Checks for errors when player wants to turn in certain cards. Checks for errors that
     * can be checked for before looking at each card, but not errors that can only be determined
     * after looking at each card
     * @param turnInCards Indices representing the cards that player wants to turn in
     * @return False if preconditions not met (specified in regular method)
     * Returns true if all of the conditions are met.
     *
     */
    private String turnInCardsErrorHandling(List<Integer> turnInCards) {
        List<Card> playerCards = new ArrayList<>(players.get(currentPlayerIndex).getCards());
        String errors = "";
        if (playerCards.size() < CARD_TURN_IN_SIZE) {
            errors += "ERROR: You must have at least " + CARD_TURN_IN_SIZE + " cards to turn in ";
            errors += "Current number of cards " + players.get(currentPlayerIndex).getCards().size();
        } else if(turnInCards.size() != CARD_TURN_IN_SIZE) {
            errors += "ERROR: You must turn in the correct amount of cards ";
            errors += "Card number to turn in: " + CARD_TURN_IN_SIZE + " ";
            errors += "Cards attempted to turn in " + turnInCards.size();
        } else if(phase != Phase.ATTACK && phase != Phase.DRAFT) {
            errors += "ERROR: You can only turn in cards in draft or attack phase ";
            errors += "Current phase: " + phase;
        } else if(phase == Phase.ATTACK && playerCards.size() < MAX_CARDS) {
            errors += "ERROR: You can only turn in cards on attack phase if you have at least " + MAX_CARDS + " ";
            errors += "Current number cards: " + playerCards.size();
        } else if(new HashSet<>(turnInCards).size() != CARD_TURN_IN_SIZE) {
            errors += "ERROR: You can't turn in the same card twice";
        }
        return errors;
    }

    private void setNeedTurnInCards() {
        if(players.get(currentPlayerIndex).getCards().size() >= MAX_CARDS) {
            needTurnInCards = true;
        }
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
            setNeedTurnInCards();
            setCanTurnInCards();
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
            setNeedTurnInCards();
            setCanTurnInCards();
        }
    }


    //Helper method Changes board state so that is now the next player's turn.
    private void nextPlayer() {
        currentPlayerIndex = currentPlayerIndex == players.size() - 1 ? 0 : currentPlayerIndex + 1;
        currentPlayerName = players.get(currentPlayerIndex).getName();
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

    public int getMinimumTroopsDefeatedCountry() {
        return minimumTroopsDefeatedCountry;
    }

    public void setMinimumTroopsDefeatedCountry(int minimumTroopsDefeatedCountry) {
        this.minimumTroopsDefeatedCountry = minimumTroopsDefeatedCountry;
    }

    public String getCurrentDefeatedCountry() {
        return currentDefeatedCountry;
    }

    public void setCurrentDefeatedCountry(String currentDefeatedCountry) {
        this.currentDefeatedCountry = currentDefeatedCountry;
    }

    public String getCurrentVictorCountry() {
        return currentVictorCountry;
    }

    public void setCurrentVictorCountry(String currentVictorCountry) {
        this.currentVictorCountry = currentVictorCountry;
    }

    public boolean isNeedTurnInCards() {
        return needTurnInCards;
    }

    public void setNeedTurnInCards(boolean needTurnInCards) {
        this.needTurnInCards = needTurnInCards;
    }

    private void setCanTurnInCards() {
        canTurnInCards = canTurnInCards(possibleTurnInCards());
    }

    public boolean isCanTurnInCards() {
        return canTurnInCards;
    }

    public void setCanTurnInCards(boolean canTurnInCards) {
        this.canTurnInCards = canTurnInCards;
    }
}
