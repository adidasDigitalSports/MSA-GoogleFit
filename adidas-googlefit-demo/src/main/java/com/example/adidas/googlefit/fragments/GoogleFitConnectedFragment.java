package com.example.adidas.googlefit.fragments;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

public abstract class GoogleFitConnectedFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = GoogleFitConnectedFragment.class.getSimpleName();

    private static final int REQUEST_RESOLUTION = 0x1;
    private static final String AUTH_PENDING = "auth_state_pending";

    protected GoogleApiClient mClient = null;
    // boolean to track whether the app is already resolving an error
    private boolean authInProgress = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called!");
        if (requestCode == REQUEST_RESOLUTION) {
            if (resultCode == Activity.RESULT_OK) {
                authInProgress = false;
                // Make sure the app is not already connected or attempting to connect.
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Optional: disable any UI components (if needed) due to user cancelling OAuth
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mClient == null || !mClient.isConnected()) {
            connectFitness();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mClient != null && mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed(): " + result.toString());

        if (!result.hasResolution()) {
            // Show the localized error dialog
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                    getActivity(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization dialog is displayed to the user.
        if (!authInProgress) {
            try {
                Log.d(TAG, "Attempting to resolve failed connection");
                authInProgress = true;
                result.startResolutionForResult(getActivity(),
                        REQUEST_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG,
                        "Exception while starting resolution activity", e);
            }
        } else {
            authInProgress = false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected!");

        invokeFitnessAPIs();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // If your connection gets lost at some point,
        // you'll be able to determine the reason and react to it here.
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.d(TAG, "Connection lost.  Cause: Network Lost.");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.d(TAG, "Connection lost.  Reason: Service Disconnected");

        }
    }

    protected abstract void invokeFitnessAPIs();

    private void connectFitness() {
        Log.d(TAG, "Connecting...");

        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(getActivity())
                // select the Fitness API
                .addApi(Fitness.API)
                        // specify the scopes of access
                .addScope(Fitness.SCOPE_ACTIVITY_READ)
                .addScope(Fitness.SCOPE_BODY_READ)
                        // provide callbacks
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Connect the Google API client
        mClient.connect();
    }
}
