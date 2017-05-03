package com.example.findtographer;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.findtographer.R.id.web;
import static junit.framework.Assert.assertTrue;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class Findtographer extends AppCompatActivity
        implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, View.OnClickListener {

    protected static final String TAG = "MapsMarkerActivity";
    private Button current, call;
    private Button go;
    private WebView webView;
    private TextView desc_text;
    private EditText url_txt, city;
    private String  streetAddr;
    private static TabHost tabs;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_stat_name);

        current = (Button) findViewById(R.id.buttonA);
        current.setOnClickListener(this);

        call = (Button) findViewById(R.id.call);
        call.setOnClickListener(this);

        go = (Button) findViewById(R.id.go2);

        webView = (WebView) findViewById(web);
        webView.setWebViewClient(new WebViewClient());

        url_txt = (EditText) findViewById(R.id.text2);
        desc_text = (TextView) findViewById(R.id.text3);
        city = (EditText) findViewById(R.id.city);

        tabs = (TabHost) findViewById(R.id.tabhost);
        tabs.setup();
        TabHost.TabSpec spec;

        // Initialize a TabSpec for tab1 and add it to the TabHost
        spec = tabs.newTabSpec("tag1");    //create new tab specification
        spec.setContent(R.id.tab1);    //add tab view content
        spec.setIndicator("Schedule shoot");    //put text on tab
        tabs.addTab(spec);             //put tab in TabHost container

        // Initialize a TabSpec for tab2 and add it to the TabHost
        spec = tabs.newTabSpec("tag2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Portfolios");
        tabs.addTab(spec);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // city from edit text loaded on enter key
        city.setOnKeyListener(new View.OnKeyListener() {
            @Override
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {

                        streetAddr = city.getText().toString();
                        try {
                            geocode();
                        }
                        catch(Exception e){
                            System.out.print("exception caught");
                        }

                        return true;
                    }
                    return false;
                }
            });

        //set listeners for web tab
        go.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                webView.getSettings().setJavaScriptEnabled(true);
                String url_str = "";
                String desc = "";
                if (url_txt.getText().toString().contains("Wedding")) {
                    url_str = "https://lisarigbyphotography.com";//"http://www.lenapetersonphotography.com";
                    desc = "Lisa Rigby. Price: $250-1000. Contact: lisa@lisarigbyphotography.com ";//"Leena Peterson. Price: $250-1000. Contact: info@lenapetersonphotography.com";
                } else if (url_txt.getText().toString().contains("Fashion")) {
                    url_str = "https://anna-tabakova.squarespace.com/";
                    desc = "Anna Tabakova. Price: $100-500. Contact: tabakovaannaph@gmail.com";
                } else {
                    url_str = "http://www.facebook.com/AngelaSuPhotography";
                    desc = "Angela Su. Price: $50-250. Contact: angiemsu@gmail.com";
                }

                webView.loadUrl(url_str);
                url_txt.setText("");
                desc_text.setText(desc);
            }
        });

        url_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl(url_txt.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    private void geocode() throws Exception{
        // forward geocoding
        Geocoder fwd = new Geocoder(this);
        android.location.Address location = fwd.getFromLocationName(streetAddr, 1 ).get(0);

        LatLng test = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(test).title("Shoot location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(test, 16));
    }
    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // gets current location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null)
            Log.i("something", mLastLocation.toString());
        else
            Log.i("something", "nothing");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Manipulates the map when available.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("permission", "yes");
                } else {
                    Log.e("permission", "no");
                }
                return;
            }
            case 1: {
                //
                Log.i(TAG, "location:");
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("permission1", "yes");
                } else {
                    Log.e("permission1", "no");
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMapInput) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("maps", "no permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        googleMapInput.setMyLocationEnabled(true);
        if (googleMapInput.isMyLocationEnabled())
            Log.i("check", "yes");
        else
            Log.i("check", "no");
        googleMap = googleMapInput;
        buildGoogleApiClient();
    }

    public void onClick(View v) {
        LatLng test = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        Log.i("last", mLastLocation.toString());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("mLastLocation Object Value");

        switch (v.getId()) {
            case R.id.buttonA:

                if (mLastLocation != null) {
                    googleMap.addMarker(new MarkerOptions().position(test).title("Shoot location"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(test, 16));
                    myRef.setValue(mLastLocation);
                }
                //sleep
                try {
                    Thread.sleep(1000);                 //1000 milliseconds is one second.
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                ;
                callNotification(this);
                break;

            // intent to call dialer
            case R.id.call:
                Uri uri3 = Uri.parse("tel:5089511273");
                Intent i3 = new Intent(Intent.ACTION_CALL, uri3);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(i3);
                break;
        }
    }

    // get current tab for switching tabs
    public static TabHost getCurrentTab() {
        return tabs;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            int month = datePicker.getMonth();
            int day = datePicker.getDayOfMonth();
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

        }
    }

    public void callNotification(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(context.getText(R.string.notification_title))
                        .setContentText(context.getText(R.string.notification_content));

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify("", 0, mBuilder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.switchv:
                intent = new Intent(this, Photographer.class);
                startActivity(intent);
                break;

            case R.id.back:
                intent = new Intent(this, Login.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}