package com.innovate.himnario;

import com.innovate.himnario.data.Coro;

import java.util.ArrayList;

/**
 * Created by Joel on 13-Apr-17.
 */

public class Data {

    private static ArrayList<Coro> listaCoros;
    private static Coro coro;

    public Data() {

    }

    public void setListaCoros(ArrayList<Coro> listaCoros) {
        this.listaCoros = listaCoros;
    }

    public ArrayList<Coro> getListaCoros() {
        return listaCoros;
    }

    public void setCoroBeingUsed(Coro coro) {this.coro = coro;}

    public Coro getCoroBeingUsed() {
        return coro;
    }
}
