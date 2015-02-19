package com.example.adidas.googlefit.models;

import com.google.android.gms.fitness.data.Session;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * Used to compare Google Fit Sessions by their start time.
 * Created by Nikoletta_Muhari on 2/18/2015.
 */
public class GoogleFitSessionComparator implements Comparator<Session> {

    @Override
    public int compare(Session session1, Session session2) {
        long startTime1 = session1.getStartTime(TimeUnit.MILLISECONDS);
        long startTime2 = session2.getStartTime(TimeUnit.MILLISECONDS);
        return (startTime1 < startTime2)? 1 :
                ((startTime1 == startTime2)? 0 : -1);
    }
}
