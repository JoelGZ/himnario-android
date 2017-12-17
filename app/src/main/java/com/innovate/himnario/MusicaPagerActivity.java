package com.innovate.himnario;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.WindowManager;

import com.viewpagerindicator.CirclePageIndicator;

/**
 * Created by Joel on 14-Jul-16.
 */
public class MusicaPagerActivity extends Activity{
    private static final String LOG_TAG = MusicaPagerActivity.class.getSimpleName();
    private static final String INTENT_EXTRA_LISTID = "LIST_ID";
    private static final String INTENT_EXTRA_ORDEN = "ORDEN_CORO";
    private long listaID;
    private int ordenDelCoro;
    private SwipeAdapter swipeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicapager);

        //Screen awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra((INTENT_EXTRA_LISTID))){
                listaID = intent.getLongExtra((INTENT_EXTRA_LISTID), -1);
            }

            if (intent.hasExtra(INTENT_EXTRA_ORDEN)) {
                ordenDelCoro = intent.getIntExtra(INTENT_EXTRA_ORDEN, 1);
            }

        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == 1) {         // portrait
            viewPager.setPageMargin(20);
        } else {
            viewPager.setPageMargin(5);
        }

        Log.v(LOG_TAG, listaID+"");
        swipeAdapter = new SwipeAdapter(this, listaID);
        viewPager.setAdapter(swipeAdapter);
        viewPager.setCurrentItem(ordenDelCoro);
        Log.v(LOG_TAG, "ordendelcoro: " + ordenDelCoro);
       /* viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //swipeAdapter.stopAudio();
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/

        //Bind the title indicator to the adapter
        CirclePageIndicator underlinePageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        underlinePageIndicator.setViewPager(viewPager);
    }

    @Override
    protected void onStop() {
        super.onStop();
        swipeAdapter.onBackPressed();
    }

}
