package com.innovate.himnario;

import com.innovate.himnario.data.Coro;

import java.util.ArrayList;

/**
 * Created by Joel on 13-Apr-17.
 */

public class Data {

    private static ArrayList<Coro> listaCoros;
    private static Coro coro;
    static int cantidadCorosEnListaCorosLocalOLD = 0;
    static int cantidadCorosEnListaCorosLocal = 0;

    public Data() {

    }

    public void setListaCoros(ArrayList<Coro> listaCoros) {
        this.listaCoros = listaCoros;
        setCantidadCorosEnListaCorosLocalOLD();
        cantidadCorosEnListaCorosLocal = listaCoros.size();
    }

    public ArrayList<Coro> getListaCoros() {
        return listaCoros;
    }

    public void setCoroBeingUsed(Coro coro) {this.coro = coro;}

    public void clearListaCoros(){
        listaCoros = null;
    }

    public int getCantidadCorosEnListaCorosLocal() {
        return cantidadCorosEnListaCorosLocal;
    }

    public int getCantidadCorosEnListaCorosLocalOLD(){
        return cantidadCorosEnListaCorosLocalOLD;
    }

    public void setCantidadCorosEnListaCorosLocalOLD(){
        cantidadCorosEnListaCorosLocalOLD = cantidadCorosEnListaCorosLocal;
    }
}
