package com.innovate.himnario;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.innovate.himnario.data.Coro;
import com.innovate.himnario.data.MisListasContract;

import java.util.ArrayList;


public class BusquedaParaListaActivity extends ActionBarActivity {
    private static final String LOG_TAG = BusquedaActivity.class.getSimpleName();
    private static final String STATE_LISTA = "listaRowId";
    private static final String EXTRA_LISTA_ID = "listaid";

    private static long listaId;

    private View recyclerView;
    private Data data;

    Spinner spinner;
    Button btnRapidos;
    Button btnLentos;
    Button btnMedios;
    SearchView searchView;
    LinearLayout searchLayout;
    ProgressBar progressBar;

    //Firebase setup
    FirebaseDatabase database = Utils.getDatabase();
    DatabaseReference rootRef;
    DatabaseReference corosRef;

    ArrayList<Coro> listaDeCoros;
    ArrayList<Coro> listaFiltrada;
    ArrayList<Coro> listaAux = new ArrayList<>();
    ArrayList<String> velocidadesActivas = new ArrayList<>();

    boolean rapBtnAux = false;
    boolean medBtnAux = false;
    boolean lentBtnAux = false;

    DataBaseHelper myDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busqueda_para_lista);

        //Connecting to database
        myDbHelper = new DataBaseHelper(getApplicationContext());
        try {
            myDbHelper.openDataBase();
        } catch (Exception e) {
            throw new Error("Unable to create database");
        }


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_LISTA_ID)) {
            listaId = intent.getLongExtra(EXTRA_LISTA_ID, 1);
        }
        //New code
        rootRef = database.getReference();
        corosRef = rootRef.child("coros");
        data = new Data();

        recyclerView = findViewById(R.id.coro_list_for_lists);
        assert recyclerView != null;

        if (data.getListaCoros() != null) {
            //Something was already loaded
            listaDeCoros = data.getListaCoros();
            setupRecyclerView((RecyclerView) recyclerView, listaDeCoros);
        } else {
            //Start to load data (todos los coros)
            listaDeCoros = new ArrayList<Coro>();
            final Query corosQuery = corosRef.orderByChild("orden");
            queryDataFromDB(corosQuery);
        }

        searchLayout = (LinearLayout) findViewById(R.id.searchLayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Searchview setup
        searchView = (SearchView) findViewById(R.id.searchView);
        setSearchView();
        //Spinner setup
        spinner = (Spinner) findViewById(R.id.spinner);
        setSpinner();

        //Buttons setup
        btnLentos = (Button) findViewById(R.id.buttonLentos);
        btnMedios = (Button) findViewById(R.id.buttonMedios);
        btnRapidos = (Button) findViewById(R.id.buttonRapidos);
        setButtons();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_busqueda_para_lista, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_saveList:
                myDbHelper.close();         //Watch new code
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, ArrayList<Coro> lista) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(lista));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private ArrayList<Coro> listaCoros;

        public SimpleItemRecyclerViewAdapter(ArrayList<Coro> mListaCoros) {
            listaCoros = mListaCoros;
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_coros_add, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Coro coro = listaCoros.get(position);
            holder.coro = coro;
            holder.nombreCoro.setText(coro.nombre);
            LegibleText.setVelocidad(coro.vel_let);
            String legibleVel = LegibleText.getVelocidad();
            holder.velocidad.setText(legibleVel);
            holder.tonalidad.setText(coro.ton);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("CORO", coro);
                    context.startActivity(intent);
                }
            });

            final long idCoro = coro.id;
            Cursor cursorImg = myDbHelper.getRowCoroenLista(listaId, idCoro);
            boolean flag1 = false;
            if (cursorImg.getCount() != 0){
                flag1 = true;
            }
            if (flag1){
                holder.addBtn.setImageResource(R.drawable.ic_remove_circle_outline_black_36dp);
            } else {
                holder.addBtn.setImageResource(R.drawable.ic_add_circle_outline_black_36dp);
                holder.addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor cursorListas = myDbHelper.getRowListas(listaId);
                        boolean flag = false;
                        Cursor cursorCEL = myDbHelper.getRowCoroenLista(listaId, idCoro);
                        if (cursorCEL.getCount() != 0){
                            flag = true;
                        }

                        if (!flag){
                            myDbHelper.agregarCoroALista(cursorListas.getLong(MisListasContract.MisListasEntry.KEY_ROWID), idCoro, false);
                            String msg = "Coro agregado";
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            holder.addBtn.setImageResource(R.drawable.ic_remove_circle_outline_black_36dp);
                        } else {
                            myDbHelper.deleteRowCoroenLista(listaId, idCoro);
                            String msg = "Coro descartado";
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            holder.addBtn.setImageResource(R.drawable.ic_add_circle_outline_black_36dp);
                        }
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return listaCoros.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView nombreCoro;
            public final TextView tonalidad;
            public final TextView velocidad;
            public final ImageButton addBtn;
            public Coro coro;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                nombreCoro = (TextView) view.findViewById(R.id.nombreCoroAdd);
                tonalidad = (TextView) view.findViewById(R.id.tonTextAdd);
                velocidad = (TextView) view.findViewById(R.id.velTextAdd);
                addBtn = (ImageButton) view.findViewById(R.id.addCoroToListBtn);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(STATE_LISTA, listaId);
        super.onSaveInstanceState(outState);
    }


    //New code
    public void queryDataFromDB(Query query) {

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //ListView setup
                for(DataSnapshot coroSnapshot: dataSnapshot.getChildren()) {
                    Coro coro = coroSnapshot.getValue(Coro.class);
                    int coroId = Integer.parseInt(coroSnapshot.getKey());
                    coro.id = coroId;
                    if (coroId < 3000){
                        listaDeCoros.add(coro);
                    }
                }
                setupRecyclerView((RecyclerView) recyclerView, listaDeCoros);
                data.setListaCoros(listaDeCoros);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void filterContentForSearch(String text, String tonalidad) {
        ArrayList<Coro> listaAUsar = new ArrayList<>();

        if (text.equals("")) {
            listaAUsar = listaDeCoros;
            listaFiltrada = new ArrayList<>();
            if (!listaAux.isEmpty()) {
                listaAux.clear();                 //limpiar listaAux ya que no se esta buscando por texto
            }
        } else {
            listaAUsar = listaAux;          //si se esta buscando con texto tambien
        }

        if (!listaFiltrada.isEmpty()) {
            listaFiltrada.clear();              //limpiar listaFiltrada para su uso posterior
        }

        if (velocidadesActivas.size() != 0) {
            for(Coro coro: listaAUsar) {
                if (tonalidad.equals("Ton.")) {         //Si no se esta filtrando por tonalidad
                    for (String velocidad : velocidadesActivas) {
                        if (coro.vel_let.equals(velocidad)) {
                            listaFiltrada.add(coro);
                        }
                    }
                } else {
                    if (coro.ton.equals(tonalidad)) {
                        for (String velocidad : velocidadesActivas) {
                            if (coro.vel_let.equals(velocidad)) {
                                listaFiltrada.add(coro);
                            }
                        }
                    }
                }
            }
            setupRecyclerView((RecyclerView) recyclerView, listaFiltrada);
        } else {
            for(Coro coro: listaAUsar) {
                if(tonalidad.equals("Ton.")) {
                    if (listaAux.isEmpty()) {           //no se esta buscando por texto
                        listaFiltrada = listaDeCoros;
                    } else {
                        listaFiltrada = listaAux;
                    }

                } else {
                    if (coro.ton.equals(tonalidad)) {
                        listaFiltrada.add(coro);
                    }
                }
            }
            setupRecyclerView((RecyclerView) recyclerView, listaFiltrada);
        }
    }

    public void setSpinner() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String auxTonalidad = "";
                switch((int) l){
                    case 0:
                        auxTonalidad = "Ton.";
                        break;
                    case 1:
                        auxTonalidad = "C";
                        break;
                    case 2:
                        auxTonalidad = "Eb";
                        break;
                    case 3:
                        auxTonalidad = "F";
                        break;
                    case 4:
                        auxTonalidad = "G";
                        break;
                    case 5:
                        auxTonalidad = "Bb";
                        break;
                    default:
                        auxTonalidad = "Ton.";
                }

                LegibleText.setTonalidad(auxTonalidad, 0);
                filterContentForSearch(searchView.getQuery().toString(), auxTonalidad);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        String[] spinnerItems = new String[]{"Ton.","Do","Mib","Fa","Sol", "Sib"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        spinner.setAdapter(adapterSpinner);
    }

    public void setSearchView() {

        searchView.setQueryHint("Buscar coro");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                listaAux.clear();           //clear each time I make a new search
                boolean spinnerIsActive = (spinner.getSelectedItemPosition() != 0);     // no es Ton.
                boolean velButtonsActive = (rapBtnAux || lentBtnAux || medBtnAux);      // cualquiera es true

                if (!spinnerIsActive || !velButtonsActive){
                    listaFiltrada = new ArrayList<>();
                }

                for(Coro coro: listaDeCoros) {
                    if (coro.nombre.toUpperCase().contains(s.toUpperCase()) || coro.sName.toLowerCase().contains(s.toLowerCase())){
                        listaFiltrada.add(coro);
                    }
                }

                for(Coro coro: listaFiltrada) {
                    listaAux.add(coro);
                }
                setupRecyclerView((RecyclerView) recyclerView, listaFiltrada);
                return false;
            }
        });
    }

    public void setButtons() {

        btnLentos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!lentBtnAux) {
                    btnLentos.setTextColor(0xFF7BAD3E);
                    velocidadesActivas.add("L");
                } else {
                    btnLentos.setTextColor(Color.WHITE);
                    velocidadesActivas.remove("L");
                }
                lentBtnAux = !lentBtnAux;
                LegibleText.setTonalidad(spinner.getSelectedItem().toString(), 0);
                filterContentForSearch(searchView.getQuery().toString(), LegibleText.getTonalidad());
            }
        });

        btnMedios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!medBtnAux) {
                    btnMedios.setTextColor(0xFF7BAD3E);
                    velocidadesActivas.add("M");
                } else {
                    btnMedios.setTextColor(Color.WHITE);
                    velocidadesActivas.remove("M");
                }
                medBtnAux = !medBtnAux;
                LegibleText.setTonalidad(spinner.getSelectedItem().toString(), 0);
                filterContentForSearch(searchView.getQuery().toString(), LegibleText.getTonalidad());
            }
        });

        btnRapidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!rapBtnAux) {
                    btnRapidos.setTextColor(0xFF7BAD3E);
                    velocidadesActivas.add("R");
                } else {
                    btnRapidos.setTextColor(Color.WHITE);
                    velocidadesActivas.remove("R");
                }
                rapBtnAux = !rapBtnAux;
                LegibleText.setTonalidad(spinner.getSelectedItem().toString(), 0);
                filterContentForSearch(searchView.getQuery().toString(), LegibleText.getTonalidad());
            }
        });
    }
}

