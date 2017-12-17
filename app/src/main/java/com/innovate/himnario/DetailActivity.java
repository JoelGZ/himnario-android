package com.innovate.himnario;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.innovate.himnario.data.Coro;

public class DetailActivity extends ActionBarActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final String INTENT_EXTRA_CORO = "CORO";
    private static final String INTENT_EXTRA_LISTID = "LIST_ID";
    private static final String INTENT_EXTRA_ORDEN = "ORDEN_CORO";

    Coro coro = new Coro();
    long listaId = -1;
    int ordenDelCoro = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Initializing texts
        TextView nombreText = (TextView)findViewById(R.id.nomCoroText);
        TextView tonText = (TextView)findViewById(R.id.tonalidadText);
        TextView velText = (TextView)findViewById(R.id.velocidadText);
        TextView tiempoText = (TextView)findViewById(R.id.tiempoText);
        TextView cuerpoText = (TextView)findViewById(R.id.cuerpoText);
        TextView historiaText = (TextView)findViewById(R.id.historiaText);
        TextView autorletText = (TextView)findViewById(R.id.autorletText);
        TextView autormusText = (TextView)findViewById(R.id.autormusText);
        TextView citaTxt = (TextView)findViewById(R.id.citaTxt);
        TextView txtViewCita = (TextView)findViewById(R.id.txtViewCita);

        //Screen awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(INTENT_EXTRA_CORO)) {
            coro = intent.getParcelableExtra(INTENT_EXTRA_CORO);
            Data data = new Data();
            data.setCoroBeingUsed(coro);
            setTitle(coro.nombre);
            if (intent.hasExtra(INTENT_EXTRA_LISTID)) {
                listaId = intent.getLongExtra(INTENT_EXTRA_LISTID, -1);
                ordenDelCoro = intent.getIntExtra(INTENT_EXTRA_ORDEN, 1);
            }
        }

        String nomCoro = coro.nombre;
        nombreText.setText(nomCoro);

        LegibleText.setTonalidad(coro.ton, 1);
        tonText.setText(LegibleText.getTonalidad());
        LegibleText.setVelocidad(coro.vel_let);
        velText.setText(LegibleText.getVelocidad());

        tiempoText.setText(Integer.toString(coro.tiempo));
        cuerpoText.setText(coro.cuerpo);

        String nod = "Desconocido";
        String dollar = "$";
        if(!coro.historia.equals(dollar)){
            historiaText.setText(coro.historia);
        } else {
            historiaText.setText("");
        }

        if(!coro.cita.equals(dollar)){
            citaTxt.setText(coro.cita);
        } else {
            citaTxt.setText("");
            txtViewCita.setText("");
        }

        if (!coro.aut_let.equals(dollar)){
            autorletText.setText(coro.aut_let);
        } else {
            autorletText.setText(nod);
        }

        if (!coro.aut_mus.equals(dollar)){
            autormusText.setText(coro.aut_mus);
        } else {
            autormusText.setText(nod);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
           /* case R.id.action_settings:
                return true;*/
            case R.id.action_home:
                goHome();
                return true;
            case R.id.action_search:
                goSearch();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goHome(){
        Intent inicio = new Intent(getApplicationContext(), MainActivity.class);
        String msg =  NavUtils.shouldUpRecreateTask(this, inicio) + "";
        Log.v(LOG_TAG, msg);
        NavUtils.navigateUpTo(this, inicio);
        startActivity(inicio);
    }

    public void goSearch(){
        Intent busqueda = new Intent(getApplicationContext(), BusquedaActivity.class);
        NavUtils.navigateUpTo(this, busqueda);
        startActivity(busqueda);
    }

    public void onClick_Musica(View view){
        Intent intent;
        Log.v(LOG_TAG, listaId + "");
        if (listaId != -1) {        //if parent activity is DetailListasActivity (listid has been sent)
            intent = new Intent(getApplicationContext(), MusicaPagerActivity.class)
                    .putExtra(INTENT_EXTRA_LISTID, listaId)
                    .putExtra(INTENT_EXTRA_ORDEN, ordenDelCoro);
        } else {
            intent = new Intent(getApplicationContext(), MusicaActivity.class).putExtra("CORO", coro);
        }

        ImageView musicaBtn = (ImageView)findViewById(R.id.musicaBtnDetail);

        // Just a shadow when pressing
        int[] attrs = new int[] { android.R.attr.selectableItemBackground /* index 0 */};
        TypedArray ta = obtainStyledAttributes(attrs);
        Drawable drawableFromTheme = ta.getDrawable(0);
        ta.recycle();
        musicaBtn.setBackgroundDrawable(drawableFromTheme);

        startActivity(intent);
    }

}
