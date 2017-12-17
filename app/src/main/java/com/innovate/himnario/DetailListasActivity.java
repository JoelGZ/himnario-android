package com.innovate.himnario;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.innovate.himnario.data.CELContract;
import com.innovate.himnario.data.Coro;
import com.innovate.himnario.data.Coros;
import com.innovate.himnario.data.MisListasContract;


public class DetailListasActivity extends ActionBarActivity {
    private static final String LOG_TAG = DetailListasActivity.class.getSimpleName();
    private static final String INTENT_EXTRA_CORO = "CORO";
    private static final String INTENT_EXTRA_LISTID = "LIST_ID";
    private static final String INTENT_EXTRA_ORDEN = "ORDEN_CORO";
    private static final String STATE_LISTA = "listaRowId";
    private ShareActionProvider shareAP;
    private DataBaseHelper myDbHelper;
    private Cursor cursor;
    private long idLista;
    private CorosAdapter rapidosAdapter = null;
    private CorosAdapter lentosAdapter = null;
    private int cantidadRapidos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_listas);

        FirebaseDatabase database = Utils.getDatabase();
        final DatabaseReference rootRef = database.getReference();

        //Connecting to database
        myDbHelper = new DataBaseHelper(this);
        try {
            myDbHelper.openDataBase();
        } catch (Exception e) {
            throw new Error("Unable to create database");
        }

        if (savedInstanceState != null) {
            idLista = savedInstanceState.getLong(STATE_LISTA);
            cursor = myDbHelper.getRowListas(idLista);
        } else {
            String numLista;
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra((Intent.EXTRA_TEXT))) {
                numLista = intent.getStringExtra((Intent.EXTRA_TEXT));
                long id = Long.parseLong(numLista);
                idLista = id;
                cursor = myDbHelper.getRowListas(id);
            }
        }
        //Initialize controls
        TextView nomListaDetailTxt = (TextView)findViewById(R.id.nomListaDetailTxt);

        //Screen awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String nomLista = cursor.getString(MisListasContract.MisListasEntry.KEY_FECHA);
        setTitle(nomLista);
        final long listaID = cursor.getLong(MisListasContract.MisListasEntry.KEY_ROWID);

        nomListaDetailTxt.setText(nomLista);
        String WHERERM = CELContract.CELEntry.COLUMN_VEL + "='RM'";
        String WHEREL = CELContract.CELEntry.COLUMN_VEL + "='L'";
        Cursor auxLista = myDbHelper.getAllRowsCoroenLista(listaID, null);

        //Rapidos y medios
        if (auxLista.getCount() != 0){
            Cursor rapidosCursor = myDbHelper.getAllRowsCoroenLista(listaID, WHERERM);
            Cursor lentosCursor = myDbHelper.getAllRowsCoroenLista(listaID, WHEREL);

            cantidadRapidos = rapidosCursor.getCount();
            if (rapidosCursor.getCount() != 0){
                rapidosAdapter = new CorosAdapter(DetailListasActivity.this, rapidosCursor, 2, idLista);
            }
            if (lentosCursor.getCount() != 0){
                lentosAdapter = new CorosAdapter(DetailListasActivity.this, lentosCursor, 2, idLista);
            }
        }

        final ListView listViewRapidos = (ListView)findViewById(R.id.rapidosList);
        ListView listViewLentos = (ListView)findViewById(R.id.lentosList);

        listViewRapidos.setFocusable(false);        // para que comienze en TextView
        listViewLentos.setFocusable(false);

        listViewRapidos.setAdapter(rapidosAdapter);
        listViewLentos.setAdapter(lentosAdapter);
        listViewRapidos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor)rapidosAdapter.getItem(position);
                final String numCoro = c.getString(Coros.CorosEntry.KEY_ROWID);
                final int ordenDelCoro = position;
                DatabaseReference coroRef = rootRef.child("coros").child(Integer.toString(Integer.parseInt(numCoro)));
                coroRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Coro coro = new Coro(dataSnapshot, Integer.parseInt(numCoro));
                        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                        intent.putExtra(INTENT_EXTRA_CORO, coro);
                        intent.putExtra(INTENT_EXTRA_LISTID, listaID);
                        intent.putExtra(INTENT_EXTRA_ORDEN, ordenDelCoro);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        listViewLentos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor)lentosAdapter.getItem(position);
                final String numCoro = c.getString(Coros.CorosEntry.KEY_ROWID);
                final int ordenDelCoro = position  + cantidadRapidos;
                DatabaseReference coroRef = rootRef.child("coros").child(Integer.toString(Integer.parseInt(numCoro)));
                coroRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Coro coro = new Coro(dataSnapshot, Integer.parseInt(numCoro));
                        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                        intent.putExtra(INTENT_EXTRA_CORO, coro);
                        intent.putExtra(INTENT_EXTRA_LISTID, listaID);
                        intent.putExtra(INTENT_EXTRA_ORDEN, ordenDelCoro);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_listas, menu);
    /*    getMenuInflater().inflate(R.menu.menu_share, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        shareAP = (ShareActionProvider) item.getActionProvider();*/

        // Return true to display menu
        return true;
    }

   /* // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (shareAP != null) {
            shareAP.setShareIntent(shareIntent);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
           /* will use when sending list
           Intent myShareIntent = new Intent(Intent.ACTION_SEND);
            myShareIntent.setType("text/*");
            myShareIntent.putExtra(Intent.EXTRA_TEXT, "Hola Mundo!");
            setShareIntent(myShareIntent);*/
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick_Agregar_Coro_inList(View view) {
        Log.v(LOG_TAG, "listaID: " + idLista);
        Intent intent = new Intent(getApplicationContext(), BusquedaParaListaActivity.class).putExtra("listaid", idLista);
        DetailListasActivity.this.finish();         // so when pressing back it wont go here
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(STATE_LISTA, idLista);
        Log.v(LOG_TAG, "LOGDDD: " + idLista);
        super.onSaveInstanceState(outState);
    }

   /* public void SumTonalidades(Cursor tonCursor){
        switch (tonCursor.getString(Coros.CorosEntry.KEY_TONALIDAD)) {
            case "C":
                ++Do;
                break;
            case "Eb":
                ++Mib;
                break;
            case "F":
                ++Fa;
                break;
            case "G":
                ++Sol;
                break;
            case "Bb":
                ++Bb;
                break;
        }
    }

    public void DetectTonalidad(){
        String ton = "";
        if (Do > 0){
            ton += "Do (C) ";
        } else if (Mib > Do){
            ton += "Mi Bemol (Eb) ";
        } else if (Fa > Mib) {
            ton += "Fa (F) ";
        } else if (Sol > Fa){
            ton += "Sol (G) ";
        } else if (Bb > Sol){
            ton += "Si bemol (Bb) ";
        }
        Log.v(LOG_TAG, "DO:" + Do + " Mib:" + Mib + " Fa:" + Fa + " Sol:" + Sol + " Bb:" + Bb);
        Log.v(LOG_TAG, ton);

        myDbHelper.updateTonListas(idLista, ton);
        Cursor cursor = myDbHelper.getAllRowsListas(null);

        String msg = "idLista: " + cursor.getString(MisListasContract.MisListasEntry.KEY_ROWID) +
                "nombre: " + cursor.getString(MisListasContract.MisListasEntry.KEY_FECHA) +
                "tonalidad: " +  cursor.getString(MisListasContract.MisListasEntry.KEY_TONALIDAD);
        Log.v(LOG_TAG, msg);

    }*/
}
