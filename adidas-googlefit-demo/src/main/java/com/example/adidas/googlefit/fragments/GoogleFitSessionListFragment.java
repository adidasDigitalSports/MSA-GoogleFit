package com.example.adidas.googlefit.fragments;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.google.android.gms.fitness.result.SessionReadResult;

import com.example.adidas.googlefit.DemoConstants;
import com.example.adidas.googlefit.R;
import com.example.adidas.googlefit.activities.GoogleFitSessionDetailActivity;
import com.example.adidas.googlefit.adapters.GoogleFitSessionAdapter;
import com.example.adidas.googlefit.models.DemoApplication;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GoogleFitSessionListFragment extends GoogleFitConnectedFragment
        implements AdapterView.OnItemClickListener {

    private static final String TAG = GoogleFitSessionListFragment.class.getSimpleName();
    private static final String DATATYPE_NAME_QUICKNESS = "com.adidas.quickness";
    private static final String DATATYPE_NAME_JUMPHEIGHT = "com.adidas.jump.height";
    private static final String DATATYPE_NAME_HUSTLE = "com.adidas.hustle";
    private DemoApplication mDemoApplication;
    private List<Session> mSessions = new ArrayList<Session>();
    private Map<String, List<DataSet>> mSessionDataSets = new HashMap<String, List<DataSet>>();
    private ListView mListView;
    private GoogleFitSessionAdapter mSessionAdapter;
    private DataType dataTypeQuickness;
    private DataType dataTypeJumpHeight;
    private DataType dataTypeHustle;
    private Activity mActivity;

    public GoogleFitSessionListFragment() {
        // Required empty public constructor
    }

    public static GoogleFitSessionListFragment newInstance() {
        return new GoogleFitSessionListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mSessions = new ArrayList<Session>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(android.R.layout.list_content, container, false);
        mListView = (ListView) v.findViewById(android.R.id.list);
        mListView.setBackgroundColor(getResources().getColor(R.color.adidas_white_background));
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();
        mActivity.getActionBar().setTitle(
                getString(R.string.nav_drawer_item_display_data));

        mDemoApplication = (DemoApplication) mActivity.getApplication();

        if (mSessions == null) {
            // Create the Array List of nearby items for the listview
            mSessions = new ArrayList<Session>();
        }
        if (mSessionDataSets == null) {
            mSessionDataSets = new HashMap<String, List<DataSet>>();
        }

        mSessions = mDemoApplication.getSessions();
        mSessionDataSets = mDemoApplication.getSessionDataSets();

        if (mSessionAdapter == null) {
            mSessionAdapter = new GoogleFitSessionAdapter(mActivity, mSessions);
        }

        mListView.setAdapter(mSessionAdapter);
        mListView.setOnItemClickListener(this);

    }

    public void invokeFitnessAPIs() {
        // Request shareable data types
        Log.d(TAG, "Requesting Google Fit data!");
        new DataTypesRequest().execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mSessions == null || mSessions.isEmpty()) {
            Log.d(TAG, "Session list is empty!");
            return;
        }
        Intent intent = new Intent(mActivity, GoogleFitSessionDetailActivity.class);
        intent.putExtra(DemoConstants.ADIDAS_DEMO_GFIT_SESSION_ID,
                mSessions.get(position).getIdentifier());
        startActivity(intent);
    }

    private void readSession() {
        Log.d(TAG, "readSession() called!");
        long DAY_IN_MS = TimeUnit.DAYS.toMillis(100);
        Date now = new Date();
        // Set a range of the day, using a start time of 7 days before this moment.
        long endTime = now.getTime();
        long startTime = endTime - DAY_IN_MS;

        SessionReadRequest request = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(dataTypeJumpHeight)
                .read(dataTypeHustle)
                .read(dataTypeQuickness)
                .read(DataType.TYPE_HEART_RATE_BPM)
                .readSessionsFromAllApps()
                .build();

        PendingResult<SessionReadResult> pendingResult =
                Fitness.SessionsApi.readSession(mClient, request);

        // 3. Check the result
        pendingResult.setResultCallback(
                new ResultCallback<SessionReadResult>() {
                    @Override
                    public void onResult(SessionReadResult sessionReadResult) {
                        // Get a list of sessions that match the criteria
                        Log.d(TAG, "Sessions found: " + sessionReadResult.getSessions().size());

                        mSessions = sessionReadResult.getSessions();
                        mDemoApplication.setSessions(mSessions);
                        for (Session session : mSessions) {
                            String sessionName = session.getName();
                            Log.d(TAG, "Session: " + sessionName);

                            // Get the currentSessionDataSets for the time interval of this session
                            List<DataSet> currentSessionDataSets = sessionReadResult.getDataSet(
                                    session);
                            mSessionDataSets.put(session.getIdentifier(), currentSessionDataSets);
                        }
                        mDemoApplication.setSessionDataSets(mSessionDataSets);
                        Log.d(TAG, "The number of sessions is: " + mSessions.size());
                        mSessionAdapter.setSessions(mSessions);
                        mSessionAdapter.notifyDataSetChanged();
                    }
                }
        );
    }

    private class DataTypesRequest extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "doInBackground() called!");
            DataTypeResult quicknessResult = Fitness.ConfigApi.readDataType(
                    mClient, DATATYPE_NAME_QUICKNESS).await();
            DataTypeResult hustleResult = Fitness.ConfigApi.readDataType(
                    mClient, DATATYPE_NAME_HUSTLE).await();
            DataTypeResult jumpHeightResult = Fitness.ConfigApi.readDataType(
                    mClient, DATATYPE_NAME_JUMPHEIGHT).await();

            boolean result = quicknessResult.getStatus().isSuccess() && hustleResult.getStatus()
                    .isSuccess() && jumpHeightResult.getStatus().isSuccess();

            if (result) {
                Log.d(TAG, "All data types retrieved successfully!");
                dataTypeQuickness = quicknessResult.getDataType();
                dataTypeHustle = hustleResult.getDataType();
                dataTypeJumpHeight = jumpHeightResult.getDataType();
                // Request Google Fit data
                readSession();
            } else {
                Log.e(TAG, "Unable to retrieve Google Fit shareable data types!");
            }
            return null;
        }
    }
}
