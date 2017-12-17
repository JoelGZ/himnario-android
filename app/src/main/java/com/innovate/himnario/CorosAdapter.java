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
import android.widget.Toast;

import com.innovate.himnario.data.CELContract;
import com.innovate.himnario.data.Coros;
import com.innovate.himnario.data.MisListasContract;

/**
 * Created by Joel on 19-Jul-15.
 */
public class CorosAdapter extends CursorAdapter {
    private static final String LOG_TAG = CorosAdapter.class.getSimpleName();
    private int flags;
    private Context context;
    public Cursor cursorG;
    public Cursor cursorListas;
    public long listaID;
    private DataBaseHelper myDbHelper;

    public CorosAdapter(Context context, Cursor c, int flags, long listaID){
        super(context, c, flags);
        this.flags = flags;
        this.context = context;
        this.cursorG = c;
        this.listaID = listaID;
        //Connecting to database
        myDbHelper = new DataBaseHelper(context);
        try {
            myDbHelper.openDataBase();
        } catch (Exception e) {
            throw new Error("Unable to create database");
        }
        this.cursorListas = myDbHelper.getRowListas(listaID);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (flags == 0){
            return LayoutInflater.from(context).inflate(R.layout.list_item_coros, parent, false);
        } else if (flags == 1){
            return LayoutInflater.from(context).inflate(R.layout.list_item_coros_add, parent, false);
        } else if (flags == 2){
            return LayoutInflater.from(context).inflate(R.layout.list_item_coros_lista, parent, false);
        } else {
            return null;
        }
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        if (flags == 0){            // cuando lo llama ResultsActivity desde Busqueda
            String nombre = cursor.getString(Coros.CorosEntry.KEY_NOMBRE);
            TextView nombreCoro = (TextView)view.findViewById(R.id.nombreCoro);
            nombreCoro.setText(nombre);

            LegibleText.setVelocidad(cursor.getString(Coros.CorosEntry.KEY_VELOCIDAD_LETRA));
            String vel = LegibleText.getVelocidad();
            TextView velText = (TextView)view.findViewById(R.id.velText);
            velText.setText(vel);

            LegibleText.setTonalidad(cursor.getString(Coros.CorosEntry.KEY_TONALIDAD), 0);
            String ton = LegibleText.getTonalidad();
            TextView tonText = (TextView)view.findViewById(R.id.tonText);
            tonText.setText(ton);
        } else if (flags == 1){         // cuando lo llama ResultsActivity desde NuevaLista
            final String nombre2 = cursor.getString(Coros.CorosEntry.KEY_NOMBRE);
            TextView nombreCoroAdd = (TextView)view.findViewById(R.id.nombreCoroAdd);
            nombreCoroAdd.setText(nombre2);

            LegibleText.setVelocidad(cursor.getString(Coros.CorosEntry.KEY_VELOCIDAD_LETRA));
            String vel = LegibleText.getVelocidad();
            TextView velText = (TextView)view.findViewById(R.id.velTextAdd);
            velText.setText(vel);

            LegibleText.setTonalidad(cursor.getString(Coros.CorosEntry.KEY_TONALIDAD), 0);
            String ton = LegibleText.getTonalidad();
            TextView tonText = (TextView)view.findViewById(R.id.tonTextAdd);
            tonText.setText(ton);

            final long idCoro = Long.parseLong(cursor.getString(Coros.CorosEntry.KEY_ROWID));
            Log.v(LOG_TAG, "listID: "  + listaID);
            Cursor cursorImg = myDbHelper.getRowCoroenLista(listaID, idCoro);
            final ImageButton btn = (ImageButton)view.findViewById(R.id.addCoroToListBtn);

            boolean flag1 = false;
            if (cursorImg.getCount() != 0){
                flag1 = true;
            }
            if (flag1){
                btn.setImageResource(R.drawable.ic_check);
            } else {
                btn.setImageResource(R.drawable.ic_plus);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cursorListas = myDbHelper.getRowListas(listaID);
                        boolean flag = false;
                        Cursor cursorCEL = myDbHelper.getRowCoroenLista(listaID, idCoro);
                        if (cursorCEL.getCount() != 0){
                           flag = true;
                        }

                        if (!flag){
                            myDbHelper.agregarCoroALista(cursorListas.getLong(MisListasContract.MisListasEntry.KEY_ROWID), idCoro, false);
                            String msg = "El coro ha sido agregado a la lista.";
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                            btn.setImageResource(R.drawable.ic_check);
                        }
                    }
                });
            }
        } else if (flags == 2){     //llamado para poblar una lista en DetailListaActivity
            final String nombre = cursor.getString(CELContract.CELEntry.KEY_NOMBRE);
            TextView nombreCoro = (TextView)view.findViewById(R.id.nombreCoro);
            nombreCoro.setText(nombre);

            String where = "";
            final String coroVel = cursor.getString(CELContract.CELEntry.KEY_VEL);
            if (coroVel.equals("L")){
                where = CELContract.CELEntry.COLUMN_VEL + "=\'" + coroVel + "\'";
            } else {
                where = CELContract.CELEntry.COLUMN_VEL + "=\'RM\'";
            }

            final String whereFinal = where;
            final long idCoro = Long.parseLong(cursor.getString(CELContract.CELEntry.KEY_CORO_ID));
            ImageButton btn = (ImageButton)view.findViewById(R.id.removeCorodLista);
            btn.setImageResource(R.drawable.ic_remove);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);

                    alertBuilder.setMessage("¿Deseas eliminar el coro " + nombre + " de esta lista?");
                    alertBuilder.setTitle("Eliminando...");
                    alertBuilder.setCancelable(false);

                    alertBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertBuilder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msg = "El coro ha sido eliminado de la lista.";
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                            Cursor cursor1 = myDbHelper.getRowListas(listaID);
                            myDbHelper.deleteRowCoroenLista(cursor1.getLong(MisListasContract.MisListasEntry.KEY_ROWID), idCoro);
                            Cursor cursor2 = myDbHelper.getAllRowsCoroenLista(listaID, whereFinal);
                            changeCursor(cursor2);
                            notifyDataSetChanged();
                        }
                    });

                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
            });

            //Moviemiento arriba
            ImageButton upBtn = (ImageButton)view.findViewById(R.id.upInLista);
            upBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(LOG_TAG, whereFinal);
                    myDbHelper.editOrdenListas(listaID, idCoro, 1, coroVel);
                    Cursor cursor2 = myDbHelper.getAllRowsCoroenLista(listaID, whereFinal);
                    changeCursor(cursor2);
                    notifyDataSetChanged();
                }
            });

            //Moviemiento abajo
            ImageButton downBtn = (ImageButton)view.findViewById(R.id.downInLista);
            downBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(LOG_TAG, whereFinal);
                    myDbHelper.editOrdenListas(listaID, idCoro, 0, coroVel);
                    Cursor cursor2 = myDbHelper.getAllRowsCoroenLista(listaID, whereFinal);
                    changeCursor(cursor2);
                    notifyDataSetChanged();
                }
            });
        }
    }
}
