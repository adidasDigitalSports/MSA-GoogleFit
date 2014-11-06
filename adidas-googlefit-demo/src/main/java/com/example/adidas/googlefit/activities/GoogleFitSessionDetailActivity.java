package com.example.adidas.googlefit.activities;

import com.google.android.gms.fitness.data.Session;

import com.example.adidas.googlefit.DemoConstants;
import com.example.adidas.googlefit.R;
import com.example.adidas.googlefit.fragments.GoogleFitSessionDetailFragment;
import com.example.adidas.googlefit.models.DemoApplication;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class GoogleFitSessionDetailActivity extends Activity {

    private Session mSession;
    private String mSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gfit_session_detail);

        if (savedInstanceState == null) {
            Intent i = getIntent();
            if (i.hasExtra(DemoConstants.ADIDAS_DEMO_GFIT_SESSION_ID)) {
                mSessionId = getIntent().getStringExtra(DemoConstants.ADIDAS_DEMO_GFIT_SESSION_ID);
            }
        } else {
            mSessionId = savedInstanceState.getString(DemoConstants.ADIDAS_DEMO_GFIT_SESSION_ID);
        }

        mSession = ((DemoApplication) getApplication()).getSession(mSessionId);
        getFragmentManager().beginTransaction()
                .add(R.id.container, GoogleFitSessionDetailFragment.newInstance(mSession
                )).commit();

        ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mSession.getName());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(DemoConstants.ADIDAS_DEMO_GFIT_SESSION_ID, mSessionId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
