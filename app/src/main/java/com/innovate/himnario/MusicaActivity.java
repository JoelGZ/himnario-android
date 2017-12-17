package com.innovate.himnario;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.innovate.himnario.data.Coro;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;


public class MusicaActivity extends ActionBarActivity implements MediaController.MediaPlayerControl{
    private static final String LOG_TAG = MusicaActivity.class.getSimpleName();

    public MediaPlayer mPlayer;
    private MediaController mController;
    public ImageButton controls;
    private ImageView imageView;
    private PhotoViewAttacher mAttacher;
    private int orientation;
    private Coro coro;
    private File audio;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mCorosRef = mRootRef.child("coros");
    private DatabaseReference coroRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_musica);

        imageView = (ImageView)findViewById(R.id.partitura);
        controls = (ImageButton)findViewById(R.id.showControlsBtn);

        //Screen awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("CORO")){
            coro = intent.getParcelableExtra("CORO");
        }

        //Firebase references
        coroRef = mCorosRef.child(Integer.toString(coro.id));

        String nomCoro = coro.nombre;
        setTitle(nomCoro);

        //Partitura setup
        String partituraStr = coro.sName.replace(" ", "_") + ".jpg";
        File file = new File(getFilesDir() + "/" + partituraStr);
        if (file.exists()) {
            Glide.with(getApplicationContext()).load(file).into(imageView);
        } else {
            DatabaseReference mPartituraRef = coroRef.child("partitura");
            mPartituraRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String scoreURL = dataSnapshot.getValue(String.class);
                    Glide.with(getApplicationContext()).load(scoreURL).into(imageView);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        mAttacher = new PhotoViewAttacher(imageView);

        orientation = getResources().getConfiguration().orientation;
        if (orientation == 2){              // if phone is in landscape
            mAttacher.setMaximumScale((float)3.5);
            mAttacher.setMediumScale((float)3.0);
            mAttacher.setMinimumScale((float)2.5);
            mAttacher.setScale((float) 2.5, 120, 0, true);
            Log.v(LOG_TAG, "getScale: " + mAttacher.getScale());
        } else {
            mAttacher.setMinimumScale((float)1.0);
        }

        mAttacher.update();

        // Audio setup
        String audioStr = coro.sName.replace(" ", "_") + ".mp3";
        String audioPath = getFilesDir() + "/" + audioStr;
        audio = new File(audioPath);
        if (audio.exists()) {       // if audio has been downloaded.. else read from internet
            try {
                setAudio(audioPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            new InternetAudioSetup().execute("");
        }

    }

    private class InternetAudioSetup extends AsyncTask<String, Void, String> {
        long audioDidSetFlag = 0;

        @Override
        protected String doInBackground(String... params) {
            DatabaseReference mAudioRef = coroRef.child("audio");

            mAudioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String text = dataSnapshot.getValue(String.class);
                    try {
                        setAudio(text);
                        audioDidSetFlag = 1;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            while (audioDidSetFlag == 0) {      //if audio is not set stop the thread from finishing, this is to avoid mController having a NullPointerException on line 286
                try{
                    Thread.sleep(500);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }


        ///TODO: see if you have to change this
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onPrepared();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_musica, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageView.setImageDrawable(null);
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayer != null && !audio.exists()) {
            DatabaseReference mAudioRef = coroRef.child("audio");
            mAudioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String text = dataSnapshot.getValue(String.class);
                    String audioStringURL = text;
                    try {
                        setAudio(audioStringURL);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MusicaActivity.this, "error", Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mPlayer != null && mController != null){
            mController.hide();
            mPlayer.stop();
            mPlayer.release();
        }
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
        }*/

        return super.onOptionsItemSelected(item);
    }



    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    public void onClick_Controls(View view){
        if (!audio.exists()) {
            if (!isOnline()){
                Toast.makeText(this, "No hay conexión al internet. Esta función solo esta disponible en linea.", Toast.LENGTH_LONG).show();
            } else {
                if (mController != null) {
                    if (mController.isShowing()){
                        mController.hide();
                    } else {
                        mController.show(0);
                    }
                } else {
                    Toast.makeText(this, "Conexión de internet lenta. Intente mas tarde.", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            if (mController != null) {
                if (mController.isShowing()){
                    mController.hide();
                } else {
                    mController.show(0);
                }
            }
        }
    }


    public void setAudio(String url) throws Exception {
        mPlayer = new MediaPlayer();
        mPlayer.setDataSource(url);
        mPlayer.prepare();
        onPrepared();
    }

    public void onPrepared() {
        mController = new MediaController(this);
        mController.setMediaPlayer(this);
        mController.setAnchorView(findViewById(R.id.musica_layout));
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    // controller methods
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
}