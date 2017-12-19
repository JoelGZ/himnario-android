package com.innovate.himnario;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.innovate.himnario.data.CELContract;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Joel on 14-Jul-16.
 */
public class SwipeAdapter extends PagerAdapter {
    private static final String LOG_TAG = SwipeAdapter.class.getSimpleName();
    private ArrayList<File> partitura_files =  new ArrayList<>();
    private ArrayList<DatabaseReference> audio_references = new ArrayList<>();
    private MediaPlayer[] playlist;
    //private String[] playlist;
    private long listaID;
    final private Context context;
    private DataBaseHelper myDbHelper;
    private PhotoViewAttacher mAttacher;
    private int orientation;
    private ViewGroup layout;

    private MediaPlayer mPlayer;
    private MediaController mController;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mCorosRef = mRootRef.child("coros");
    private DatabaseReference coroRef;
    DatabaseReference mAudioRef;

    public SwipeAdapter(Context c, long listaID) {
        this.listaID = listaID;
        this.context = c;

        //Connecting to database
        myDbHelper = new DataBaseHelper(context);
        try {
            myDbHelper.openDataBase();
        } catch (Exception e) {
            throw new Error("Unable to create database");
        }

        String WHERERM = CELContract.CELEntry.COLUMN_VEL + "='RM'";
        String WHEREL = CELContract.CELEntry.COLUMN_VEL + "='L'";

        Cursor rapidosCursor = myDbHelper.getAllRowsCoroenLista(listaID, WHERERM);
        Cursor lentosCursor = myDbHelper.getAllRowsCoroenLista(listaID, WHEREL);

        int arrayLength = rapidosCursor.getCount() + lentosCursor.getCount();
        playlist = new MediaPlayer[arrayLength];

        if (rapidosCursor.getCount() != 0) {
            for (int h = 1; h <= rapidosCursor.getCount(); h++) {
                long coroID = rapidosCursor.getLong(CELContract.CELEntry.KEY_CORO_ID);
                String fileName = rapidosCursor.getString(CELContract.CELEntry.KEY_FILE_NAME) + ".jpg";
                coroRef = mCorosRef.child(String.valueOf(coroID));

                File partituraFile = new File(context.getFilesDir() + "/" + fileName);
                if (partituraFile.exists()) {
                    partitura_files.add(partituraFile);
                } else {
                    Toast.makeText(context, "Partitura no disponible", Toast.LENGTH_SHORT).show();
                }

                mAudioRef = coroRef.child("audio");
                audio_references.add(mAudioRef);

                rapidosCursor.moveToNext();
            }
        }

        if (lentosCursor.getCount() != 0) {
            for (int h = 1; h <= lentosCursor.getCount(); h++) {
                long coroID = lentosCursor.getLong(CELContract.CELEntry.KEY_CORO_ID);
                String fileName = lentosCursor.getString(CELContract.CELEntry.KEY_FILE_NAME) + ".jpg";

                coroRef = mCorosRef.child(String.valueOf(coroID));

                File partituraFile = new File(context.getFilesDir() + "/" + fileName);
                if (partituraFile.exists()) {
                    partitura_files.add(partituraFile);
                } else {
                    Toast.makeText(context, "Partitura no disponible", Toast.LENGTH_SHORT).show();
                }

                mAudioRef = coroRef.child("audio");
                audio_references.add(mAudioRef);

                lentosCursor.moveToNext();
            }
        }
    }

    public void onBackPressed() {
        if(mPlayer != null && mController != null){
            mController.hide();
            if (mPlayer.isPlaying()) {
                try{
                    mPlayer.stop();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
            mPlayer.reset();
            mPlayer = null;
        }
    }

    public void stopAudio(){
        if(mPlayer != null) {
            if (mPlayer.isPlaying()) {
                try{
                    mPlayer.stop();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
            mPlayer.reset();
        }
    }
    @Override
    public int getCount() {
        return partitura_files.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        layout = (ViewGroup) layoutInflater.inflate(R.layout.swipe_adapter, container, false);
        ImageView partitura_image = (ImageView) layout.findViewById(R.id.partitura_image);
//        ImageButton audioControls = (ImageButton) layout.findViewById(R.id.showControlsBtn);

/*        audioControls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnline()){
                    Toast.makeText(context, "No hay conexión al internet. Esta función solo esta disponible en linea.", Toast.LENGTH_LONG).show();
                } else {
                    new AudioSetup().execute(position);
                    if (mPlayer != null) {
                        if (mPlayer.isPlaying()) {
                            if (mController.isShowing()) {
                                mController.hide();
                            } else {
                                mController.show(0);
                            }
                        } else {

                            onPrepared();
                            if (mController.isShowing()) {
                                mController.hide();
                            } else {
                                mController.show(0);
                            }
                        }
                    }
                }
            }
        });*/

        mAttacher = new PhotoViewAttacher(partitura_image);
        Glide.with(context).load(partitura_files.get(position)).into(partitura_image);
        orientation = context.getResources().getConfiguration().orientation;
        if (orientation == 2){              // if phone is in landscape
            mAttacher.setMaximumScale((float)3.5);
            mAttacher.setMediumScale((float)3.0);
            mAttacher.setMinimumScale((float)2.5);
            mAttacher.setScale((float) 2.6, 120, 0, true);
        } else {
            mAttacher.setMinimumScale((float)1.0);
        }

        mAttacher.update();

        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
        /*if(mPlayer != null && mController != null){
            mController.hide();
            if (mPlayer.isPlaying()) {
                try{
                    mPlayer.stop();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }

            mPlayer.reset();
        }*/
    }
/*
    private class AudioSetup extends AsyncTask<Integer, Void, String> {
        long audioDidSetFlag = 0;

        @Override
        protected String doInBackground(final Integer... params) {
            final int position = params[0];
            audio_references.get(position).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String text = dataSnapshot.getValue(String.class);
                    try {
                        setAudio(text, position);
                        audioDidSetFlag = 1;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return null;
        }
    }

    public class SlowConnectionCheck extends AsyncTask<String, Void, String> {
        boolean flag = true;

        @Override
        protected String doInBackground(String... params) {
                try {
                    Thread.sleep(2000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                if (mController == null) {
                    flag = false;
                }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!flag) {
                Toast.makeText(context, "Conexión de internet lenta. Intente mas tarde.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setAudio(String url, int position) throws Exception {
        mPlayer = new MediaPlayer();
        mPlayer.setDataSource(url);
        mPlayer.prepare();
    }

    public void onPrepared() {
        mController = new MediaController(this.context);
        mController.setMediaPlayer(this);
        mController.setAnchorView(layout);
    }

    @Override
    public void start() {
        mPlayer.start();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayer.start();
            }
        });
    }

    @Override
    public void pause() {
        mPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() { return mPlayer.isPlaying();}

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return mPlayer.getAudioSessionId();
    }
    */
}
