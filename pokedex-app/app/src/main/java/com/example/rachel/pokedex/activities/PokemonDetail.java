package com.example.rachel.pokedex.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rachel.pokedex.DataCallback;
import com.example.rachel.pokedex.DataProvider;
import com.example.rachel.pokedex.R;
import com.example.rachel.pokedex.database.PokemonAccess;
import com.example.rachel.pokedex.database.PokemonDBHelper;
import com.example.rachel.pokedex.model.Stats;
import com.example.rachel.pokedex.model.SubPokemon;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

//This class also implements the Data Callback because it makes an API call to the specific pokemon, the first time a user wants info on that pokemon
public class PokemonDetail extends AppCompatActivity implements DataCallback<SubPokemon> {

    private PokemonDBHelper dbHelper;
    private ProgressBar progressBar;

    private int id;
    private String name;
    private String description;

    private TextView pId;
    private TextView pName;
    private TextView height;
    private TextView weight;
    private ImageView image;

    private TextView hpStat;
    private TextView attackStat;
    private TextView defenseStat;
    private TextView specialAttackStat;
    private TextView specialDefenseStat;
    private TextView speedStat;
    private TextView totalStats;

    private ProgressBar hpBar;
    private ProgressBar attackBar;
    private ProgressBar defenseBar;
    private ProgressBar specialAttackBar;
    private ProgressBar specialDefenseBar;
    private ProgressBar speedBar;

    private TextView type;
    private TextView moves;
    private TextView evolution;

    TextToSpeech textService;
    ImageButton sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //This sets the back button up in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //All the Views on this screen are connected to their corresponding XML
        pId = (TextView) findViewById(R.id.id);
        pName = (TextView) findViewById(R.id.name);
        weight = (TextView) findViewById(R.id.weight);
        height = (TextView) findViewById(R.id.height);

        hpStat = (TextView) findViewById(R.id.hpStat);
        attackStat = (TextView) findViewById(R.id.attackStat);
        defenseStat = (TextView) findViewById(R.id.defenseStat);
        specialAttackStat = (TextView) findViewById(R.id.specialAttackStat);
        specialDefenseStat = (TextView) findViewById(R.id.specialDefenseStat);
        speedStat = (TextView) findViewById(R.id.speedStat);
        totalStats = (TextView) findViewById(R.id.totalStats);

        hpBar = (ProgressBar) findViewById(R.id.hpBar);
        attackBar = (ProgressBar) findViewById(R.id.attackBar);
        defenseBar = (ProgressBar) findViewById(R.id.defenseBar);
        specialAttackBar = (ProgressBar) findViewById(R.id.specialAttackBar);
        specialDefenseBar = (ProgressBar) findViewById(R.id.specialDefenseBar);
        speedBar = (ProgressBar) findViewById(R.id.speedBar);

        type = (TextView) findViewById(R.id.type);
        moves = (TextView) findViewById(R.id.moves);
        evolution = (TextView) findViewById(R.id.evolution);

        //The image button for sound is connected here
        //This uses the TextToSpeech tool to get the Pokemon description for that Pokemon and read it out on click on the button
        //Just like a Pokedex does
        sound = (ImageButton) findViewById(R.id.sound);
        textService = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textService.setLanguage(Locale.ENGLISH);
                }
            }
        });

        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toSpeak = description;
                textService.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        //The progress bar is set to show Loading, whilst the program makes the API callto collect the response for a specific pokemon
        progressBar = (ProgressBar) findViewById(R.id.detailProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        //The pokemon item row in the Main Activity passes the ID to this screen on click
        // This method collects that information that was passed through along with the intent
        //It will do a log check to discover what the ID is
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String stringId = extras.getString("ID");
            id = Integer.parseInt(stringId);
            name = extras.getString("NAME");
            Log.d("ID CHECK", "ID IS: " + id);
        }

        dbHelper = new PokemonDBHelper(getBaseContext());
        DataProvider dataProvider = new DataProvider(this.getBaseContext());

        //Next, to check if the SubPokemon has already had it's information stored in the database
        //we call on the dbhelper to submit a select query for that particular id
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM subpokemon WHERE id = " + id + ";", null);

        //if no information was returned, then we call the Data Provider to get the data from the API
        //Otherwise, we call the Async task to set up the Pokemon details
        if (cursor != null) {
            if (!cursor.moveToFirst()) {
                dataProvider.getDescription(id, this);
            } else {
                new SetSubPokemon().execute(id);
            }
        } else {
        }

        //This image view calls on the async task to place the Pokemon image on the screen
        image = (ImageView) findViewById(R.id.photo);
        new InternetImageTask().execute(id);
    }

    @Override
    public void onSuccess(SubPokemon pokemon) {

        //On success of collecting the Pokemon's details, we will insert this info into the SQLite database
        //Additionally, we insert the Pokemon's stats into a separate database
        PokemonAccess access = new PokemonAccess(dbHelper);
        access.insertSubPokemon(pokemon);
        List<Stats> pStats = pokemon.getStats();
        for (Stats stat : pStats) {
            access.insertStats(stat);
        }


        //And THEN we set up the Pokemon's information on the screen
        new SetSubPokemon().execute(id);
    }

    @Override
    public void onFailure(String fail) {

    }

    //The Image Task gets the image from the Internet, based on its id, then uses an Input Stream to set it into the image view
    private class InternetImageTask extends AsyncTask<Integer, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Integer... integers) {
            int pId = integers[0];
            String url = "";

            if (pId < 10) {
                url = "http://assets.pokemon.com/assets/cms2/img/pokedex/full/00" + id + ".png";
            } else if (pId >= 10 && pId < 100) {
                url = "http://assets.pokemon.com/assets/cms2/img/pokedex/full/0" + id + ".png";
            } else {
                url = "http://assets.pokemon.com/assets/cms2/img/pokedex/full/" + id + ".png";
            }
            Bitmap pokemonImage = null;

            try {
                InputStream in = new java.net.URL(url).openStream();
                pokemonImage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return pokemonImage;
        }

        //On Post Execute, it will set the image
        protected void onPostExecute(Bitmap result) {
            image.setImageBitmap(result);
        }
    }

    //This class sets all the data onto the screen
    private class SetSubPokemon extends AsyncTask<Integer, Void, SubPokemon> {

        //In the background, we access the Pokemon from the database using its ID
        @Override
        protected SubPokemon doInBackground(Integer... integers) {
            int pId = integers[0];
            PokemonAccess access = new PokemonAccess(dbHelper);
            SubPokemon subPokemon = access.getDetailPokemon(pId);
            return subPokemon;
        }

        //Then on Post Execute, we set all the data into the fields using the "SubPokemon" we received from do in background
        @Override
        protected void onPostExecute(SubPokemon subPokemon) {
            pId.setText(String.valueOf(id));
            pName.setText(name);
            description = subPokemon.getDescription();
            weight.setText(String.valueOf(subPokemon.getWeight()));
            height.setText(String.valueOf(subPokemon.getHeight()));

            String types = "";
            List<String> typeList = subPokemon.getTypes();
            for (int i = 0; i < typeList.size(); i++) {
                if (!(i == typeList.size() - 1)) {
                    types += typeList.get(i) + ", ";
                } else {
                    types += typeList.get(i);
                }
            }
            type.setText(types);

            List<Stats> statList = subPokemon.getStats();

            int hp = statList.get(0).getStatNo();
            hpStat.setText(String.valueOf(hp));
            hpBar.setProgress(hp);
            int attack = statList.get(1).getStatNo();
            attackStat.setText(String.valueOf(attack));
            attackBar.setProgress(attack);
            int defense = statList.get(2).getStatNo();
            defenseStat.setText(String.valueOf(defense));
            defenseBar.setProgress(defense);
            int specialAttack = statList.get(3).getStatNo();
            specialAttackStat.setText(String.valueOf(specialAttack));
            specialAttackBar.setProgress(specialAttack);
            int specialDefense = statList.get(4).getStatNo();
            specialDefenseStat.setText(String.valueOf(specialDefense));
            specialDefenseBar.setProgress(specialDefense);
            int speed = statList.get(5).getStatNo();
            speedStat.setText(String.valueOf(speed));
            speedBar.setProgress(speed);

            int total = hp + attack + defense + specialAttack + specialDefense + speed;
            totalStats.setText(String.valueOf(total));

            String move = "";
            List<String> moveList = subPokemon.getMoves();
            for (int i = 0; i < moveList.size(); i++) {
                if (!(i == moveList.size() - 1)) {
                    move += moveList.get(i) + ", ";
                } else {
                    move += moveList.get(i);
                }
            }
            moves.setText(move);

            List<String> evolutionList = subPokemon.getEvolution();
            String evolutions = "";
            for (int i = 0; i < evolutionList.size(); i++) {
                if (!(i == evolutionList.size() - 1)) {
                    evolutions += evolutionList.get(i) + ", ";
                } else {
                    evolutions += evolutionList.get(i);
                }
            }
            evolution.setText(evolutions);

            //And finally, when it's all in there, we make the progress bar invisible
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}