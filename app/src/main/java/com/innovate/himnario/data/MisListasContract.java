package com.innovate.himnario.data;

import android.content.Context;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Joel on 16-Jul-15.
 */
public class MisListasContract  {
    public static final String LOG_TAG = MisListasContract.class.getSimpleName();
    private static final String LISTAS_PREFERENCE = "lista_de_listas";
    static ArrayList<Long> listasArray = new ArrayList<Long>();
    private Context context;

    public MisListasContract(Context context) {
        this.context = context;
        if (listasArray.isEmpty()) {
            String listaDListas = PreferenceManager.getDefaultSharedPreferences(context).getString(LISTAS_PREFERENCE, "");
            Scanner sc = new Scanner(listaDListas);
            sc.useDelimiter(",");
            while(sc.hasNext()) {
                long listaID = Long.parseLong(sc.next());
                listasArray.add(listaID);
            }
        }

    }

    public static final class MisListasEntry implements BaseColumns{
        public static final String TABLE_NAME = "listasTable";

        public static final String COLUMN_LIST_ID = "_id";
        public static final String COLUMN_FECHA = "fecha";
        public static final String COLUMN_TON_RAP = "ton_rap";
        public static final String COLUMN_TON_LENT = "ton_lent";
        public static final String COLUMN_TON_GLOBAL = "ton_global";
        public static final String COLUMN_NOM_FILE= "nom_file";

        public static final int KEY_ROWID = 0;
        public static final int KEY_FECHA = 1;
        public static final int KEY_TON_RAP = 2;
        public static final int KEY_TON_LENT = 3;
        public static final int KEY_TON_GLOBAL = 4;
        public static final int KEY_NOM_FILE = 5;
    }

    public void addListaToArray(long rowID){
        Log.v(LOG_TAG, "rowId: " + rowID);
        listasArray.add(rowID);
    }

    public long getElementInListasArray(int index){
        return listasArray.get(index);
    }

    public ArrayList<Long> getListasArray() {
        return listasArray;
    }

    //Always call when deleting list
    public void removeElementFromArrayList(long deletedRowID) {
        listasArray.remove(deletedRowID);
        String newList = toString();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(LISTAS_PREFERENCE, newList).apply();
    }

    public void removeAllElements(){
        listasArray.clear();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(LISTAS_PREFERENCE, "").apply();
    }

    public void printArray() {
        Log.v(LOG_TAG, "ArrayList: " + listasArray);
    }
    
    public String toString() {
        String listaStr = "";
        for (long lista: listasArray) {
            listaStr += lista + ",";
        }
        return listaStr;
    }
}

