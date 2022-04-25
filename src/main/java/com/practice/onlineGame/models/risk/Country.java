package com.practice.onlineGame.models.risk;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
@Entity
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    protected Long id;
    private String name; //current name
    private int troops; //current level of troops

    public int getTroops() {
        return troops;
    }

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "bordering_countries",
            joinColumns = {@JoinColumn(name = "country_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "bordering_country_id", referencedColumnName = "id")})
    private Set<Country> borderingCountries;

    @JsonIgnoreProperties({"cards", "color", "out"})
    @OneToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name="player_id")
    private Player occupant;

    public Country() {
    }

    //Create Country give name. Sets troops to 0 and player to null
    public Country(String name) {
        this(name, 0, null);
    }

    //Create country given a name, troops, and occupant
    public Country(String name, int troops, Player occupant) {
        this.name = name;
        this.troops = troops;
        borderingCountries = new HashSet<>();
        this.occupant = occupant;
    }

    public void addBorderingCountry(Country c) {
        borderingCountries.add(c);
    }
    // Return the current troop number in the country
    public int getTroopCount() {
        return troops;
    }

    // Return the current occupant of the country
    public Player getOccupant() {
        return occupant;
    }

    // Return the current name of the country
    public String getName() {
        return name;
    }

    //Reduce the number of troops by reduceTroopNumber
    public void reduceTroops(int reduceTroopNumber) {
        this.troops = this.troops - reduceTroopNumber;
    }

    // Increase the troops by IncreaseTroopNumber
    public void increaseTroops(int increaseTroopNumber) {
        this.troops = this.troops + increaseTroopNumber;
    }

    // change current occupant and troops to occupant and troops
    public void changeOccupant(Player occupant, int troops) {
        this.occupant = occupant;
        this.troops = troops;
    }

    public boolean borders(Country c) {
        return borderingCountries.contains(c);
    }

    @JsonIgnore
    public Set<Country> getBorderingCountries() {
        return borderingCountries;
    }
    @Override
    public String toString() {
        return name;
    }
}
