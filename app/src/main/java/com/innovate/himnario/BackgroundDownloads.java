package com.innovate.himnario;

import android.content.Context;
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

    // Firebase variables
    private FirebaseDatabase database = Utils.getDatabase();
    private StorageReference partituraStorageRef;
    private StorageReference audioStorageRef;
    private StorageReference todasPartiturasStorageRef;
    private StorageReference todosAudiosStorageRef;
    private DatabaseReference corosRef;
    private FirebaseStorage storage;

    private int contPartituras = 0;
    private int contAudios = 0;
    private Context context;

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

        if (params[0].equals("partituras")){
            getPartiturasFromDB(corosQuery);
        } else if (params[0].equals("audios")) {
            getAudiosFromDB(corosQuery);
        }

        return null;
    }

    public void getPartiturasFromDB(Query query) {
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
                downloadPartitura(partiturasStack, nombresStack);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void downloadPartitura(final Stack<StorageReference> partiturasStack, final Stack<String> nombresStack) {
        final String partituraStr = nombresStack.pop();
        final File localFile = new File(context.getFilesDir() + "/" + partituraStr);
        StorageReference storageReference = partiturasStack.pop();
        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.v("DOWNLOADS", partituraStr);
                if (!partiturasStack.isEmpty()) {
                    downloadPartitura(partiturasStack, nombresStack);
                } else {
                    Toast.makeText(context, "Descarga completada.", Toast.LENGTH_LONG).show();
                }
            }
        });
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
}
