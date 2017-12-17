package com.innovate.himnario;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.innovate.himnario.data.Coros;


public class ResultsActivity extends ActionBarActivity {

    private static final String LOG_TAG = ResultsActivity.class.getSimpleName();
    CorosAdapter mCorosAdapter = null;
    private Cursor cursor = null;
    private DataBaseHelper myDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Connecting to database
        myDbHelper = new DataBaseHelper(this);
        try {
            myDbHelper.openDataBase();
        } catch (Exception e) {
            throw new Error("Unable to open database");
        }
        String query;
        int flag = 0;

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra((Intent.EXTRA_TEXT))){
            query = intent.getStringExtra(Intent.EXTRA_TEXT);
            cursor = myDbHelper.getAllRowsCoros(query);

            // next block is to fix bug un nullpointer exception in cursor.getCount()
            int cursorCount;
            try {
                cursorCount = cursor.getCount();
            } catch (NullPointerException e) {
                try {
                    myDbHelper.createDataBase(true);
                } catch (Exception err) {
                    throw new Error("Unable to create database");
                }
                cursorCount = cursor.getCount();
            }

            if(cursorCount > 0){              // si hay resultados
              //  setContentView(R.layout.activity_results);
                if (intent.hasExtra(("listaID"))){
                    flag = 1;
                    Log.v(LOG_TAG, "Si recibe listaID.");
                }
                long idRow = intent.getLongExtra("listaID", -1);
                if (idRow == 0){
                    flag = 0;
                    Log.v(LOG_TAG, "no row id");
                }

                mCorosAdapter = new CorosAdapter(getApplicationContext(), cursor, flag, idRow);
                ListView listView = new ListView(this);
               // ListView listView = (ListView)findViewById(R.id.);
                listView.setAdapter(mCorosAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.v(LOG_TAG, "item with position " + position + " was clicked.");
                        Cursor c = (Cursor)mCorosAdapter.getItem(position);
                        String numCoro = c.getString(Coros.CorosEntry.KEY_ROWID);
                        Intent detailIntent = new Intent(getApplicationContext(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, numCoro);
                        startActivity(detailIntent);
                    }
                });
            } else {
                setContentView(R.layout.noresults);
                TextView noresult = (TextView)findViewById(R.id.noResultsTxt);
                noresult.setText("No existen resultados de busqueda.");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_results, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }-*/

        return super.onOptionsItemSelected(item);
    }
}
