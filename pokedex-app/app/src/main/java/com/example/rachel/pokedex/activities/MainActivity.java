package com.example.rachel.pokedex.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.rachel.pokedex.PokemonAdapter;
import com.example.rachel.pokedex.R;
import com.example.rachel.pokedex.database.PokemonAccess;
import com.example.rachel.pokedex.database.PokemonDBHelper;
import com.example.rachel.pokedex.model.Pokemon;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Pokemon array list is to store the arraylist that comes back from the Database, to display.
    //Recycler view and adapter displays items in the recycler view list
    private List<Pokemon> pokemonArrayList = new ArrayList<Pokemon>();
    private RecyclerView recyclerView;
    private PokemonDBHelper dbHelper;
    private PokemonAdapter pAdapter;
    private EditText search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        search = (EditText) findViewById(R.id.search);

        //On create, the database helper will help to retrieve all the pokemon
        dbHelper = new PokemonDBHelper(getBaseContext());
        PokemonAccess access = new PokemonAccess(dbHelper);
        pokemonArrayList = access.getAll();

        //Then they are displayed in the recycler view via an adapter
        recyclerView = (RecyclerView) findViewById(R.id.list);
        pAdapter = new PokemonAdapter(pokemonArrayList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(pAdapter);

        //A textlistener is added on the search field, so when a user types, a new, refined list will dynamically appear
        addTextListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //https://www.studytutorial.in/android-search-to-recyclerview-tutorial
    //Within the text listener, there are 3 methods to be overriden
    //before text changed and after text changed is untouched
    //In the on text changed method, the query from the search field is input and changed to a lower case string
    //A new array list is created to display filtered results
    //Then we run a loop through the entire original array list, to check if the pokemon name matches the letters in the query
    //If they do, then we add it to the new filtered list and return that list
    //Everytime this process is done, we notify the adapter that the data set has changed, so it updates
    public void addTextListener() {
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence query, int start, int before, int count) {
                query = query.toString().toLowerCase();

                final List<Pokemon> filteredList = new ArrayList<>();

                for (int i = 0; i < pokemonArrayList.size(); i++) {
                    final String text = pokemonArrayList.get(i).getName().toLowerCase();
                    if (text.contains(query)) {
                        filteredList.add(pokemonArrayList.get(i));
                    }
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                pAdapter = new PokemonAdapter(filteredList, MainActivity.this);
                recyclerView.setAdapter(pAdapter);
                pAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}