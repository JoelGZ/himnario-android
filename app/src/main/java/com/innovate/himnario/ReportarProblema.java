package com.innovate.himnario;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ReportarProblema extends AppCompatActivity {

    private EditText subject;
    private EditText body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportar_problema);

        subject = (EditText) findViewById(R.id.subjectBody);
        body = (EditText) findViewById(R.id.body);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }

    public void onClick_Send(View view){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"innovate.appsjgz@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject.getText() + " (Android)");
        intent.putExtra(Intent.EXTRA_TEXT, body.getText());
        try {
            startActivity(Intent.createChooser(intent, "Enviar correo..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ReportarProblema.this, "Error. No hay una aplicación de correo instalada.", Toast.LENGTH_LONG).show();
        }
    }

}
