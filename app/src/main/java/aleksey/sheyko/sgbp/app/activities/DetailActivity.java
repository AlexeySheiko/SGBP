package aleksey.sheyko.sgbp.app.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import aleksey.sheyko.sgbp.R;
import aleksey.sheyko.sgbp.model.Notification;
import aleksey.sheyko.sgbp.model.Store;
import aleksey.sheyko.sgbp.rest.ApiService;
import aleksey.sheyko.sgbp.rest.RestClient;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DetailActivity extends Activity {

    private static final long PARTICIPATE_INTERVAL = 15 * 60 * 1000;

    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int storeId = mSharedPrefs.getInt("storeId", -1);
        final Store currentStore = Store.find(Store.class, "storeid = ?", String.valueOf(storeId)).get(0);

        final double latitude = Double.parseDouble(currentStore.getLatitude());
        final double longitude = Double.parseDouble(currentStore.getLongitude());
        final String name = currentStore.getName();
        String address = currentStore.getAddress();
        String phone = currentStore.getPhone();

        final Button actionButton = (Button) findViewById(R.id.button);
        boolean isMobile = mSharedPrefs.getBoolean("isMobile", false);
        if (isMobile) {
            if (currentStore.isParticipated()) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                long currentTime = calendar.getTimeInMillis();

                if (currentStore.getTimeAllowNext() < currentTime) {
                    currentStore.setParticipated(false);
                    actionButton.setEnabled(true);
                    enableButton(actionButton, currentStore);
                    return;
                }
                actionButton.setText("Participated");
                actionButton.setBackgroundResource(R.drawable.participate_button_selector);
                actionButton.setEnabled(false);
            } else {
                enableButton(actionButton, currentStore);
            }
        } else {
            actionButton.setText("Make route");
            actionButton.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View view) {
                    startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?daddr=" + latitude + "," + longitude)));
                }
            });
        }

        ((TextView) findViewById(R.id.address)).setText(address);
        ((TextView) findViewById(R.id.phone)).setText(phone);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        GoogleMap map = mapFragment.getMap();

        if (map == null) {
            Toast.makeText(this,
                    "Install Google Play Services to see places on map",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        map.getUiSettings().setAllGesturesEnabled(false);
        map.setMyLocationEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnMapClickListener(new OnMapClickListener() {
            @Override public void onMapClick(LatLng latLng) {
                navigateToMap();
            }
        });
        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(name)).showInfoWindow();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13));


        if (address.isEmpty()) {
            ((TextView) findViewById(R.id.address)).setText(getString(R.string.unspecified));
            findViewById(R.id.button_map).setVisibility(View.GONE);
            findViewById(R.id.address_container).setClickable(false);
        }
        if (phone.isEmpty()) {
            ((TextView) findViewById(R.id.phone)).setText(getString(R.string.unspecified));
            findViewById(R.id.button_phone).setVisibility(View.GONE);
            findViewById(R.id.phone_container).setClickable(false);
        }
    }

    private void enableButton(final Button actionButton, final Store currentStore) {
        actionButton.setText("Participate");
        actionButton.setBackgroundResource(R.drawable.participate_button_selector);
        actionButton.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View view) {
                int storeId = mSharedPrefs.getInt("storeId", -1);
                String dateTime = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'").format(new Date());
                String deviceId = mSharedPrefs.getString("device_id", "");
                String userId = mSharedPrefs.getInt("user_id", -1) + "";
                int schoolId = mSharedPrefs.getInt("school_id", -1);

                ApiService service = new RestClient().getApiService();
                service.participate(userId + "", Integer.parseInt(userId), deviceId, schoolId, storeId, dateTime, true, new ResponseCallback() {
                    @Override public void success(Response response) {
                        currentStore.setParticipated(true);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        long currentTime = calendar.getTimeInMillis();
                        currentStore.setTimeAllowNext(currentTime + PARTICIPATE_INTERVAL);
                        currentStore.save();

                        actionButton.setText("Participated");
                        actionButton.setEnabled(false);

                        Toast.makeText(DetailActivity.this, "Thanks for participation", Toast.LENGTH_SHORT).show();

                        showNotification(currentStore.getName());
                        new Notification(currentStore.getName(), getCurrentTime()).save();
                    }

                    @Override public void failure(RetrofitError e) {
                        e.printStackTrace();
                    }
                });

            }
        });
    }

    private void navigateToMap() {
        String name = mSharedPrefs.getString("name", "");
        double latitude = Double.parseDouble(mSharedPrefs.getString("latitude", ""));
        double longitude = Double.parseDouble(mSharedPrefs.getString("longitude", ""));

        startActivity(new Intent(DetailActivity.this, MapPane.class)
                .putExtra("name", name)
                .putExtra("latitude", latitude)
                .putExtra("longitude", longitude));
    }

    public void showMap(View view) {
        navigateToMap();
    }

    public void dial(View view) {
        String phone = mSharedPrefs.getString("phone", "");
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone.replaceAll("[^0-9]", "")));
        startActivity(intent);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return true;
    }

    private void showNotification(String name) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Participation accepted")
                        .setContentText("Thank you for supporting " + name);
        mBuilder.setAutoCancel(true);
        // Sets an ID for the notification
        int mNotificationId = 123;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    public String getCurrentTime() {
        return new SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
                .format(new Date());
    }
}
