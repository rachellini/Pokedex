package com.example.rachel.pokedex.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by student on 27/09/2016.
 */
public class SubPokemon extends Pokemon {

    private int weight;
    private int height;
    private String description;
    private List<String> types;
    private List<Stats> stats;
    private List<String> moves;
    private List<String> evolution;

    public SubPokemon(int id, String description, int weight, int height, List<String> types, List<Stats> stats, List<String> moves, List<String> evolution) {
        super(id);
        this.description = description;
        this.weight = weight;
        this.height = height;
        this.types = types;
        this.stats = stats;
        this.moves = moves;
        this.evolution = evolution;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<Stats> getStats() {
        return stats;
    }

    public void setStats(List<Stats> stats) {
        this.stats = stats;
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }


    public List<String> getEvolution() {
        return evolution;
    }

    public void setEvolution(List<String> evolution) {
        this.evolution = evolution;
    }

}
