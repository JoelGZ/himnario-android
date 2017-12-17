package com.innovate.himnario;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Joel on 29-Mar-17.
 */

public class Utils {

    private static FirebaseDatabase database;

    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }
        return database;
    }
}