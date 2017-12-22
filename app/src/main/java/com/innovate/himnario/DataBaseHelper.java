package com.innovate.himnario;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.innovate.himnario.data.CELContract;
import com.innovate.himnario.data.Coros;
import com.innovate.himnario.data.MisListasContract;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = DataBaseHelper.class.getSimpleName();

	FirebaseDatabase database = Utils.getDatabase();
	DatabaseReference rootRef;
	DatabaseReference corosRef;

    //The Android's default system path of your application database.
    private static final String DB_PATH = "/data/data/com.innovate.himnario/databases/";
    private static final String DB_NAME = "corosDB.db";
	private static final int DATABASE_VERSION = 11;

	// NO OLVIDARSE de copiar db del folder de debug a release IMPORTANTISIMO

	/* Arreglos a letra de coros
	 * 04/01/2016 version 5
	 * 09/02/2016 version 6
	 * 18/03/2016 v7 Te Envio a Ti & El Señor Hara Volver -> agregados  V22
	 * 26/03/2016 v8 copiar base de datos de debug a release para V23
	 * 30/05/2016 v9 Su Nombre es Exaltado
	 * 16/09/2016 v10 El Amor de Dios y Tu Dios Reina
	 * 20/12/2017 v11 Actualizacion a v2.0.0
	 */

	public static final String TABLE_MAIN = Coros.CorosEntry.TABLE_NAME;
	public static final String TABLE_LISTAS = MisListasContract.MisListasEntry.TABLE_NAME;

	public static final String[] ALL_KEYS_COROS = new String[] {
			Coros.CorosEntry.COLUMN_ROWID,
			Coros.CorosEntry.COLUMN_ORDEN,
			Coros.CorosEntry.COLUMN_NOMBRE,
			Coros.CorosEntry.COLUMN_CUERPO,
			Coros.CorosEntry.COLUMN_TONALIDAD,
			Coros.CorosEntry.COLUMN_VELOCIDAD_LETRA,
			Coros.CorosEntry.COLUMN_TIEMPO,
			Coros.CorosEntry.COLUMN_MUSICA,
			Coros.CorosEntry.COLUMN_AUT_MUS,
			Coros.CorosEntry.COLUMN_AUT_LET,
			Coros.CorosEntry.COLUMN_CITA,
			Coros.CorosEntry.COLUMN_HISTORIA};

	public static final String[] ALL_KEYS_LISTAS = new String[]{
			MisListasContract.MisListasEntry.COLUMN_LIST_ID,
			MisListasContract.MisListasEntry.COLUMN_FECHA,
			MisListasContract.MisListasEntry.COLUMN_TON_RAP,
			MisListasContract.MisListasEntry.COLUMN_TON_LENT,
			MisListasContract.MisListasEntry.COLUMN_TON_GLOBAL,
			MisListasContract.MisListasEntry.COLUMN_NOM_FILE};

	public static final String[] ALL_KEYS_COROS_EN_LISTA = new String[]{
			CELContract.CELEntry.COLUMN_CORO_ID,
			CELContract.CELEntry.COLUMN_NOMBRE,
			CELContract.CELEntry.COLUMN_FILE_NAME,
			CELContract.CELEntry.COLUMN_VEL,
			CELContract.CELEntry.COLUMN_ORDEN};
 
    private SQLiteDatabase corosDB;
 
    private Context myContext;
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * param context
     */
    public DataBaseHelper(Context context) {
    	super(context, DB_NAME, null, DATABASE_VERSION);
        this.myContext = context;

		rootRef = database.getReference();
		corosRef = rootRef.child("coros");
    }
 
 	 /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase(boolean overrideExistanceCheck) throws IOException {

		boolean dbExist;		//just initializing
		if (overrideExistanceCheck) {
			dbExist = false;
		} else {
			dbExist = checkDataBase();
		}

 
    	if(!dbExist) {
			//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
			Log.v(LOG_TAG, "Does not exist");
			this.getWritableDatabase();					/// change to writeable datbase

			try {

				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
			//else do nothing - database already exist
		} else {
			Log.v(LOG_TAG, "DOES EXIST");
		}
 
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    	}catch(SQLiteException e){
    		//database does't exist yet.
			Log.e(LOG_TAG, "menseje2:::: "+ e.getMessage());
    	}
		Log.e(LOG_TAG,"checkDB: " + checkDB);
		boolean flag = checkDB != null;
    	if(flag){
    		checkDB.close();
		}

    	return flag;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[123904];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }
 
    public void openDataBase() throws SQLException {
    	//Open the database
		corosDB = this.getWritableDatabase();

    }
 
    @Override
	public synchronized void close() {
    	    if(corosDB != null)
    		    corosDB.close();
 		   	    super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion){
			try{
				//Open your local db as the input stream
				InputStream myInput = myContext.getAssets().open(DB_NAME);

				// Path to the just created empty db
				String outFileName = DB_PATH + DB_NAME;

				//Open the empty db as the output stream
				OutputStream myOutput = new FileOutputStream(outFileName);

				//transfer bytes from the inputfile to the outputfile
				byte[] buffer = new byte[123904];
				int length;
				while ((length = myInput.read(buffer))>0){
					myOutput.write(buffer, 0, length);
				}

				//Close the streams
				myOutput.flush();
				myOutput.close();
				myInput.close();
			} catch (IOException e){}
		}
	}

	// Return all data in the database. Coros
	public Cursor getAllRowsCoros(String where) {
		try{
			Cursor c = corosDB.query(TABLE_MAIN, ALL_KEYS_COROS, where, null, null, null, Coros.CorosEntry.COLUMN_ORDEN);
			if (c != null) {
				c.moveToFirst();
			}
			return c;
		}catch (Exception e){
			Log.e(LOG_TAG, e.getMessage());
		}
		return null;
	}

	// Get a specific row (by rowId)
	public Cursor getRowCoros(long rowId) {
		String where = Coros.CorosEntry.COLUMN_ROWID + "=" + rowId;
		Cursor c = 	corosDB.query(true, TABLE_MAIN, ALL_KEYS_COROS,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Get a specific row (by nombre)
	public Cursor getRowCoros(String nombre) {
		String where = Coros.CorosEntry.COLUMN_NOMBRE + "=\'" + nombre+"\'";
		Cursor c = 	corosDB.query(true, TABLE_MAIN, ALL_KEYS_COROS,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Get a specific row (by rowId)
	public Cursor getRowListas(long rowId) {
		String where = Coros.CorosEntry.COLUMN_ROWID + "=" + rowId;
		Cursor c = 	corosDB.query(true, TABLE_LISTAS, ALL_KEYS_LISTAS,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	// Return all data in the database.
	public Cursor getAllRowsListas(String where) {
		try{
			String orderBy =  "_id DESC";
			Cursor c = corosDB.query(TABLE_LISTAS, ALL_KEYS_LISTAS, where, null, null, null, orderBy);
			if (c != null) {
				c.moveToFirst();
			}
			return c;
		}catch (Exception e){
			Log.e(LOG_TAG, e.getMessage());
		}
		return null;
	}

	public Cursor getAllRowsCoroenLista(long listaID, String where){		// where usually refers to velocidad
		String TABLE_NAME = "Coros" + listaID;
		try{
			Cursor c = corosDB.query(TABLE_NAME, ALL_KEYS_COROS_EN_LISTA, where, null, null, null, CELContract.CELEntry.COLUMN_ORDEN);
			if (c != null) {
				c.moveToFirst();
			}
			return c;
		}catch (Exception e){
			Log.e(LOG_TAG, e.getMessage());
		}

		return null;
	}

	// get a specific song from lista by coroID
	public Cursor getRowCoroenLista(long listaID, long coroID){
		String TABLE_NAME = "Coros" + listaID;
		String where = CELContract.CELEntry.COLUMN_CORO_ID + "=\'" + coroID+"\'";
		Cursor c = 	corosDB.query(true, TABLE_NAME, ALL_KEYS_COROS_EN_LISTA,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// get a specific song from lista by orden
	public Cursor getRowCoroenLista(long listaID, long orden, String vel){
		String TABLE_NAME = "Coros" + listaID;
		String where = CELContract.CELEntry.COLUMN_ORDEN + "=\'" + orden + "\' AND " + CELContract.CELEntry.COLUMN_VEL + "=\'" + vel + "\'";
		Cursor c = 	corosDB.query(true, TABLE_NAME, ALL_KEYS_COROS_EN_LISTA,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	public boolean deleteRowCoroenLista(long listaID, long coroID){
		String TABLE_NAME = "Coros" + listaID;
		String where = CELContract.CELEntry.COLUMN_CORO_ID + "=" + coroID;

		Cursor cursor = getRowCoroenLista(listaID, coroID);
		String velActual = cursor.getString(CELContract.CELEntry.KEY_VEL);
		Long ordenActual = cursor.getLong(CELContract.CELEntry.KEY_ORDEN);
		String w1 = CELContract.CELEntry.COLUMN_VEL + "=\'" + velActual + "\' AND " + CELContract.CELEntry.COLUMN_ORDEN +  ">\'" + ordenActual + "\'";
		cursor = getAllRowsCoroenLista(listaID, w1);

		long cont = 0;
		String w2 = "";
		ContentValues values = new ContentValues();
		if (cursor.getCount() != 0){
			while(!cursor.isLast()){
				w2 = CELContract.CELEntry.COLUMN_ORDEN + "=" + cursor.getLong(CELContract.CELEntry.KEY_ORDEN)
						+ " AND " + CELContract.CELEntry.COLUMN_VEL + "=\'" + cursor.getString(CELContract.CELEntry.KEY_VEL) + "\'";
				values.put(CELContract.CELEntry.COLUMN_ORDEN, ordenActual + cont);
				corosDB.update(TABLE_NAME, values, w2, null);
				cursor.moveToNext();
				cont++;
			}
			w2 = CELContract.CELEntry.COLUMN_ORDEN + "=" + cursor.getLong(CELContract.CELEntry.KEY_ORDEN)
					+ " AND " + CELContract.CELEntry.COLUMN_VEL + "=\'" + cursor.getString(CELContract.CELEntry.KEY_VEL) + "\'";
			values.put(CELContract.CELEntry.COLUMN_ORDEN, ordenActual + cont);
			corosDB.update(TABLE_NAME, values, w2, null);
		}

		return corosDB.delete(TABLE_NAME, where, null) != 0;
	}

	// elimina la lista
	public boolean deleteRowListas(long rowId, String filename){
		String where = Coros.CorosEntry.COLUMN_ROWID + "=" + rowId;
		Cursor cursor = getRowListas(rowId);
		deleteListaTable(cursor.getInt((MisListasContract.MisListasEntry.KEY_ROWID)));		//eliminando su tabla asociada

		//delete text file
	/*	if (filename != null){		// null when I have to delete every file
			myContext.deleteFile(filename);
		} else {
			Cursor c = corosDB.query(MisListasContract.MisListasEntry.TABLE_NAME, ALL_KEYS_LISTAS, null, null, null, null, null);
			for (int i = 0; i < c.getCount(); i++){
				int filenameAux = c.getInt(MisListasContract.MisListasEntry.KEY_NOM_FILE);
				filename = filenameAux + "";
				myContext.deleteFile(filename);
			}
		}*/
		return corosDB.delete(TABLE_LISTAS, where, null) != 0;
	}

	public void deleteAllRowsListas() {
		Cursor c = getAllRowsListas(null);
		long rowId = c.getColumnIndexOrThrow(Coros.CorosEntry.COLUMN_ROWID);
		if (c.moveToFirst()){
			do {
				deleteRowListas(c.getLong((int) rowId), null);
			} while (c.moveToNext());
		}
		c.close();
	}


	public void createListaTable(long listaID){			// tabla asociada a cada lista
		deleteListaTable(listaID);
		String TABLE_NAME = "Coros" + listaID;
		// Table Create CorosEnLista
		final String CREATE_TABLE_COROS_EN_LISTA = "CREATE TABLE "
				+ TABLE_NAME + "(" + CELContract.CELEntry.COLUMN_CORO_ID + " INTEGER PRIMARY KEY,"
				+ CELContract.CELEntry.COLUMN_NOMBRE + " TEXT,"
				+ CELContract.CELEntry.COLUMN_FILE_NAME + " TEXT,"
				+ CELContract.CELEntry.COLUMN_VEL + " TEXT,"
				+ CELContract.CELEntry.COLUMN_ORDEN + " TEXT" + ")";
		Log.v(LOG_TAG, CREATE_TABLE_COROS_EN_LISTA);

		corosDB.execSQL(CREATE_TABLE_COROS_EN_LISTA);
	}

	// borra la tabla de la lista
	public void deleteListaTable(long listaID){
		String TABLE_NAME = "Coros" + listaID;
		final String DELETE_TABLE_COROS_EN_LISTA = "DROP TABLE IF EXISTS " + TABLE_NAME;
		corosDB.execSQL(DELETE_TABLE_COROS_EN_LISTA);
	}

	public boolean agregarCoroALista(final long listaID, final long coroID, final boolean colocacionMedio){
		final String TABLE_NAME = "Coros" + listaID;
		final ContentValues values = new ContentValues();
		// Casos especiales de coros medios
		final int[] mediosEspeciales = {1001,82,261,1019,1012,84,174,1006,1008,5,338,360};

		DatabaseReference coroRef = corosRef.child(Long.toString(coroID));
		//final Query corosQuery = corosRef.orderByChild("orden");	//no pedir por orden sino directo el id
		//corosQuery.addListenerForSingleValueEvent(new ValueEventListener() {
		coroRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				//ListView setup
				int iteration = 1;
				String vel_let = "";
				String sName = "";
				for(DataSnapshot coroSnapshot: dataSnapshot.getChildren()) {
					if (iteration == 7) {		// when snapshot is in iteration #7 I get the name.
						values.put(CELContract.CELEntry.COLUMN_NOMBRE, coroSnapshot.getValue().toString());
					} else if (iteration == 10) {		// sName
						sName = coroSnapshot.getValue().toString();
						String easyFileName = sName.replace(" ", "_");
						values.put(CELContract.CELEntry.COLUMN_FILE_NAME, easyFileName);
					} else if (iteration == 14) {		// Velocidad en letra
						vel_let = coroSnapshot.getValue().toString();
					}

					values.put(CELContract.CELEntry.COLUMN_CORO_ID, coroID);

					String whereLent = CELContract.CELEntry.COLUMN_VEL + "='L'";
					String whereRap = CELContract.CELEntry.COLUMN_VEL + "='RM'";
					final Cursor cursorLentos = getAllRowsCoroenLista(listaID, whereLent);
					final Cursor cursorRapidos = getAllRowsCoroenLista(listaID, whereRap);

					if (vel_let.equals("L")){
						values.put(CELContract.CELEntry.COLUMN_ORDEN, cursorLentos.getCount()+1);
						values.put(CELContract.CELEntry.COLUMN_VEL, vel_let);
					} else if(vel_let.equals("M")){

						boolean isMedioEspecial = false;

						for(int k = 0; k < mediosEspeciales.length; k++){
							if (coroID == mediosEspeciales[k]){
								isMedioEspecial = true;
							}
						}

						if(colocacionMedio || isMedioEspecial){
							values.put(CELContract.CELEntry.COLUMN_ORDEN, cursorLentos.getCount()+1);
							values.put(CELContract.CELEntry.COLUMN_VEL, "L");
						} else {
							values.put(CELContract.CELEntry.COLUMN_ORDEN, cursorRapidos.getCount()+1);
							values.put(CELContract.CELEntry.COLUMN_VEL, "RM");
						}
					} else {
						values.put(CELContract.CELEntry.COLUMN_ORDEN, cursorRapidos.getCount() + 1);
						values.put(CELContract.CELEntry.COLUMN_VEL, "RM");
					}
					iteration += 1;
				}
				corosDB.insert(TABLE_NAME, null, values);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

		return true;
	}

	//editar orden en lista
	// flag = 0 down -- flag = 1 up
	public boolean editOrdenListas(long listaID, final long coroID, long flag, String RML){
		final String TABLE_NAME = "Coros" + listaID;
		String whereActual = CELContract.CELEntry.COLUMN_CORO_ID + "=" + coroID;

		ContentValues valuesActual = new ContentValues();
		ContentValues valuesAux = new ContentValues();
		Cursor cursor;
		Cursor cursorCoro = getRowCoroenLista(listaID, coroID);
		String whereL = CELContract.CELEntry.COLUMN_VEL + "='L'";
		String whereRM = CELContract.CELEntry.COLUMN_VEL + "='RM'";
		if (RML.equals("L")){
			cursor = getAllRowsCoroenLista(listaID, whereL);
		} else {
			cursor = getAllRowsCoroenLista(listaID, whereRM);
		}

		long posActual = cursorCoro.getLong(CELContract.CELEntry.KEY_ORDEN);

		if (flag == 0){		// down
			if (posActual != cursor.getCount()){
				valuesActual.put(CELContract.CELEntry.COLUMN_ORDEN, posActual + 1);
				cursorCoro = getRowCoroenLista(listaID, (posActual+1), RML);
				valuesAux.put(CELContract.CELEntry.COLUMN_ORDEN, posActual);
			}
		} else {			// up
			if (posActual != 1){
				valuesActual.put(CELContract.CELEntry.COLUMN_ORDEN, posActual - 1);
				cursorCoro = getRowCoroenLista(listaID, posActual-1, RML);
				valuesAux.put(CELContract.CELEntry.COLUMN_ORDEN, posActual);
			}
		}

		if(posActual != cursor.getCount() && flag == 0 || posActual != 1 && flag == 1){
			String whereNext = CELContract.CELEntry.COLUMN_CORO_ID + "=" + cursorCoro.getLong(CELContract.CELEntry.KEY_CORO_ID);
			corosDB.update(TABLE_NAME, valuesActual, whereActual, null);
			corosDB.update(TABLE_NAME, valuesAux, whereNext, null);
			return true;
		}

		return false;
	}

	// for finding next coro down or up
	// flag = 0 down -- flag = 1 up
	// returns orden of next coro
	public long getNextCoro(Cursor c, long flag, long posActual) {
		if (flag == 0){			// down
			long down = posActual;
			int cont = 0;
			if (c.getCount() != 0){
				while(!c.isLast()){
					long orden = c.getLong(CELContract.CELEntry.KEY_ORDEN);
					if (orden > down) {
						Log.v(LOG_TAG, orden + "/" + down);
						if (cont == 0){
							down = orden;
						}
					}
					cont++;
					c.moveToNext();
				}

				Log.v(LOG_TAG, posActual + "-" + down);
			}
			return down;
		} else if (flag == 1){		// up
			long up = posActual;
			long ordenAux = -5;
			if (c.getCount() != 0){
				while(!c.isLast()){
					long orden = c.getLong(CELContract.CELEntry.KEY_ORDEN);
					if (orden < up){
						ordenAux = orden;
						Log.v(LOG_TAG, orden + "/" + up);

					}
					c.moveToNext();
				}
			}
			return ordenAux;
		}

		return -1;
	}

	public boolean updateTonListas(long rowIdListas, String tonalidad) {
		String where = MisListasContract.MisListasEntry.COLUMN_LIST_ID + "=" + rowIdListas;

		// Create row's data:
		/*ContentValues newValues = new ContentValues();
		newValues.put(MisListasContract.MisListasEntry.COLUMN_TON_, tonalidad);


		// Insert it into the database.
		return corosDB.update(TABLE_LISTAS, newValues, where, null) != 0;*/
		return false;
	}
}