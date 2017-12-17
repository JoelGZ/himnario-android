package com.innovate.himnario;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.innovate.himnario.data.MisListasContract;


public class MisListasActivity extends ActionBarActivity {
    private ListasAdapter mListasAdapter = null;
    private static final String LOG_TAG = MisListasActivity.class.getSimpleName();
    private static final String EXTRA_LISTA_ID = "listaid";
    private static final String LISTAS_PREFERENCE = "lista_de_listas";
    DataBaseHelper dbHelper = new DataBaseHelper(this);
    DataBaseHelper myDbHelper;
    SQLiteDatabase db;
    Cursor cursor;
    int flag;
    ListView listView;
    int contadorListas = 0;

    @Override
    protected void onResume() {
        super.onResume();
        cursor = myDbHelper.getAllRowsListas(null);
        if (contadorListas != cursor.getCount()) {
            loadListas();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Connecting to database
        myDbHelper = new DataBaseHelper(getApplicationContext());
        try {
            myDbHelper.openDataBase();
        } catch (Exception e) {
            throw new Error("Unable to create database");
        }

        //Connecting to database
        db = dbHelper.getWritableDatabase();

        try {
            setContentView(R.layout.activity_mis_listas);
            loadListas();
        } catch (Exception e) {}

    }

    public void loadListas() {
        cursor = myDbHelper.getAllRowsListas(null);
        flag = 1;
        mListasAdapter = new ListasAdapter(this, cursor, flag);

        listView = (ListView)findViewById(R.id.listasList);
        listView.setAdapter(mListasAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOG_TAG, "item with position " + position + " was clicked.");
                Cursor c = (Cursor) mListasAdapter.getItem(position);
                String numLista = c.getString(MisListasContract.MisListasEntry.KEY_ROWID);
                Log.v(LOG_TAG, "IDlista seleccionada: " + numLista);
                Intent detailListasIntent = new Intent(getApplicationContext(), DetailListasActivity.class).putExtra(Intent.EXTRA_TEXT, numLista);
                startActivity(detailListasIntent);
            }
        });
        contadorListas = cursor.getCount();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mis_listas_fragment, menu);
        getMenuInflater().inflate(R.menu.menu_mis_listas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        } else */if(id == R.id.action_deleteAll){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setMessage("¿Estas seguro? Esto no se puede deshacer.");
            alert.setTitle("Eliminando...");
            alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            });

            alert.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    myDbHelper.deleteAllRowsListas();
                    MisListasContract listas = new MisListasContract(getApplicationContext());
                    listas.removeAllElements();
                    loadListas();
                }
            });

            AlertDialog alertDialog = alert.create();
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick_Nueva_Lista(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText textBox = new EditText(this);
        textBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        final FrameLayout.LayoutParams lparams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        lparams.setMargins(30,10,15,0);

        builder.setTitle("Nombrar la lista");
        builder.setMessage("Escriba el nombre de su lista.");
        builder.setView(textBox);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //agregar coro a la lista
                String nombreDeLista = textBox.getText().toString();
                if (nombreDeLista.equals("")){
                    Toast.makeText(getApplicationContext(),"Por favor especifique un nombre de lista.", Toast.LENGTH_LONG).show();
                } else {
                    ContentValues values = new ContentValues();
                    values.put(MisListasContract.MisListasEntry.COLUMN_FECHA, nombreDeLista);

                    try {
                        long newRowId = 1000;
                        newRowId = db.insert(MisListasContract.MisListasEntry.TABLE_NAME, MisListasContract.MisListasEntry.COLUMN_FECHA, values);
                        myDbHelper.createListaTable(newRowId);
                        //metiendo la lista a un array
                        MisListasContract listas = new MisListasContract(getApplicationContext());
                        listas.addListaToArray(newRowId);
                        listas.printArray();

                        String where = "_id=" + newRowId;
                        ContentValues valuesFile = new ContentValues();
                        valuesFile.put(MisListasContract.MisListasEntry.COLUMN_NOM_FILE, "");           ///no se que es este filename pero lo tengo que eliminar
                        db.update(MisListasContract.MisListasEntry.TABLE_NAME, valuesFile, where, null);

                        // guardar el id de la lista en shared preferences para guardar los ids de todas las listas en un string
                        String listaDListas = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(LISTAS_PREFERENCE, "");
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(LISTAS_PREFERENCE, listaDListas + newRowId + ",").apply();

                        Log.v(LOG_TAG, "idlista desde newlist: " + newRowId);
                        Intent intent = new Intent(getApplicationContext(), BusquedaParaListaActivity.class).putExtra(EXTRA_LISTA_ID, newRowId);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "menseje:::: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "La lista no pudo ser creada. Intentelo de nuevo.", Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
                    dialog.cancel();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

        textBox.setLayoutParams(lparams);
    }
}
