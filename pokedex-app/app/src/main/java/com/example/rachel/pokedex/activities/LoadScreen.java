package com.example.rachel.pokedex.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.example.rachel.pokedex.DataCallback;
import com.example.rachel.pokedex.DataProvider;
import com.example.rachel.pokedex.R;
import com.example.rachel.pokedex.database.PokemonAccess;
import com.example.rachel.pokedex.database.PokemonDBHelper;
import com.example.rachel.pokedex.model.Pokemon;

import java.util.List;

//The Load Screen class makes the initial call to the PokeApi to collect the ID, Name, Sprite and URL for all 151 Pokemon. During this, the user
//is displayed a Load Screen, with a Progress Bar. Once the call is successfully completed, the Main Screen will automatically be displayed

public class LoadScreen extends AppCompatActivity implements DataCallback<List<Pokemon>> {

    private PokemonDBHelper dbHelper;
    private ProgressBar progressBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_screen);

        //Once Load Screen is shown, a Progress bar is immediately loaded, so that the user knows that something is happening
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        //The DBHelper is instantiated so we can get data from the SQLite database
        //Also, the Data Provider is instantiated, so in case there is no existing data in the db, then we will call it from the API
        dbHelper = new PokemonDBHelper(getBaseContext());
        DataProvider dataProvider = new DataProvider(this.getBaseContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM pokemon", null);

        //This checks if there is existing data in the database. If not, then make an API call via the Data Provider. If there isn, then just
        //show the Main Activity
        if (cursor != null) {
            if (!cursor.moveToFirst()) {
                dataProvider.getAllPokemon(this);
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        } else {

        }
    }

    //This is the two overriden Data Callback methods, so the program knows that the API calls were successful. Once it is CONFIRMED
    //that there has been a response, which is the Pokemon List containing ALL 151 pokemon, then we can insert them into the database
    //After inserting them, we show the Main Activity
    @Override
    public void onSuccess(List<Pokemon> result) {

        if (result.size() == 151) {
            PokemonAccess access = new PokemonAccess(dbHelper);
            access.insertPokemons(result);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
        }
    }

    @Override
    public void onFailure(String fail) {

    }
}
