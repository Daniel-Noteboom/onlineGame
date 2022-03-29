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

    private Country getCountry(String country) {
        return countryNames.get(country);
    }
}
