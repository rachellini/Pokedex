package com.example.rachel.pokedex;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rachel.pokedex.activities.PokemonDetail;
import com.example.rachel.pokedex.model.Pokemon;

import java.util.List;

/**
 * Created by student on 13/09/2016.
 */

//The Pokemon adapter allows views to be displayed in the recycler view
public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.ViewHolder> {

    private List<Pokemon> pokemonList;
    public Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView id, name;
        public ImageView icon;

        public ViewHolder(View view) {
            super(view);
            id = (TextView) view.findViewById(R.id.id);
            icon = (ImageView) view.findViewById(R.id.icon);
            name = (TextView) view.findViewById(R.id.name);
            view.setOnClickListener(this);
        }

        //On Click of each viewholder, the item will move to the Pokemon Detail activity, whilst passing along the id and name of that specific pokemon
        //in the view holder
        @Override
        public void onClick(View view) {
            Context context = itemView.getContext();
            Intent intent = new Intent(context, PokemonDetail.class);
            Bundle extras = new Bundle();
            extras.putString("ID", id.getText().toString());
            extras.putString("NAME", name.getText().toString());
            intent.putExtras(extras);
            context.startActivity(intent);
        }
    }

    public PokemonAdapter(List<Pokemon> pokemonList, Context context) {
        this.pokemonList = pokemonList;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pokemonlistitem, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pokemon pokemon = pokemonList.get(position);
        holder.id.setText(String.valueOf(pokemon.getId()));
        holder.icon.setImageBitmap(pokemon.getIcon());
        holder.name.setText(pokemon.getName());
    }

    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

}