package com.example.adidas.googlefit.activities;


import com.example.adidas.googlefit.R;
import com.example.adidas.googlefit.adapters.NavDrawerAdapter;
import com.example.adidas.googlefit.fragments.CustomIntentSampleFragment;
import com.example.adidas.googlefit.fragments.GoogleFitSessionListFragment;
import com.example.adidas.googlefit.models.NavItem;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int NAV_ITEM_SYNC_INTENT = 0;
    private static final int NAV_ITEM_VIEW_GFIT_DATA = 1;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActionBar = getActionBar();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        List<NavItem> listItems = new ArrayList<NavItem>();
        listItems.add(new NavItem(getString(R.string.nav_drawer_item_sync_data),
                R.drawable.ic_action_reload));
        listItems.add(new NavItem(getString(R.string.nav_drawer_item_display_data),
                R.drawable.ic_action_bike));
        NavDrawerAdapter adapter = new NavDrawerAdapter(getApplicationContext(),
                listItems);

        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Fragment currentFragment;
                switch (position) {
                    case NAV_ITEM_SYNC_INTENT: {
                        currentFragment = CustomIntentSampleFragment.newInstance();
                        break;
                    }
                    case NAV_ITEM_VIEW_GFIT_DATA: {
                        currentFragment = GoogleFitSessionListFragment.newInstance();
                        break;
                    }
                    default: {
                        currentFragment = null;
                        break;
                    }
                }

                if (currentFragment == null) {
                    Log.wtf(TAG, "null onItemClick");
                    return;
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.container,
                                currentFragment)
                        .commit();

                mDrawerLayout.closeDrawer(Gravity.START);

            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        // Setup initial fragment to the custom intent as this is the main focus of the demo app
        if (savedInstanceState == null) {
            CustomIntentSampleFragment fragment = CustomIntentSampleFragment.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment).commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // This activity is intercepting GFitConnectedFragment's startResolutionForResult
        // error handling call. The code below send the resultCode back to the fragment
        // so we can initiate the Google API client's connect() call
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}
