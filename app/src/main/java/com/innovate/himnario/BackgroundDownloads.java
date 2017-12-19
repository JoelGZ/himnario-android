package com.innovate.himnario;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.innovate.himnario.data.Coro;

import java.io.File;
import java.util.Stack;


/**
 * Created by Joel on 05-Dec-17.
 */

public class BackgroundDownloads extends AsyncTask {
    private static final String LOG_TAG = BackgroundDownloads.class.getSimpleName();
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String CONT_PART_DES = "ContadorPartiturasDescargadas";

    // Firebase variables
    private FirebaseDatabase database = Utils.getDatabase();
    private StorageReference partituraStorageRef;
    private StorageReference audioStorageRef;
    private StorageReference todasPartiturasStorageRef;
    private StorageReference todosAudiosStorageRef;
    private DatabaseReference corosRef;
    private FirebaseStorage storage;

    private Context context;
    private int contadorPartiturasStackInicial = 0;
    private int contadorGlobal = 0;
    private SharedPreferences.Editor editor;
    private SharedPreferences settings;

    public BackgroundDownloads(Context c) {
        this.context = c;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        storage = FirebaseStorage.getInstance();
        todasPartiturasStorageRef = storage.getReference().child("partituras");
        todosAudiosStorageRef = storage.getReference().child("audios");
        corosRef = database.getReference().child("coros");
        Query corosQuery = corosRef.orderByChild("orden");

        settings = context.getSharedPreferences(PREFS_NAME, 0);

        if (params[0].equals("partituras")){
            getPartiturasFromDB(corosQuery, params);
        } else if (params[0].equals("audios")) {
            getAudiosFromDB(corosQuery);
        }

        return null;
    }

    public void getPartiturasFromDB(Query query, final Object[] params) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Stack<StorageReference> partiturasStack = new Stack<>();
                Stack<String> nombresStack = new Stack<String>();
                for(DataSnapshot coroSnapshot: dataSnapshot.getChildren()) {
                    Coro coro = coroSnapshot.getValue(Coro.class);
                    int coroId = Integer.parseInt(coroSnapshot.getKey());
                    coro.id = coroId;
                    if (coroId < 3000){
                        String partituraStr = coro.sName.replace(" ", "_") + ".jpg";
                        partituraStorageRef = todasPartiturasStorageRef.child(partituraStr);
                        partiturasStack.add(partituraStorageRef);
                        nombresStack.add(partituraStr);
                    }
                }


                if (!nombresStack.isEmpty()) {
                    contadorPartiturasStackInicial = partiturasStack.size();
                    downloadPartiturasFromStack(partiturasStack, nombresStack);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void downloadPartiturasFromStack(final Stack<StorageReference> partiturasStack, final Stack<String> nombresStack) {
        final String partituraStr = nombresStack.pop();
        final File localFile = new File(context.getFilesDir() + "/" + partituraStr);

        /*
        Esta condicion revisa si todas las paritutras estan descargadas.
        Si getContador es 0 es porque no se ha descargado ninguna.
        Si getContador es menor a partituraStack.size es porque no se descargaron todas las partituras
         */
        final int contador = settings.getInt(CONT_PART_DES, 0);
        if (contador != contadorPartiturasStackInicial) {
            if (!localFile.exists()) {
                StorageReference storageReference = partiturasStack.pop();
                storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        if (!partiturasStack.isEmpty()) {
                            addToContador();
                            editor = settings.edit();
                            editor.putInt(CONT_PART_DES, getContador());
                            editor.apply();
                            downloadPartiturasFromStack(partiturasStack, nombresStack);
                        } else {
                            Toast.makeText(context, "Descarga completada.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                //go to next
                if (!partiturasStack.isEmpty() && !nombresStack.isEmpty()) {
                    partiturasStack.pop();
                    downloadPartiturasFromStack(partiturasStack, nombresStack);
                }
            }
        }
    }

    public void getAudiosFromDB(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Stack<StorageReference> audiosStack = new Stack<>();
                Stack<String> nombresStack = new Stack<>();
                for(DataSnapshot coroSnapshot: dataSnapshot.getChildren()) {
                    Coro coro = coroSnapshot.getValue(Coro.class);
                    int coroId = Integer.parseInt(coroSnapshot.getKey());
                    coro.id = coroId;
                    if (coroId < 3000){
                        String audioStr = coro.sName.replace(" ", "_") + ".mp3";
                        audioStorageRef = todosAudiosStorageRef.child(audioStr);
                        audiosStack.add(audioStorageRef);
                        nombresStack.add(audioStr);
                    }
                }
                downloadAudios(audiosStack, nombresStack);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void downloadAudios(final Stack<StorageReference> audiosStack, final Stack<String> nombresStack) {
        final String audioStr = nombresStack.pop();
        final File localFile = new File(context.getFilesDir() + "/" + audioStr);
        StorageReference storageReference = audiosStack.pop();
        storageReference.getFile(localFile).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Downloads", "FAILURE: " + audioStr);
                if (!audiosStack.isEmpty()) {
                    downloadAudios(audiosStack, nombresStack);
                } else {
                    Toast.makeText(context, "Descarga completada.", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("Downloads", "SUCCESS: " + audioStr);
                if (!audiosStack.isEmpty()) {
                    downloadAudios(audiosStack, nombresStack);
                } else {
                    Toast.makeText(context, "Descarga completada.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void addToContador() {
        int contadorSettings = settings.getInt(CONT_PART_DES, 0);
        if (contadorSettings == 0) {
            contadorGlobal += 1;
        } else {
            if (contadorGlobal == 0) {
                contadorGlobal = contadorSettings + 1;
            } else {
                contadorGlobal += 1;
            }
        }
    }

    public int getContador() {
        return contadorGlobal;
    }
}
