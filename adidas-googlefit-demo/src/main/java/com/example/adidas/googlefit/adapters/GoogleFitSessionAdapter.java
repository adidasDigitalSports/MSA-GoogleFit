package com.example.adidas.googlefit.adapters;


import com.google.android.gms.fitness.data.Session;


import com.example.adidas.googlefit.R;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GoogleFitSessionAdapter extends BaseAdapter {

    private Context mContext;
    private List<Session> mSessions;

    public GoogleFitSessionAdapter(Context context, List<Session> sessionList) {
        this.mContext = context;
        this.mSessions = sessionList;
    }

    @Override
    public int getCount() {
        if (mSessions == null) {
            return -1;
        }
        return mSessions.size();
    }

    @Override
    public Object getItem(int position) {
        return mSessions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SessionViewHolder holder;

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(android.R.layout.simple_list_item_2, parent, false);

            // cache row fields into holder
            holder = new SessionViewHolder();
            holder.tvSessionName = (TextView) convertView.findViewById(android.R.id.text1);
            holder.tvSessionTimestamp = (TextView) convertView.findViewById(android.R.id.text2);

            // associate the holder with the row for later lookup
            convertView.setTag(holder);
        } else {
            holder = (SessionViewHolder) convertView.getTag();
        }

        Session mSession = (Session) getItem(position);
        holder.tvSessionName.setText(mSession.getName().toUpperCase());
        holder.tvSessionName.setTextColor(mContext.getResources().getColor(R.color.adidas_black));
        holder.tvSessionName.setTypeface(Typeface.DEFAULT_BOLD);
        long sessionTimestamp = mSession.getStartTime(TimeUnit.MILLISECONDS);
        holder.tvSessionTimestamp.setText(DateUtils.formatDateTime(mContext, sessionTimestamp,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
        holder.tvSessionTimestamp.setTextColor(
                mContext.getResources().getColor(R.color.adidas_green));
        holder.tvSessionTimestamp.setTypeface(Typeface.DEFAULT_BOLD);

        return convertView;
    }

    public void setSessions(List<Session> sessions) {
        this.mSessions = sessions;
    }

    private class SessionViewHolder {

        public TextView tvSessionName = null;
        public TextView tvSessionTimestamp = null;
    }
}

