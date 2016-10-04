package com.example.rachel.pokedex.model;

import android.graphics.Bitmap;

/**
 * Created by student on 13/09/2016.
 */
public class Pokemon {

    private Bitmap icon;
    private int id;
    private String name;
    private String url;


    public Pokemon(int id, Bitmap icon, String name, String url) {
        this.id = id;
        this.icon = icon;
        this.name = name;
        this.url = url;
    }

    public Pokemon(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
