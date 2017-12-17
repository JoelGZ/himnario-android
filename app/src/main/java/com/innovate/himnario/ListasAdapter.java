package com.innovate.himnario;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.innovate.himnario.data.CELContract;
import com.innovate.himnario.data.MisListasContract;

/**
 * Created by Joel on 01-Aug-15.
 */
public class ListasAdapter extends CursorAdapter {
    private static final String LOG_TAG = ListasAdapter.class.getSimpleName();
    private Cursor c;
    private Context contextGlobal;
    private int flag;
    private DataBaseHelper myDbHelper;

    public ListasAdapter(Context context, Cursor c, int flag) {
    //cuando flag es 1 solo indica que es MisListas Activity la que esta llamando, 0 serai Detail o results activities
        super(context, c);
        this.c = c;
        this.contextGlobal = context;
        this.flag = flag;

        //Connecting to database
        myDbHelper = new DataBaseHelper(contextGlobal);
        try {
            myDbHelper.openDataBase();
        } catch (Exception e) {
            throw new Error("Unable to create database");
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = null;
        if (flag == 0){
            view = LayoutInflater.from(context).inflate(R.layout.item_lista_listas, parent, false);
        } else if (flag == 1){
           view = LayoutInflater.from(context).inflate(R.layout.list_item_listas, parent, false);
        }
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        if (flag == 0){
            String nombre_fecha =  cursor.getString(MisListasContract.MisListasEntry.KEY_FECHA);
            TextView nombre = (TextView) view.findViewById(R.id.nombreListaDialog);
            nombre.setText(nombre_fecha);
        } else if (flag == 1){
            String nombre_fecha2 =  cursor.getString(MisListasContract.MisListasEntry.KEY_FECHA);
            Log.v(LOG_TAG, nombre_fecha2);
            TextView nombre2 = (TextView) view.findViewById(R.id.nombreLista);
            nombre2.setText(nombre_fecha2);

            String coros = "";
            String where = null;
            Cursor cursorCEL = myDbHelper.getAllRowsCoroenLista(cursor.getLong(MisListasContract.MisListasEntry.KEY_ROWID), where);
            Log.v(LOG_TAG, "rowid: " + cursor.getLong(MisListasContract.MisListasEntry.KEY_ROWID));
            Log.v(LOG_TAG, "Count: " + cursorCEL.getCount());
            if (cursorCEL.getCount() != 0){
                Log.v(LOG_TAG, "HOLA" + cursorCEL.getString(CELContract.CELEntry.KEY_NOMBRE));
                if (cursorCEL.getCount() == 1) {
                    coros += cursorCEL.getString(CELContract.CELEntry.KEY_NOMBRE);
                } else {
                    while(!cursorCEL.isLast()){
                        Log.v(LOG_TAG, cursorCEL.getString(CELContract.CELEntry.KEY_NOMBRE));
                        if(cursorCEL.isFirst()){
                            coros += cursorCEL.getString(CELContract.CELEntry.KEY_NOMBRE);
                        } else {
                            coros += " ~ " + cursorCEL.getString(CELContract.CELEntry.KEY_NOMBRE);
                        }
                        cursorCEL.moveToNext();
                    }
                    if (cursorCEL.isLast()){
                        coros += " ~ " + cursorCEL.getString(CELContract.CELEntry.KEY_NOMBRE);
                    }
                }
            }

            TextView corosTxt = (TextView)view.findViewById(R.id.corosLista);
            corosTxt.setText(coros);

            final long idLista = Long.parseLong(cursor.getString(MisListasContract.MisListasEntry.KEY_ROWID));
            final ImageButton eliminarBtn = (ImageButton)view.findViewById(R.id.eliminarListaBtn);
            eliminarBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(contextGlobal);
                    alert.setMessage("¿Estas seguro? Esto no se puede deshacer.");
                    alert.setTitle("Eliminando...");
                    alert.setCancelable(false);
                    alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });

                    alert.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            myDbHelper.deleteRowListas(idLista, null);
                            MisListasContract listas = new MisListasContract(contextGlobal);
                            listas.removeElementFromArrayList(idLista);
                            listas.printArray();
                            Cursor cursor2 = myDbHelper.getAllRowsListas(null);
                            changeCursor(cursor2);
                            notifyDataSetChanged();
                        }
                    });

                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                }
            });
        }
    }
}
