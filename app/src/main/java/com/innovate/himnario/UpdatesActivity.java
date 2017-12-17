package com.innovate.himnario;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class UpdatesActivity extends AppCompatActivity {

    private TextView updatesTxt;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updates);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        updatesTxt = (TextView)findViewById(R.id.updatesTxt);

        loadUpdates();
    }

    public void loadUpdates() {
        FirebaseDatabase database = Utils.getDatabase();
        DatabaseReference updatesRef = database.getReference().child("updates");
        Query query = updatesRef.orderByKey();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                text = "";
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    text += snap.getValue().toString() + "\n\n";
                }


                updatesTxt.setText(text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
