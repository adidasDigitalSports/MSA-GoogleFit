package com.example.adidas.googlefit.adapters;



import com.example.adidas.googlefit.R;
import com.example.adidas.googlefit.models.NavItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class NavDrawerAdapter extends BaseAdapter {

    private Context mContext;
    private List<NavItem> mItems;

    public NavDrawerAdapter(Context context, List<NavItem> listItems) {
        this.mContext = context;
        this.mItems = listItems;
    }

    @Override
    public int getCount() {
        if (mItems == null) {
            return 0;
        }
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final NavItemViewHolder holder;

        if (convertView != null) {
            holder = (NavItemViewHolder) convertView.getTag();
        } else {
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.nav_drawer_list_item, parent, false);

            // cache row fields into holder
            holder = new NavItemViewHolder();
            holder.tvItemImage = (ImageView) convertView.findViewById(R.id.itemImage);
            holder.tvItemTitle = (TextView) convertView.findViewById(R.id.itemText);

            // associate the holder with the row for later lookup
            convertView.setTag(holder);
        }

        NavItem currentNavItem = mItems.get(position);
        holder.tvItemImage.setImageResource(currentNavItem.getIcon());
        holder.tvItemTitle.setText(currentNavItem.getTitle());

        return convertView;
    }

    private class NavItemViewHolder {

        public ImageView tvItemImage = null;
        public TextView tvItemTitle = null;
    }
}

