package com.example.rachel.pokedex.database;

import android.database.sqlite.SQLiteOpenHelper;

import com.example.rachel.pokedex.model.Pokemon;
import com.example.rachel.pokedex.model.Stats;
import com.example.rachel.pokedex.model.SubPokemon;

import java.util.List;

/**
 * Created by student on 18/09/2016.
 */

//The access class is just to make it easier to access the database contract methods
public class PokemonAccess {

    private final PokemonContract pokemonContract;

    public PokemonAccess(SQLiteOpenHelper sqLiteOpenHelper) {
        this.pokemonContract = new PokemonContract(sqLiteOpenHelper);
    }

    //Example of overriding interface
    public List<Pokemon> getAll() {
        return pokemonContract.getAllPokemon();
    }

    public SubPokemon getDetailPokemon(int id) {
        return pokemonContract.getDetailPokemon(id);
    }

    //Example standard contract methods
    public void insertPokemon(Pokemon pokemon){
        pokemonContract.insert(pokemon);
    }

    public void insertSubPokemon(SubPokemon subPokemon){
        pokemonContract.insertSubpokemon(subPokemon);
    }

    public void insertStats(Stats stats){
        pokemonContract.insertStat(stats);
    }

    public void deletePokemon(int id){
        pokemonContract.delete(id);
    }

    //Example of extra methods
    public void insertPokemons(List<Pokemon> pokemons){
        for(Pokemon pokemon : pokemons){
            pokemonContract.insert(pokemon);
        }
    }

    public void deletePokemons(int... ids){
        for(int id : ids){
            pokemonContract.delete(id);
        }
    }

    public boolean hasPokemon(int id){
        return pokemonContract.getPokemon(id) != null;
    }
}
