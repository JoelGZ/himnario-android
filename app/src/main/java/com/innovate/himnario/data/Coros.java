package com.innovate.himnario.data;

import android.provider.BaseColumns;

/**
 * Created by Joel on 01-Aug-15.
 */
public class Coros {
    public static final class CorosEntry implements BaseColumns {
        public static final String TABLE_NAME = "corosTable";

        public static final String COLUMN_ROWID = "_id";
        public static final String COLUMN_ORDEN = "orden";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_CUERPO = "cuerpo";
        public static final String COLUMN_TONALIDAD = "tonalidad";
        public static final String COLUMN_VELOCIDAD_LETRA = "velletra";
        public static final String COLUMN_TIEMPO = "tiempo";
        public static final String COLUMN_MUSICA = "musica";
        public static final String COLUMN_AUT_MUS = "autormusica";
        public static final String COLUMN_AUT_LET = "autorletra";
        public static final String COLUMN_CITA = "cita";
        public static final String COLUMN_HISTORIA = "historia";

        public static final int KEY_ROWID = 0;
        public static final int KEY_ORDEN = 1;
        public static final int KEY_NOMBRE = 2;
        public static final int KEY_CUERPO = 3;
        public static final int KEY_TONALIDAD = 4;
        public static final int KEY_VELOCIDAD_LETRA = 5;
        public static final int KEY_TIEMPO = 6;
        public static final int KEY_MUSICA = 7;
        public static final int KEY_AUT_MUS = 8;
        public static final int KEY_AUT_LET = 9;
        public static final int KEY_CITA = 10;
        public static final int KEY_HISTORIA = 11;

    }
}
