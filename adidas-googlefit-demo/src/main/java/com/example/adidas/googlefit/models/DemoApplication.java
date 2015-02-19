package com.example.adidas.googlefit.models;

import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;

import android.app.Application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoApplication extends Application {

    private List<Session> mSessions = new ArrayList<Session>();
    private Map<String, List<DataSet>> mSessionDataSets = new HashMap<String, List<DataSet>>();

    public List<Session> getSessions() {
        return mSessions;
    }

    public void setSessions(List<Session> nearbyVenues) {
        Collections.sort(nearbyVenues, new GoogleFitSessionComparator());
        this.mSessions = nearbyVenues;
    }

    public Map<String, List<DataSet>> getSessionDataSets() {
        return mSessionDataSets;
    }

    public void setSessionDataSets(Map<String, List<DataSet>> sessionDataSets) {
        this.mSessionDataSets = sessionDataSets;
    }

    public Session getSession(String sessionId) {
        if (!mSessions.isEmpty()) {
            for (Session session : mSessions) {
                if (session.getIdentifier().equals(sessionId)) {
                    return session;
                }
            }
        }
        return null;
    }

    public List<DataSet> getSessionDataSets(String sessionId) {
        if (!mSessions.isEmpty() && !mSessionDataSets.isEmpty()) {
            for (Session session : mSessions) {
                if (session.getIdentifier().equals(sessionId)) {
                    return mSessionDataSets.get(sessionId);
                }
            }
        }
        return null;
    }
}
