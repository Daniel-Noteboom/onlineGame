package com.practice.onlineGame.models.risk;

import javax.persistence.*;
import java.util.Collections;
import java.util.Set;

@Entity
public class Continent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    protected Long id;
    private int troopNumber;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "continent_countries",
            joinColumns = {@JoinColumn(name = "country_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "continent_id", referencedColumnName = "id")})
    private Set<Country> countries;
    private String name;

    public Continent() {
    }
    public Continent(int troopNumber, Set<Country> countries, String name) {
        this.troopNumber = troopNumber;
        this.countries = Collections.unmodifiableSet(countries);
        this.name = name;
    }

    public Set<Country> getCountries() {
        return countries;
    }
    public int getTroopNumber() {
        return troopNumber;
    }

    //Returns true if the countries passed in contains all of this classes countries
    public boolean containsAll(Set<Country> countries) {
        return countries.containsAll(this.countries);
    }

    public String getName() {
        return name;
    }
}
