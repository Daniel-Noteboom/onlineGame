package com.practice.onlineGame.models.risk;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    protected Long id;
    // Contains all continents in the board
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "board_continents",
            joinColumns = {@JoinColumn(name = "continent_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "board_id", referencedColumnName = "id")})
    private Set<Continent> continents;

    //Maps the name of the country to the country
      @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
      @JoinTable(name = "board_countries",
        joinColumns = {@JoinColumn(name = "board_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "country_id", referencedColumnName = "id")})
      @MapKeyJoinColumn(name = "country_name")
      private Map<String, Country> countryNames;

    public Board() {

    }
    
    public Board(String jsonBoard) {
        this.continents = new HashSet<>();
        this.countryNames = new HashMap<>();
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(jsonBoard);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        JSONObject countries = (JSONObject) json.get("countries");
        for(Object oString: countries.keySet()) {
            String countryName = (String) oString;
            JSONArray borderCountries = (JSONArray) ((JSONObject) countries.get(countryName)).get("bordering_countries");
            Country country = getOrCreateCountry(countryName);
            for(Object borderObjectString: borderCountries) {
                String borderCountryName = (String) borderObjectString;
                Country borderCountry = getOrCreateCountry(borderCountryName);
                country.addBorderingCountry(borderCountry);
            }
        }
        JSONObject continents = (JSONObject) json.get("continents");

        for(Object oString: continents.keySet()) {
            String continentName = (String) oString;
            JSONObject continentJSON = (JSONObject) continents.get(continentName);
            long troopValue = (long) continentJSON.get("troop_value");
            JSONArray contCountries = (JSONArray) continentJSON.get("countries");
            Set<Country> setContCountries = new HashSet<>();
            for(Object oCountryString: contCountries) {
                String countryName = (String) oCountryString;
                Country country = getCountry(countryName);
                setContCountries.add(country);
            }

            Continent continent = new Continent((int) troopValue, setContCountries, continentName);
            this.continents.add(continent);
        }
    }

    public Set<String> countryNames() {
        return countryNames.keySet();
    }

    private Country getOrCreateCountry(String countryName) {
        if(!countryNames.containsKey(countryName)) {
            Country country = new Country(countryName);
            countryNames.put(countryName, country);
        }
        return countryNames.get(countryName);
    }

    public Set<Continent> getContinents() {
        return continents;
    }

    public Player getOccupant(String country) {
        return getCountry(country).getOccupant();
    }

    public void reduceTroops(String country, int troops) {
        getCountry(country).reduceTroops(troops);
    }

    public void increaseTroops(String country, int troops) {
        getCountry(country).increaseTroops(troops);
    }

    public int getTroopCount(String country) {
        return getCountry(country).getTroopCount();
    }

    public void changeOccupant(String country, Player occupant, int troops) {
        getCountry(country).changeOccupant(occupant, troops);
    }

    //Returns whether this board contains this country
    public boolean containsCountry(String country) {
        return countryNames.containsKey(country);
    }

    //Returns whether Country A is bordering Country B
    public boolean isBordering(String countryA, String countryB) {
        return getCountry(countryA).borders(getCountry(countryB));
    }
    /**
     * Determines whether the player is done or not. They are done if they no longer control any countries
     * @param player The player that we're seeing is done
     * @return Whether the player is done
     */
    public boolean playerFinished(Player player) {
        for(Country c: countryNames.values()) {
            if(c.getOccupant() == player) {
                return false;
            }
        }
        return true;
    }
    public Map<String, Country> getCountryNames() {
        return countryNames;
    }

    public void clearTroops() {
        for(Country c: countryNames.values()) {
            c.changeOccupant(null, 0);
        }
    }
    /**
     * Returns all countries bordering country with name countryName whose occupant is different than the current
     * country with countryName
     * @param countryName The name of the country
     * @return The countries bordering with different occupants
     */
    public Set<String> opposingCountries(String countryName) {
        Country country = countryNames.get(countryName);
        Set<Country> borderCountries = country.getBorderingCountries();
        Set<String> opposingCountries = new HashSet<>();
        for(Country c: borderCountries) {
            if(c.getOccupant() != country.getOccupant()) {
                opposingCountries.add(c.getName());
            }
        }
        return opposingCountries;
    }

    //Returns a randomCountry belonging to Player p
    public String randomCountry(Player p) {
        Set<Country> currentPlayerCountries = countriesOccupied(p);
        int randomCountry = (int) (Math.random() * currentPlayerCountries.size());
        int countriesCovered = 0;
        for(Country c: currentPlayerCountries) {
            if (countriesCovered == randomCountry) {
                return c.getName();
            }
            countriesCovered++;
        }
        return "";
    }
    //Returns a random Country that is unoccupied.
    //countriesOccupied-The number of countries that are occupied
    //If countriesOccupied is not an accurate number it is possible, but not guaranteed, that a Country with a blank
    //name will be returned
    public Country randomUnoccupiedCountry(int countriesOccupied) {
        int randomCountry =  (int) (Math.random() * (countryNames.keySet().size() - countriesOccupied));
        int countriesCovered = 0;
        for (Country c: countryNames.values()) {
            if (c.getOccupant() == null) {
                if(countriesCovered == randomCountry) {
                    return c;
                } else {
                    countriesCovered++;
                }
            }
        }
        return new Country("");
    }

    //Returns the countries currently occupied by this player
    public Set<Country> countriesOccupied(Player player) {
        Set<Country> playerCountries = new HashSet<Country>();
        for(Country c: countryNames.values()) {
            if(c.getOccupant() == player) {
                playerCountries.add(c);
            }
        }
        return playerCountries;
    }

    public int numberCountries() {
        return countryNames.keySet().size();
    }
    /**
     * Finds all the countries that are connected for the given country. Connected means that the occupant can
     * trace a path to these countries without having to enter enemy territory
     * @param countryName The name of the country we're looking for connected countries
     * @return The set of countryNames that are connected
     */
    public Set<String> connectedCountries(String countryName) {
        Country country = getCountry(countryName);
        Set<Country> connectedCountries = new HashSet<Country>();
        Set<Country> countriesCovered = new HashSet<Country>();
        //Add this country in temporarily to find all the connected countries
        connectedCountries.add(country);
        connectedCountries(country, connectedCountries, countriesCovered);
        Set<String> countryNames = new HashSet<>();
        for(Country c: connectedCountries) {
            countryNames.add(c.getName());
        }
        //We need to remove the country itself now!
        countryNames.remove(countryName);
        return countryNames;
    }

    /**
     * Private recursive method that returns all the connected countries for the given country. For definition of
     * what connected means refer to the non-recursive method with less parameters with the same name
     * @param country The country that we're looking for connected countries for
     * @param connectedCountries The countries that are connected together
     * @param countriesCovered The countries that have been covered on the board.
     */
    private void connectedCountries(Country country, Set<Country> connectedCountries,
                                    Set<Country> countriesCovered) {
        //Immediately add the country to covered so that we don't keep hitting it in recursion
        countriesCovered.add(country);
        for(Country borderCountry: country.getBorderingCountries()) {
            if(country.getOccupant() == borderCountry.getOccupant()) {
                //If we haven't covered this country we need to add and call the recursive method to find other countries
                if (!(countriesCovered.contains(borderCountry))) {
                    connectedCountries.add(borderCountry);
                    connectedCountries(borderCountry, connectedCountries, countriesCovered);
                }
                //Add the countries where the occupant is not correct to covered
            } else {
                countriesCovered.add(borderCountry);
            }
        }
    }
    /**
     * Returns the number of troops a player gets from continent bonuses
     * @param player The player that you're returning troops from continents for
     * @return The number of troops the player will get from controlling entire continent
     */
    public int troopsFromContinents(Player player) {
        int troops = 0;
        for(Continent cont: continents) {
            boolean controlsContinent = true;
            for(Country country: cont.getCountries()) {
                if(country.getOccupant() != player) {
                    controlsContinent = false;
                }
            }
            if(controlsContinent) {
                troops += cont.getTroopNumber();
            }
        }
        return troops;
    }

    public Country getCountry(String country) {
        return countryNames.get(country);
    }
}
