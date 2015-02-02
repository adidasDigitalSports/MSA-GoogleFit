# Introduction

This demo app is intended to showcase how third-party developers can integrate their Android app with [miCoach MultiSport](https://play.google.com/store/apps/details?id=com.adidas.micoach.x_cell) and [Google Fit](https://developers.google.com/fit/) for the [Google Fit Developer Challenge](https://developers.google.com/fit/challenge).  Specifically, with this demo app you will be able to achieve the following:

1. Initiate a sync between an [adidas X_CELL](http://micoach.adidas.com/x_cell/) or an [adidas SPEED_CELL](http://micoach.adidas.com/speed_cell/) sensor and miCoach MultiSport, which saves workout data to Google Fit.
2. View the miCoach MultiSport workout data as saved on Google Fit.

# Developing Your App for adidas & Google Fit Integration

### Pre-requisites

1. In order to for your app to be able to communicate with the Google Fit API, you must first enable Google Fit for your app’s package name and SHA through the [Google Developer Console](https://console.developers.google.com). Instructions on how to do this can be found on the [Google Fit Getting Started Guide](https://developers.google.com/fit/android/get-started).
2. You'll need to import **Google Play services** into your project.  This can be done by adding the following line of code in the *dependencies* block of your project's build.gradle file:

`compile 'com.google.android.gms:play-services:6.1.71'`


For a full example, please look at the build.gradle file in this repository. For further information, please refer to the [Google Play services documentation](http://developer.android.com/google/play-services/setup.html).

### Requesting a Sync

To request a sync from miCoach MultiSport, you will need to create an [explicit intent](http://developer.android.com/guide/components/intents-filters.html#ExampleExplicit) that will launch the syncing component of miCoach MultiSport.  In this demo app we accomplish this with the following code in the onActivityCreated method of CustomIntentSampleFragment:

	Intent i = new Intent();
    i.setAction(getString(R.string.micoach_multisport_package_name) + ".sync");

    if (!isPackageInstalled(getString(R.string.micoach_multisport_package_name),
            getActivity())) {
        promptInstallMiCoach(getActivity());
        return;
    }
    startActivityForResult(i, ADIDAS_MSA_REQUEST_CODE);

The *isPackageInstalled()* call above is used to verify whether miCoach MultiSport is installed on the user’s device:

	PackageManager pm = context.getPackageManager();
	try {
	    pm.getPackageInfo("com.adidas.micoach.x_cell", PackageManager.GET_ACTIVITIES);
	    return true;
	} catch (PackageManager.NameNotFoundException e) {
	    return false;
	}

If miCoach MultiSport is not installed on the device, we show a dialog prompting the user to install the app from the Play Store. Otherwise, we call *startActivityForResult()* to launch the intent.

**Capturing Results from miCoach MultiSport**

Once the intent has been launched and miCoach MultiSport has retrieved workout data from the adidas X_CELL sensor, it sends the data to Google Fit and sends an intent result code back to your app.  In order to capture the result, your app must implement  *onActivityForResult()* in your Activity or Fragment. In this demo, we accomplish this inside CustomIntentSampleFragment since this is where the intent was launched from:

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
	           // TODO: Read data from Google Fit.
	           break;
	       }
	       case ADIDAS_MSA_RESULT_SYNC_FAILED: {
	           Log.d(TAG, "Sync failed: " + message);
	           // TODO: Notify user of failure, retry, etc.
	           break;
	       }
	       case ADIDAS_MSA_RESULT_NOT_ENABLED: {
	           Log.d(TAG, "Not authenticated: " + message);
	           // TODO: Notify user to authenticate to miCoach MultiSport and Google Fit through miCoach MultiSport-app.
	           break;
	       }
	   }

	}

The table below shows all the possible intent results that need to be handled by your app.


| Result                                	| Description                                                                                   	|
|---------------------------------------	|-----------------------------------------------------------------------------------------------	|
| ADIDAS\_MSA_RESULT\_SYNC\_OK (-1)     	| Workout data is available in Google Fit                                                       	|
| ADIDAS\_MSA\_RESULT\_SYNC\_FAILED (1) 	| Workout data could not be synced with Google Fit                                              	|
| ADIDAS\_MSA\_RESULT\_NOT\_ENABLED (2) 	| User has not logged-in to miCoach MultiSport or Google Fit integration is not enabled. 	|

### Retrieving miCoach MultiSport workout data from Google Fit

Once successfully synced, your app is ready to retrieve workout data from Google Fit.  In this demo, we illustrate how to accomplish this inside GoogleFitSessionListFragment's *readSession()* method:

        long DAY_IN_MS = TimeUnit.DAYS.toMillis(7);
        Date now = new Date();
        long endTime = now.getTime();
        long startTime = endTime - DAY_IN_MS;

        SessionReadRequest request = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(dataTypeJumpHeight)
                .read(dataTypeHustle)
                .read(dataTypeQuickness)
                .read(DataType.TYPE_HEART_RATE_BPM)
                .read(DataType.TYPE_DISTANCE_DELTA)
                .read(DataType.TYPE_SPEED)
                .readSessionsFromAllApps()
                .build();

        PendingResult<SessionReadResult> pendingResult =
                Fitness.SessionsApi.readSession(mClient, request);

        pendingResult.setResultCallback(
                new ResultCallback<SessionReadResult>() {
                    @Override
                    public void onResult(SessionReadResult sessionReadResult) {

                        mSessions = sessionReadResult.getSessions();
                        mDemoApplication.setSessions(mSessions);
                        for (Session session : mSessions) {
                            String sessionName = session.getName();

                            List<DataSet> currentSessionDataSets = sessionReadResult.getDataSet(
                                    session);
                            mSessionDataSets.put(session.getIdentifier(), currentSessionDataSets);
                        }
                        mDemoApplication.setSessionDataSets(mSessionDataSets);
                        mSessionAdapter.setSessions(mSessions);
                        mSessionAdapter.notifyDataSetChanged();
                    }
                }
        );

In the above code we are requesting the last 7 days worth of Google Fit data for the device's authenticated Google account.  From the *sessionReadResult* inside the *onResult()* callback you can retrieve both workout Sessions as well as the DataSets for each session.  Please note that the Google Fit account in use might have some workout data that was not generated by miCoach MultiSport.  As such, when you call the *getSessions()* method inside the onResult() callback, you will want to filter the data so that only sessions whose identifier starts with "micoach-" are used.

Once the data is persisted in your app (in-memory cache, database, etc) you use the Session and DataSet objects to display or manipulate the workout data.  As an example, the GoogleFitDetailFragment in the demo app shows graphs for distance, speed, heart rate, hustle, quickness and vertical (jump height) for a selected workout session from the GoogleFitSessionListFragment. Of particular interest is the *populateCharts()* method which reads the workout data from the Session's DataSet(s).

For more general information on Google Fit APIs please refer to the following references:

1. [Connect to the Google Fitness service](https://developers.google.com/fit/android/get-started#step_5_connect_to_the_fitness_service)
2. [Send a custom data type request](https://developers.google.com/fit/android/data-types#retrieve_your_custom_data_types) to Google Fit to retrieve the custom adidas data types: jump height, hustle, quickness. For more information about these data types, reference the [Google Fit Available shareable data types] (https://developers.google.com/fit/android/data-types#available_shareable_data_types) documentation.  
**NOTE:** Heart rate is a DataType object built into the Google Fit API and does not need to be requested</li>
3. [Send a session read request](https://developers.google.com/fit/android/using-sessions#read_fitness_data_using_sessions) to Google Fit to retrieve the workout data for the authenticated account.

#Credits

This demo app makes use of third-party library [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) to generate all its workout charts. The usage purposes of MPAndroidChart on this demo app is only to illustrate how workout data could look like. However, the use of such a library is not required in order for third-party developers to integrate their app with miCoach MultiSport and Google Fit.


#License
-------

    Copyright 2014 Adidas AG ("Adidas") and its licensors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

