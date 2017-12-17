package com.innovate.himnario;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            signInAnonymously();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Connecting to database
        DataBaseHelper myDbHelper;
        myDbHelper = new DataBaseHelper(this);
        try {
            myDbHelper.createDataBase(false);
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        setTitle(R.string.title_activity_main);

        mAuth = FirebaseAuth.getInstance();

        File partitura = new File(getFilesDir() + "/y_mire_y_oi.jpg");
        if (!partitura.exists()) {
            descargarPartituras();
        }
    }

    public void descargarPartituras() {
        if (isOnline()) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage("Se descargaran todas las partituras (~30MB). Por favor mantenerse conectado al internet. La operación se realizará en segundo plano.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BackgroundDownloads downloadPartituras = new BackgroundDownloads(getApplicationContext());
                            downloadPartituras.execute("partituras");
                        }
                    });
            AlertDialog dialog = alertBuilder.create();
            dialog.show();
        } else {
            Toast.makeText(this, "Por favor conéctese al internet para la descarga de partituras.", Toast.LENGTH_LONG).show();
        }

    }

    public void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(LOG_TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(), user.toString(), Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(LOG_TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void onClick_MisListas(View view){
        Intent intent = new Intent(this, MisListasActivity.class);
        startActivity(intent);
    }

    public void onClick_Ajustes(View view) {
        Intent intent = new Intent(this, AjustesActivity.class);
        startActivity(intent);
    }

    public void onClick_Busqueda(View view){
        Intent intent = new Intent(this, BusquedaActivity.class).putExtra(Intent.EXTRA_TEXT, false);
        startActivity(intent);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}

