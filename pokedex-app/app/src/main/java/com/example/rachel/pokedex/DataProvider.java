package com.example.rachel.pokedex;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.rachel.pokedex.database.PokemonDBHelper;
import com.example.rachel.pokedex.model.Pokemon;
import com.example.rachel.pokedex.model.Stats;
import com.example.rachel.pokedex.model.SubPokemon;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by student on 18/09/2016.
 */
//Async task for API call and set into Array List
public class DataProvider {

    private Context context;

    public DataProvider(Context context) {
        this.context = context;
    }

    //This call is made by the Load Screen activity to initially get all 151 Pokemon to display in the list
    //It calls the Pokemon Form api using a JSON Object Request, provided by Volley
    //Once the id, name, sprite url and pokemon url are received
    //This will call the get sprite method, passing all that data along
    public void getPokemon(int id, final DataCallback<Pokemon> callback) {

        String url = "http://pokeapi.co/api/v2/pokemon-form/" + id;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("DEBUG", "Response received");
                    int id = response.getInt("id");
                    String name = WordUtils.capitalize(response.getString("name"));
                    JSONObject sprites = response.getJSONObject("sprites");
                    String imgUrl = sprites.getString("front_default");
                    JSONObject pokemonType = response.getJSONObject("pokemon");
                    String pokemonUrl = pokemonType.getString("url");

                    getSprite(imgUrl, id, name, pokemonUrl, callback);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DEBUG", "There is an error");
            }
        });
        MySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    //The Get Sprite method will make an Image Request using Volley to get the Sprite based on the sprite url passed through
    //Once this is received, a new Pokemon instance is made and passed through the callback to the On Success method
    //This On Success exists in the Get All Pokemon Method
    public void getSprite(String imgUrl, final int id, final String name, final String url, final DataCallback<Pokemon> callback) {
        ImageRequest imageRequest = new ImageRequest(imgUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap icon) {
                        Pokemon pokemon = new Pokemon(id, icon, name, url);
                        callback.onSuccess(pokemon);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Image Error", "Response: " + error);
                    }
                });

        MySingleton.getInstance(context).addToRequestQueue(imageRequest);
    }

    //This gets ALL pokemon and calls back to the Load Screen activity
    public void getAllPokemon(final DataCallback<List<Pokemon>> mainCallback) {
        final List<Pokemon> allPokemon = new ArrayList<Pokemon>();
        DataCallback<Pokemon> callback = new DataCallback<Pokemon>() {

            @Override
            public void onSuccess(Pokemon result) {
                allPokemon.add(result);
                mainCallback.onSuccess(allPokemon);
            }

            @Override
            public void onFailure(String fail) {
                Log.e("Error on All Pokemon", fail);
            }
        };

        //Here, each Pokemon is called and retrieved individually for all 151
        //Once they are successfully retrieved, they will callback to the onSuccess within this method, which calls back to the onSuccess in the Load Screen
        for (int i = 1; i < 152; i++) {
            getPokemon(i, callback);
        }
    }

    //This gets the more detailed data for a specific pokemon, using the Pokemon url + pokemon id
    //This is the last method called before passing back to the Pokemon Detail onSuccess method
    //It retrieves weight, height, types, stats, moves
    public void getPokemonDetail(final int id, final String description, final List<String> evolutions, final DataCallback<SubPokemon> detailCallback) {
        String url = "http://pokeapi.co/api/v2/pokemon/" + id;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("DEBUG", "Response received");

                    int weight = response.getInt("weight");
                    int height = response.getInt("height");

                    List<String> pType = new ArrayList<>();
                    JSONArray types = response.getJSONArray("types");
                    for (int i = 0; i < types.length(); i++) {
                        JSONObject slot = types.getJSONObject(i);
                        JSONObject type = slot.getJSONObject("type");
                        String typeName = WordUtils.capitalize(type.getString("name"));
                        pType.add(typeName);
                    }

                    List<Stats> pStats = new ArrayList<>();
                    JSONArray stats = response.getJSONArray("stats");
                    for (int i = stats.length() - 1; i >= 0; i--) {
                        JSONObject stat = stats.getJSONObject(i);
                        JSONObject statType = stat.getJSONObject("stat");
                        String statName = WordUtils.capitalize(statType.getString("name"));
                        statName = statName.replace("-", " ");
                        int statNo = stat.getInt("base_stat");

                        Stats statistics = new Stats(id, statName, statNo);
                        pStats.add(statistics);
                    }

                    List<String> pMoves = new ArrayList<>();
                    JSONArray moves = response.getJSONArray("moves");
                    for (int i = 0; i < moves.length(); i++) {
                        JSONObject move = moves.getJSONObject(i);
                        JSONObject moveType = move.getJSONObject("move");
                        String moveName = moveType.getString("name");
                        moveName = moveName.replace("-", " ");
                        pMoves.add(moveName);
                    }

                    SubPokemon detailPokemon = new SubPokemon(id, description, weight, height, pType, pStats, pMoves, evolutions); //add evolutions here
                    detailCallback.onSuccess(detailPokemon);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DEBUG", "There is an error");
                error.printStackTrace();
            }
        });
        MySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    //This is the first method called when a Sub Pokemon is being collected from the API
    //It uses the ID passed through to search the Pokemon Species url and then obtain the flavour text and also the evolution url
    //Then it will pass this along to the getEvolutions method
    public void getDescription(final int id, final DataCallback<SubPokemon> detailCallback) {
        String url = "http://pokeapi.co/api/v2/pokemon-species/" + id;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("DEBUG", "Response received");

                    JSONArray descriptionArray = response.getJSONArray("flavor_text_entries");
                    JSONObject descriptionBlock = descriptionArray.getJSONObject(1);
                    String description = descriptionBlock.getString("flavor_text");
                    description = description.replace("\n", " ");
                    JSONObject evolutionChain = response.getJSONObject("evolution_chain");
                    String evolutionUrl = evolutionChain.getString("url");

                    getEvolutions(evolutionUrl, id, description, detailCallback);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DEBUG", "There is an error");
                error.printStackTrace();
            }
        });
        MySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    //This method gets the Evolution chain, then passes everything it has along with the evolutions to the getPokemonDetail method for the final
    //bit of info
    public void getEvolutions(final String url, final int id, final String description, final DataCallback<SubPokemon> detailCallback) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("DEBUG", "Response received");

                    List<String> evolutionChain = new ArrayList<>();

                    JSONObject chain = response.getJSONObject("chain");

                    JSONObject species = chain.getJSONObject("species");
                    String name = WordUtils.capitalize(species.getString("name"));
                    evolutionChain.add(name);

                    if (chain.has("evolves_to")) {
                        JSONArray evolvesTo = chain.getJSONArray("evolves_to");
                        if (evolvesTo != null && evolvesTo.length() > 0) {
                            JSONObject evolutionObject = evolvesTo.getJSONObject(0);
                            JSONObject species1 = evolutionObject.getJSONObject("species");
                            String name2 = WordUtils.capitalize(species1.getString("name")); //shows ivysaur
                            evolutionChain.add(name2);

                            if (evolutionObject.has("evolves_to")) {
                                JSONArray evolvesTo2 = evolutionObject.getJSONArray("evolves_to");
                                if (evolvesTo2 != null && evolvesTo2.length() > 0) {
                                    JSONObject evolutionObject2 = evolvesTo2.getJSONObject(0);
                                    JSONObject species2 = evolutionObject2.getJSONObject("species");
                                    String name3 = WordUtils.capitalize(species2.getString("name"));
                                    evolutionChain.add(name3);
                                }
                            }
                        }
                    }

                    getPokemonDetail(id, description, evolutionChain, detailCallback);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DEBUG", "There is an error");
                error.printStackTrace();
            }
        });
        MySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }

}