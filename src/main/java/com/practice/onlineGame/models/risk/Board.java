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

    private Country getOrCreateCountry(String countryName) {
        if(!countryNames.containsKey(countryName)) {
            Country country = new Country(countryName);
            countryNames.put(countryName, country);
        }
        return countryNames.get(countryName);
    }

    public void clearTroops() {
        for(Country c: countryNames.values()) {
            c.changeOccupant(null, 0);
        }
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
    public void increaseTroops(String country, int troops) {
        countryNames.get(country).increaseTroops(troops);
    }

    public int numberCountries() {
        return countryNames.keySet().size();
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

    private Country getCountry(String country) {
        return countryNames.get(country);
    }
}
