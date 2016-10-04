package com.example.rachel.pokedex.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;

import com.example.rachel.pokedex.model.Pokemon;
import com.example.rachel.pokedex.model.Stats;
import com.example.rachel.pokedex.model.SubPokemon;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by student on 18/09/2016.
 */
//Cite this
public final class PokemonContract {

    public static final String TABLE_NAME = "pokemon";
    public static final String SUB_TABLE_NAME = "subpokemon";
    public static final String STATS_TABLE_NAME = "stats";
    private final SQLiteOpenHelper dbHelper;
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ",";

    //A table is created for pokemon entries, subpokemon and statistics
    public static final String CREATE_POKEMON_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    PokemonEntry._ID + " INTEGER PRIMARY KEY," +
                    PokemonEntry.COLUMN_NAME_ID + INT_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_IMG + BLOB_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_URL + TEXT_TYPE + ")";

    public static final String CREATE_SUB_TABLE =
            "CREATE TABLE " + SUB_TABLE_NAME + " (" +
                    PokemonEntry._ID + " INTEGER PRIMARY KEY," +
                    PokemonEntry.COLUMN_NAME_ID + INT_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_WEIGHT + INT_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_HEIGHT + INT_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_TYPES + TEXT_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_MOVES + TEXT_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_EVOLUTIONS + TEXT_TYPE + ")";

    public static final String CREATE_STATS_TABLE =
            "CREATE TABLE " + STATS_TABLE_NAME + " (" +
                    PokemonEntry._ID + " INTEGER PRIMARY KEY," +
                    PokemonEntry.COLUMN_NAME_ID + INT_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_STATNAME + TEXT_TYPE + COMMA_SEP +
                    PokemonEntry.COLUMN_NAME_STATNO + INT_TYPE + ")";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String SUB_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SUB_TABLE_NAME;

    public static final String STATS_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + STATS_TABLE_NAME;

    //These column names are defined
    public abstract class PokemonEntry implements BaseColumns {
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_IMG = "icon";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_URL = "url";

        public static final String COLUMN_NAME_WEIGHT = "weight";
        public static final String COLUMN_NAME_HEIGHT = "height";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_TYPES = "types";
        public static final String COLUMN_NAME_MOVES = "moves";
        public static final String COLUMN_NAME_EVOLUTIONS = "evolutions";

        public static final String COLUMN_NAME_STATNAME = "statname";
        public static final String COLUMN_NAME_STATNO = "statno";
    }

    public PokemonContract(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    //Before doing anything, we always get an instance of the dbHelper to get a writable or readable database
    //In insert, we just insert our Pokemon into their respective columns
    public long insert(Pokemon pokemon) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PokemonEntry.COLUMN_NAME_ID, pokemon.getId());
        values.put(PokemonEntry.COLUMN_NAME_NAME, pokemon.getName());

        //The bitmap must be stored as a byte array
        //When it is returned, it is transformed back into a bitmap
        byte[] icon = getBitmapAsByteArray(pokemon.getIcon());
        values.put(PokemonEntry.COLUMN_NAME_IMG, icon);
        values.put(PokemonEntry.COLUMN_NAME_URL, pokemon.getUrl());

        long newRowId;
        newRowId = db.insert(TABLE_NAME, null, values);
        db.close();

        return newRowId;
    }

    public long insertSubpokemon(SubPokemon subPokemon) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PokemonEntry.COLUMN_NAME_ID, subPokemon.getId());
        values.put(PokemonEntry.COLUMN_NAME_WEIGHT, subPokemon.getWeight());
        values.put(PokemonEntry.COLUMN_NAME_HEIGHT, subPokemon.getHeight());
        values.put(PokemonEntry.COLUMN_NAME_DESCRIPTION, subPokemon.getDescription());
        values.put(PokemonEntry.COLUMN_NAME_TYPES, convertArrayToString(subPokemon.getTypes()));
        values.put(PokemonEntry.COLUMN_NAME_MOVES, convertArrayToString(subPokemon.getMoves()));
        values.put(PokemonEntry.COLUMN_NAME_EVOLUTIONS, convertArrayToString(subPokemon.getEvolution()));

        long newRowId;
        newRowId = db.insert(SUB_TABLE_NAME, null, values);
        db.close();

        return newRowId;
    }

    //We insert stats separately and use the Pokemon ID to identify them, because they are objects that have multiple attributes
    public long insertStat(Stats stats) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PokemonEntry.COLUMN_NAME_ID, stats.getPokemonId());
        values.put(PokemonEntry.COLUMN_NAME_STATNAME, stats.getStatName());
        values.put(PokemonEntry.COLUMN_NAME_STATNO, stats.getStatNo());

        long newRowId;
        newRowId = db.insert(STATS_TABLE_NAME, null, values);
        db.close();

        return newRowId;
    }

    //To get all the pokemon, we submit a db query and append the results to an array list
    public List<Pokemon> getAllPokemon() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //Columns to query
        String[] columns = {
                PokemonEntry._ID,
                PokemonEntry.COLUMN_NAME_ID,
                PokemonEntry.COLUMN_NAME_IMG,
                PokemonEntry.COLUMN_NAME_NAME,
                PokemonEntry.COLUMN_NAME_URL
        };

        //Sort order
        String sortOrder = PokemonEntry.COLUMN_NAME_ID;

        Cursor cur = db.query(
                TABLE_NAME,  // The table to query
                columns,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        List<Pokemon> pokemons = new ArrayList<>();

        //At each row, the cursor gets the Pokemon's id, name, image and url and then creates a new pokemon and adds it to our temp array list
        while (cur.moveToNext()) {
            int id = cur.getInt(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_ID));
            byte[] img = cur.getBlob(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_IMG));
            Bitmap icon = BitmapFactory.decodeByteArray(img, 0, img.length);
            String name = cur.getString(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_NAME));
            String url = cur.getString(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_URL));

            Pokemon pokemon = new Pokemon(id, icon, name, url);
            pokemons.add(pokemon);
        }

        cur.close();
        db.close();
        return pokemons;
    }

    public void delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //WHERE ID = ?
        String selection = PokemonEntry.COLUMN_NAME_ID + " = '?'";

        //All the different IDs its equal to
        String[] selectionArgs = {String.valueOf(id)};

        db.delete(TABLE_NAME, selection, selectionArgs);

        db.close();
    }


    public Pokemon getPokemon(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = PokemonEntry.COLUMN_NAME_ID + " = '?'";

        String[] selectionArgs = {String.valueOf(id)};

        //Columns to query
        String[] columns = {
                PokemonEntry._ID,
                PokemonEntry.COLUMN_NAME_ID,
                PokemonEntry.COLUMN_NAME_IMG,
                PokemonEntry.COLUMN_NAME_NAME,
                PokemonEntry.COLUMN_NAME_URL
        };

        Cursor cur = db.query(
                TABLE_NAME,  // The table to query
                columns,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        Pokemon pokemon = null;
        if (cur.moveToNext()) {
            int pokemonId = cur.getInt(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_ID));
            byte[] img = cur.getBlob(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_IMG));
            Bitmap icon = BitmapFactory.decodeByteArray(img, 0, img.length);
            String name = cur.getString(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_NAME));
            String url = cur.getString(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_URL));

            pokemon = new Pokemon(pokemonId, icon, name, url);
        }
        cur.close();
        db.close();
        return pokemon;
    }

    public SubPokemon getDetailPokemon(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = PokemonEntry.COLUMN_NAME_ID + " =?";

        String[] selectionArgs = { String.valueOf(id)};

        //Columns to query
        String[] columns = {
                PokemonEntry._ID,
                PokemonEntry.COLUMN_NAME_ID,
                PokemonEntry.COLUMN_NAME_WEIGHT,
                PokemonEntry.COLUMN_NAME_HEIGHT,
                PokemonEntry.COLUMN_NAME_DESCRIPTION,
                PokemonEntry.COLUMN_NAME_TYPES,
                PokemonEntry.COLUMN_NAME_MOVES,
                PokemonEntry.COLUMN_NAME_EVOLUTIONS
        };

        Cursor cur = db.query(
                SUB_TABLE_NAME,  // The table to query
                columns,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        SubPokemon subPokemon = null;
        if (cur.moveToNext()) {
            int pokemonId = cur.getInt(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_ID));
            int weight = cur.getInt(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_WEIGHT));
            int height = cur.getInt(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_HEIGHT));
            String description = cur.getString(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_DESCRIPTION));
            String stringTypes = cur.getString(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_TYPES));
            List<String> types = convertStringToArray(stringTypes);
            String stringMoves = cur.getString(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_MOVES));
            List<String> moves = convertStringToArray(stringMoves);
            String stringEvolutions = cur.getString(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_EVOLUTIONS));
            List<String> evolutions = convertStringToArray(stringEvolutions);

            //For the SubPokemon, we get their stats from the stats table using the Pokemon Id
            List<Stats> stats = getStats(pokemonId);

            subPokemon = new SubPokemon(pokemonId, description, weight, height, types, stats, moves, evolutions);
        }
        cur.close();
        db.close();
        return subPokemon;
    }

    //To get the relevant stats, we need the specific Pokemon ID
    public List<Stats> getStats(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = PokemonEntry.COLUMN_NAME_ID + " =?";

        String[] selectionArgs = {String.valueOf(id)};

        //Columns to query
        String[] columns = {
                PokemonEntry._ID,
                PokemonEntry.COLUMN_NAME_ID,
                PokemonEntry.COLUMN_NAME_STATNAME,
                PokemonEntry.COLUMN_NAME_STATNO
        };

        Cursor cur = db.query(
                STATS_TABLE_NAME,  // The table to query
                columns,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        List<Stats> stats = new ArrayList<>();

        while (cur.moveToNext()) {
            int pokemonId = cur.getInt(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_ID));
            String statName = cur.getString(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_STATNAME));
            int statNo = cur.getInt(cur.getColumnIndexOrThrow(PokemonEntry.COLUMN_NAME_STATNO));

            Stats stat = new Stats(pokemonId, statName, statNo);
            stats.add(stat);
        }

        cur.close();
        db.close();
        return stats;
    }

    //This will transform a Bitmap to a Byte Array so it can be put into the database
    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    //Converts a String array to string to store it
    public static String convertArrayToString(List<String> array) {
        String stringArray = "";
        for(String item : array) {
            stringArray += item;

            if(array.indexOf(item) != array.size()-1) {
                stringArray += ", ";
            }
        }
        return stringArray;
    }

    //Converts the string back to a String Array
    public static List<String> convertStringToArray(String string) {
        List<String> array = Arrays.asList(string.split("\\s*,\\s*"));
        return array;
    }

}

