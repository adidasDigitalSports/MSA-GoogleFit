package com.example.adidas.googlefit.fragments;



import com.example.adidas.googlefit.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class CustomIntentSampleFragment extends Fragment {

    private static final String TAG = CustomIntentSampleFragment.class.getSimpleName();

    private static final int ADIDAS_MSA_REQUEST_CODE = 100;
    private static final int ADIDAS_MSA_RESULT_SYNC_OK = Activity.RESULT_OK;
    private static final int ADIDAS_MSA_RESULT_SYNC_FAILED = Activity.RESULT_FIRST_USER;
    private static final int ADIDAS_MSA_RESULT_NOT_ENABLED = Activity.RESULT_FIRST_USER + 1;
    private static final String ADIDAS_MSA_RESULT_EXTRA_MESSAGE = "message";

    private Button mLaunchMiCoachBtn;

    public static CustomIntentSampleFragment newInstance() {
        return new CustomIntentSampleFragment();
    }

    public CustomIntentSampleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_custom_intent_sample, container, false);
        mLaunchMiCoachBtn = (Button) view.findViewById(R.id.launchMiCoachApp);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        activity.getActionBar().setTitle(
                getString(R.string.nav_drawer_item_sync_data));

        mLaunchMiCoachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(getString(R.string.micoach_multisport_package_name) + ".sync");

                if (!isPackageInstalled(getString(R.string.micoach_multisport_package_name),
                        activity)) {
                    promptInstallMiCoach(activity);
                    return;
                }
                try {
                    startActivityForResult(i, ADIDAS_MSA_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(activity, activity.getString(R.string.launch_app_sync_intent_not_available),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void showMiCoachInstallScreen(Context context) {

        Uri uri = Uri.parse(
                "market://search?q=pname:" + getString(R.string.micoach_multisport_package_name));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.launch_app_play_store_missing_msg),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ADIDAS_MSA_REQUEST_CODE != requestCode) {
            return;
        }

        String message = null != data && data.hasExtra(ADIDAS_MSA_RESULT_EXTRA_MESSAGE)
                ? data.getStringExtra(ADIDAS_MSA_RESULT_EXTRA_MESSAGE)
                : null;

        if (message == null) {
            return;
        }

        switch (resultCode) {
            case ADIDAS_MSA_RESULT_SYNC_OK: {
                Log.d(TAG, "Sync succeeded: " + message);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
                        .show();
                break;
            }
            case ADIDAS_MSA_RESULT_SYNC_FAILED: {
                Log.d(TAG, "Sync failed: " + message);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
                        .show();
                break;
            }
            case ADIDAS_MSA_RESULT_NOT_ENABLED: {
                Log.d(TAG, "Not authenticated: " + message);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
                        .show();
                break;
            }
        }

    }

    private void promptInstallMiCoach(final Context context) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder
                .setTitle(context.getString(R.string.custom_intent_dialog_title));

        alertDialogBuilder
                .setMessage(R.string.custom_intent_dialog_message)
                .setCancelable(false)
                .setPositiveButton(
                        R.string.custom_intent_dialog_positive_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showMiCoachInstallScreen(context);
                            }
                        })
                .setNegativeButton(R.string.custom_intent_dialog_negative_button,
                        null)
                .setCancelable(true);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}

