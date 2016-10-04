package com.example.rachel.pokedex;

/**
 * Created by student on 18/09/2016.
 */

//The datacallback class is an interface that takes in any type of object
    //When a response is returned, it will call onSuccess - allowing us to continue with whatever we can do only after a response exists
public interface DataCallback<T> {

    void onSuccess(T result);

    void onFailure(String fail);

}
