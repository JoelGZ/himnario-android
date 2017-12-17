package com.innovate.himnario.data;

import android.provider.BaseColumns;

/**
 * Created by Joel on 26-Oct-15.
 */
public class CELContract {          //CEL = coros en lista
    public static final class CELEntry implements BaseColumns {

        public static final String COLUMN_CORO_ID = "_id";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_FILE_NAME = "fileName";
        public static final String COLUMN_VEL = "velocidad";
        public static final String COLUMN_ORDEN = "orden";
        public static final String COLUMN_TON = "tonalidad";

        public static final int KEY_CORO_ID = 0;
        public static final int KEY_NOMBRE = 1;
        public static final int KEY_FILE_NAME = 2;
        public static final int KEY_VEL = 3;
        public static final int KEY_ORDEN = 4;
        public static final int KEY_TON = 5;
    }
}
