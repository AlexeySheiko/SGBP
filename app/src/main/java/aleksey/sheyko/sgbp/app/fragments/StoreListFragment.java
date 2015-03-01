package aleksey.sheyko.sgbp.app.fragments;

import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.List;

import aleksey.sheyko.sgbp.R;
import aleksey.sheyko.sgbp.model.Notification;
import aleksey.sheyko.sgbp.model.Store;
import aleksey.sheyko.sgbp.app.activities.MapPane;
import aleksey.sheyko.sgbp.app.helpers.Constants;
import aleksey.sheyko.sgbp.app.adapters.NotificationsAdapter;
import aleksey.sheyko.sgbp.app.adapters.StoresAdapter;
import aleksey.sheyko.sgbp.app.tasks.UpdateStoreList;
import aleksey.sheyko.sgbp.app.tasks.UpdateStoreList.OnStoreListLoaded;

public class StoreListFragment extends ListFragment
        implements ConnectionCallbacks, OnStoreListLoaded {

    private ArrayList<Store> mStoreList = new ArrayList<>();
    private ArrayList<Notification> mNotificationList = new ArrayList<>();
    private List<Store> mStores;
    private List<Notification> mNotifications;
    private String mCategory;
    private String mSearchQuery;
    private int mViewMode;
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences mSharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mViewMode = mSharedPrefs.getInt(
                "view_mode", Constants.VIEW_CATEGORIES);
        if (mViewMode == Constants.VIEW_CATEGORIES ||
                mViewMode == Constants.VIEW_NEAREST) {
            setHasOptionsMenu(true);
        }

        if (getActivity().getIntent() != null) {
            if (getActivity().getIntent().hasExtra("category")) {
                mCategory = getActivity().getIntent().getStringExtra("category");
            } else if (getActivity().getIntent().hasExtra(SearchManager.QUERY)) {
                mSearchQuery = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
            }
        }

        if (mCategory != null) {
            mStores = Store.find(Store.class, "category = ?", mCategory);
        } else if (mSearchQuery != null) {
            mStores = Store.findWithQuery(Store.class,
                    "Select * from Store where " +
                            "name like '%" + mSearchQuery + "%' or " +
                            "address like '%" + mSearchQuery + "%' or " +
                            "category like '%" + mSearchQuery + "%'");
        } else if (mViewMode == Constants.VIEW_NOTIFICATIONS) {
            mNotifications = Select.from(Notification.class).orderBy("id DESC").list();
            for (Notification notification : mNotifications) {
                mNotificationList.add(new Notification(
                        notification.getStoreName(), notification.getDate()));
            }
            return;
        } else if (mViewMode == Constants.VIEW_COUPONS) {
            mStores = Store.listAll(Store.class);
        } else if (mViewMode == Constants.VIEW_NEAREST) {
            createLocationClient();
            mGoogleApiClient.connect();
            return;
        }

        if (mStores == null) return;
        if (mStores.size() == 0 && mSearchQuery == null) {
            new UpdateStoreList(this).execute();
            return;
        }

        for (Store store : mStores) {
            mStoreList.add(new Store(
                    store.getStoreid(),
                    store.getName(),
                    store.getAddress(),
                    store.getPhone(),
                    store.getLatitude(),
                    store.getLongitude(),
                    store.getCategory()));
        }
    }

    private synchronized void createLocationClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        updateDistances();
    }

    private void updateDistances() {
        mStores = Store.listAll(Store.class);
        Location myLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (myLocation == null) {
            populateListAdapter();
            return;
        }
        for (Store store : mStores) {
            Location storeLocation = new Location("store");
            storeLocation.setLatitude(Double.parseDouble(store.getLatitude()));
            storeLocation.setLongitude(Double.parseDouble(store.getLongitude()));

            float distance = myLocation.distanceTo(storeLocation) * 0.000621371f;
            store.setDistance(distance);
            store.save();
        }
        mStores = Select.from(Store.class).orderBy("distance").list();
        populateListAdapter();
    }

    private void populateListAdapter() {
        for (Store store : mStores) {
            mStoreList.add(new Store(
                    store.getStoreid(),
                    store.getName(),
                    store.getAddress(),
                    store.getPhone(),
                    store.getLatitude(),
                    store.getLongitude(),
                    store.getCategory()));
            mSharedPrefs.edit().putFloat(store.getStoreid() + "", store.getDistance()).apply();
        }
        StoresAdapter mAdapter = new StoresAdapter(getActivity(),
                R.layout.store_list_item, mStoreList);
        setListAdapter(mAdapter);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mViewMode == Constants.VIEW_NOTIFICATIONS) {
            NotificationsAdapter mAdapter = new NotificationsAdapter(getActivity(),
                    R.layout.store_list_item, mNotificationList);
            setListAdapter(mAdapter);
        } else {
            StoresAdapter mAdapter = new StoresAdapter(getActivity(),
                    R.layout.store_list_item, mStoreList);
            setListAdapter(mAdapter);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mViewMode == Constants.VIEW_NOTIFICATIONS) {
            getListView().setEmptyView(
                    noItems(getResources().getString(R.string.notifications_empty)));
        } else {
            getListView().setEmptyView(
                    noItems(getResources().getString(R.string.stores_empty)));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (mViewMode == Constants.VIEW_NEAREST ||
                mViewMode == Constants.VIEW_CATEGORIES) {
            String name = mStoreList.get(position).getName();
            String latitude = mStoreList.get(position).getLatitude();
            String longitude = mStoreList.get(position).getLongitude();
            Intent intent = new Intent(this.getActivity(), MapPane.class);
            intent.putExtra("name", name);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            startActivity(intent);
        } else if (mViewMode == Constants.VIEW_COUPONS) {
            Toast.makeText(this.getActivity(),
                    "Coupons are coming soon", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity())
                == ConnectionResult.SUCCESS) {
            if ((mStores != null && mStores.size() != 0) || mStores == null)
                inflater.inflate(R.menu.store_list_fragment, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                Intent intent = new Intent(this.getActivity(), MapPane.class);
                if (mCategory != null)
                    intent.putExtra("category", mCategory);
                if (mSearchQuery != null)
                    intent.putExtra(SearchManager.QUERY, mSearchQuery);
                startActivity(intent);
                return true;
        }
        return false;
    }

    private TextView noItems(String text) {
        TextView emptyView = new TextView(getActivity());
        //Make sure you import android.widget.LinearLayout.LayoutParams;
        LayoutParams mLayoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        emptyView.setLayoutParams(mLayoutParams);
        //Instead of passing resource id here I passed resolved color
        //That is, getResources().getColor((R.color.gray_dark))
        emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        emptyView.setText(text);
        emptyView.setPadding(0, 60, 0, 0);
        emptyView.setTextSize(18);
        emptyView.setVisibility(View.GONE);
        emptyView.setGravity(Gravity.CENTER_HORIZONTAL);

        //Add the view to the list view. This might be what you are missing
        ((ViewGroup) getListView().getParent()).addView(emptyView);

        return emptyView;
    }

    @Override
    public void onStoreListUpdated() {
        StoresAdapter mAdapter = new StoresAdapter(getActivity(),
                R.layout.store_list_item, mStoreList);

        setListAdapter(mAdapter);
    }
}