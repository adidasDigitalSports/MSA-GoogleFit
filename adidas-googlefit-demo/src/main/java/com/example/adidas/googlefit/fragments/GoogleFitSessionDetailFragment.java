package com.example.adidas.googlefit.fragments;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;

import com.example.adidas.googlefit.R;
import com.example.adidas.googlefit.models.DemoApplication;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.XLabels;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GoogleFitSessionDetailFragment extends Fragment {

    private static final String TAG = GoogleFitSessionDetailFragment.class.getSimpleName();

    private static final String ARG_GFIT_SESSION_ID = "gfit_session_id";

    private Session mSession;
    private List<DataSet> mDataSetsList;

    private TextView tvSessionName;
    private TextView tvSessionTimestamp;
    private LineChart mHeartRateChart;
    private LineChart mHustleChart;
    private LineChart mQuicknessChart;
    private LineChart mJumpHeightChart;
    private LineChart mDistanceChart;
    private LineChart mSpeedChart;

    private String mHr_field_name;
    private String mHustle_field_name;
    private String mJump_height_field_name;
    private String mQuickness_field_name;
    private String mDistance_field_name;
    private String mSpeed_field_name;

    private Activity mActivity;

    public GoogleFitSessionDetailFragment() {
        // Required empty public constructor
    }

    public static GoogleFitSessionDetailFragment newInstance(Session session) {
        GoogleFitSessionDetailFragment fragment = new GoogleFitSessionDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GFIT_SESSION_ID, session.getIdentifier());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String sessionId = getArguments().getString(ARG_GFIT_SESSION_ID);
            mActivity = getActivity();
            mSession = ((DemoApplication) mActivity.getApplication()).getSession(sessionId);
            mDataSetsList = ((DemoApplication) mActivity.getApplication()).getSessionDataSets(
                    sessionId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gfit_session_detail, container, false);

        tvSessionTimestamp = (TextView) v.findViewById(R.id.txtSessionName);
        tvSessionTimestamp.setText(mSession.getName().toUpperCase());

        tvSessionName = (TextView) v.findViewById(R.id.txtSessionTimestamp);
        long sessionTimestamp = mSession.getStartTime(TimeUnit.MILLISECONDS);
        tvSessionName.setText(DateUtils.formatDateTime(mActivity, sessionTimestamp,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));

        mHeartRateChart = (LineChart) v.findViewById(R.id.hrChart);
        mHustleChart = (LineChart) v.findViewById(R.id.hustleChart);
        mQuicknessChart = (LineChart) v.findViewById(R.id.quicknessChart);
        mJumpHeightChart = (LineChart) v.findViewById(R.id.jumpHeightChart);
        mDistanceChart = (LineChart) v.findViewById(R.id.distanceChart);
        mSpeedChart = (LineChart) v.findViewById(R.id.speedChart);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHr_field_name = mActivity.getResources()
                .getString(
                        R.string.google_fit_adidas_field_heart_rate);
        mHustle_field_name = mActivity.getResources()
                .getString(
                        R.string.google_fit_adidas_field_hustle);
        mJump_height_field_name = mActivity.getResources()
                .getString(
                        R.string.google_fit_adidas_field_jump_height);
        mQuickness_field_name = mActivity.getResources()
                .getString(
                        R.string.google_fit_adidas_field_quickness);
        mDistance_field_name = mActivity.getResources()
                .getString(
                        R.string.google_fit_adidas_field_distance);
        mSpeed_field_name = mActivity.getResources()
                .getString(
                        R.string.google_fit_adidas_field_speed);

        Log.d(TAG, "The number of data sets is: " + mDataSetsList.size());
        populateCharts();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_GFIT_SESSION_ID, mSession.getIdentifier());
    }

    private void populateCharts() {
        ArrayList<Entry> heartRateYValues = new ArrayList<Entry>();
        ArrayList<String> heartRateXValues = new ArrayList<String>();
        ArrayList<Entry> hustleYValues = new ArrayList<Entry>();
        ArrayList<String> hustleXValues = new ArrayList<String>();
        ArrayList<Entry> quicknessYValues = new ArrayList<Entry>();
        ArrayList<String> quicknessXValues = new ArrayList<String>();
        ArrayList<Entry> jumpHeightYValues = new ArrayList<Entry>();
        ArrayList<String> jumpHeightXValues = new ArrayList<String>();
        ArrayList<Entry> distanceYValues = new ArrayList<Entry>();
        ArrayList<String> distanceXValues = new ArrayList<String>();
        ArrayList<Entry> speedYValues = new ArrayList<Entry>();
        ArrayList<String> speedXValues = new ArrayList<String>();

        for (DataSet dataSet : mDataSetsList) {

            int hrCounter = 0;
            int hustleCounter = 0;
            int quicknessCounter = 0;
            int jumpHeightCounter = 0;
            int distanceCounter = 0;
            int speedCounter = 0;
            for (DataPoint dp : dataSet.getDataPoints()) {
                for (Field field : dp.getDataType().getFields()) {
                    if (field.getName()
                            .contains("distance")) {  // Save Distance data
                        distanceYValues.add(
                                new Entry(dp.getValue(field).asFloat(), distanceCounter++));
                        String dpTimeStamp = formatTimeStamp(dp.getStartTime(TimeUnit.MILLISECONDS));
                        distanceXValues.add(dpTimeStamp);
                    } else {
                        String dpTimeStamp = formatTimeStamp(dp.getTimestamp(TimeUnit.MILLISECONDS));

                        // Save chart data
                        if (field.getName().contains(mHr_field_name)) { // Save Heart Rate data
                            heartRateYValues.add(new Entry(dp.getValue(field).asFloat(), hrCounter++));
                            heartRateXValues.add(dpTimeStamp);
                        } else if (field.getName().contains(mHustle_field_name)) {  // Save Hustle data
                            hustleYValues.add(new Entry(dp.getValue(field).asFloat(), hustleCounter++));
                            hustleXValues.add(dpTimeStamp);
                        } else if (field.getName()
                                .contains(mQuickness_field_name)) {  // Save Quickness data
                            quicknessYValues.add(
                                    new Entry(dp.getValue(field).asFloat(), quicknessCounter++));
                            quicknessXValues.add(dpTimeStamp);
                        } else if (field.getName()
                                .contains(mJump_height_field_name)) {  // Save Jump Height data
                            jumpHeightYValues.add(
                                    new Entry(dp.getValue(field).asFloat(), jumpHeightCounter++));
                            jumpHeightXValues.add(dpTimeStamp);
                        } else if (field.getName()
                                .contains(mSpeed_field_name)) {  // Save Speed data
                            speedYValues.add(
                                    new Entry(dp.getValue(field).asFloat(), speedCounter++));
                            speedXValues.add(dpTimeStamp);
                        }
                    }
                }
            }

            Log.d(TAG, "distance count: " + distanceCounter);
        }
        // Create and display a chart for each workout statistic
        setChartData(mHeartRateChart, heartRateXValues, heartRateYValues,
                getString(R.string.google_fit_adidas_heartrate_unit)).invalidate();
        setChartData(mHustleChart, hustleXValues, hustleYValues,
                getString(R.string.google_fit_adidas_hustle_unit)).invalidate();
        setChartData(mQuicknessChart, quicknessXValues, quicknessYValues,
                getString(R.string.google_fit_adidas_quickness_unit)).invalidate();
        setChartData(mJumpHeightChart, jumpHeightXValues, jumpHeightYValues,
                getString(R.string.google_fit_adidas_jumpheight_unit)).invalidate();
        setChartData(mDistanceChart, distanceXValues, distanceYValues,
                getString(R.string.google_fit_adidas_distance_unit)).invalidate();
        setChartData(mSpeedChart, speedXValues, speedYValues,
                getString(R.string.google_fit_adidas_speed_unit)).invalidate();
    }

    private LineChart setChartData(LineChart chart,
            ArrayList<String> xValuesList,
            ArrayList<Entry> yValuesList, String unitLabel) {
        // Setup line data
        LineDataSet hrDataSet = new LineDataSet(yValuesList, unitLabel);
        hrDataSet.setLineWidth(1.75f);
        hrDataSet.setCircleSize(3f);
        hrDataSet.setColor(getResources().getColor(R.color.adidas_green));
        hrDataSet.setCircleColor(getResources().getColor(R.color.adidas_green));
        hrDataSet.setHighLightColor(getResources().getColor(R.color.adidas_green));

        // Setup LineData
        LineData hrData = new LineData(xValuesList, hrDataSet);

        // Setup chart
        // if enabled, the chart will always start at zero on the y-axis
        chart.setStartAtZero(true);

        // disable the drawing of values into the chart
        chart.setDrawYValues(false);

        // X-Axis settings
        chart.setDrawXLabels(true);
        XLabels xLabels = chart.getXLabels();
        xLabels.setPosition(XLabels.XLabelPosition.BOTTOM);
        xLabels.setCenterXLabelText(true);
        xLabels.setSpaceBetweenLabels(2);

        // Disable description text
        chart.setDescription("");
        chart.setNoDataTextDescription("No data available for this chart.");

        // enable / disable grid lines
        chart.setDrawVerticalGrid(true);
        chart.setDrawHorizontalGrid(true);

        // enable / disable grid background
        chart.setDrawGridBackground(false);
        chart.setGridColor(Color.GRAY);
        chart.setGridWidth(1.25f);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setBackgroundColor(Color.WHITE);
        chart.setValueTypeface(Typeface.DEFAULT_BOLD);

        // add data
        chart.setData(hrData);

        // Set legend position (must be done after chart has data)
        Legend l = chart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        l.setForm(Legend.LegendForm.SQUARE);

        return chart;
    }

    private String formatTimeStamp(long time) {
        String dpTimeStamp = DateUtils.formatDateTime(mActivity, time,
                DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME);
        return dpTimeStamp;
    }
}
