package com.example.rachel.pokedex.model;

/**
 * Created by student on 1/10/2016.
 */
public class Stats {

    private int pokemonId;
    private String statName;
    private int statNo;

    public Stats(int pokemonId, String statName, int statNo) {
        this.pokemonId = pokemonId;
        this.statName = statName;
        this.statNo = statNo;
    }

    public int getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }

    public String getStatName() {
        return statName;
    }

    public void setStatName(String statName) {
        this.statName = statName;
    }

    public int getStatNo() {
        return statNo;
    }

    public void setStatNo(int statNo) {
        this.statNo = statNo;
    }
}
