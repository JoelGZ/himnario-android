package com.innovate.himnario;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.innovate.himnario.data.Coro;

import java.io.File;
import java.util.ArrayList;

public class AjustesActivity extends AppCompatActivity {
    private final String LOG_TAG = AjustesActivity.class.getSimpleName();

    private FirebaseDatabase database = Utils.getDatabase();
    private DatabaseReference corosRef;

    public enum FileType {
        PARTITURA,
        AUDIO
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        ListView generalList = (ListView) findViewById(R.id.general_settings);
        ListView contactList = (ListView) findViewById(R.id.contact_settings);

        corosRef = database.getReference().child("coros");

        ArrayList<String> genArray = new ArrayList<>();
        final File audio = new File(getFilesDir() + "/a_cristo_coronad.mp3");
        if (!audio.exists()) {
            genArray.add("Descargar audios");
        } else {
            genArray.add("Eliminar audios");
        }

        ArrayAdapter<String> genArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genArray);
        generalList.setAdapter(genArrayAdapter);

        generalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        //descargar audios
                        if  (!audio.exists()) {
                            descargarAudios();
                            //change text
                        } else {
                            eliminarAudios();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        ArrayList<String> contactArray = new ArrayList<>();
        contactArray.add("Reporta un problema");
        contactArray.add("Actualizaciones");

        ArrayAdapter<String> contactArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactArray);
        contactList.setAdapter(contactArrayAdapter);

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        //Reportar Problema
                        Intent intentReporte = new Intent(AjustesActivity.this, ReportarProblema.class);
                        startActivity(intentReporte);
                        break;
                    case 1:
                        //actualizaciones
                        Intent intentUpdates = new Intent(AjustesActivity.this, UpdatesActivity.class);
                        startActivity(intentUpdates);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void eliminarAudios() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Se eiminarán los audios descargadss. Solamente estarán disponibles si hay conexión al internet.").setNegativeButton("Cancelar", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminateFile(FileType.AUDIO);
                    }
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public void descargarAudios(){
        if (isOnline()) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage("Se descargaran todos los audios (~70MB). Por favor mantenerse conectado al internet. La operación se realizará en segundo plano.").setNegativeButton("Cancelar", null)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BackgroundDownloads downloadAudios = new BackgroundDownloads(getApplicationContext());
                            downloadAudios.execute("audios");
                        }
                    });
            AlertDialog dialog = alertBuilder.create();
            dialog.show();
        } else {
            Toast.makeText(this, "No hay conexión al internet.", Toast.LENGTH_LONG).show();
        }
    }

    public void eliminateFile(final FileType type) {
        Query query = corosRef.orderByChild("orden");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot coroSnapshot: dataSnapshot.getChildren()) {
                    Coro coro = coroSnapshot.getValue(Coro.class);
                    int coroId = Integer.parseInt(coroSnapshot.getKey());
                    coro.id = coroId;
                    if (coroId < 3000){
                        String path = getFilesDir() + "/" + coro.sName.replace(" ", "_");
                        if (type == FileType.PARTITURA) {
                            path += ".jpg";
                        } else if (type == FileType.AUDIO) {
                            path += ".mp3";
                        }
                        File partituraFile = new File(path);
                        if (partituraFile.exists()) {
                            boolean flag = partituraFile.delete();
                            Log.d(LOG_TAG, "Deleted: " + coro.sName + " - " + flag);
                        }
                    }
                }
                Toast.makeText(getApplicationContext(), "Se han eliminado los archivos exitosamente.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
