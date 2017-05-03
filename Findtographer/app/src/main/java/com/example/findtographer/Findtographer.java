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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static android.R.id.input;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.findtographer.R.id.siteDesc;
import static com.example.findtographer.R.id.text3;
import static com.example.findtographer.R.id.web;
import static junit.framework.Assert.assertTrue;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class Findtographer extends AppCompatActivity
        implements AdapterView.OnItemClickListener, OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, View.OnClickListener {

    protected static final String TAG = "MapsMarkerActivity";
    private Button current;
    private Button go;
    private WebView webView;
    private TextView desc_text;
    private EditText type, city;
    private String  streetAddr;
    private static TabHost tabs;
    private ListView listview;
    private int selected;
    private String url_str;
    private ArrayAdapter<String> arrayad;
    private ArrayList<String> arrayList;
    private Thread t=null;
    private String[] items = {"Wedding","Fashion"};

    //messages from background thread contain data for UI
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String title = (String) msg.obj;

            desc_text.append(title);  //use json info for profile
            //enter.setVisibility(View.GONE);
            //url.setVisibility(View.GONE);
            desc_text.setVisibility(View.VISIBLE);
        }
    };

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

        listview = (ListView) findViewById(R.id.list);

        current = (Button) findViewById(R.id.buttonA);
        current.setOnClickListener(this);

        go = (Button) findViewById(R.id.go2);
        go.setOnClickListener(this);

        webView = (WebView) findViewById(web);
        webView.setWebViewClient(new WebViewClient());

        type = (EditText) findViewById(R.id.text2);
        desc_text = (TextView) findViewById(text3);
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

        // background thread is json parser
        t = new Thread(background);

        // Create a List from String Array elements
        arrayList = new ArrayList<String>(Arrays.asList(items));

        // Set listener
        listview.setOnItemClickListener(this);

        // Create an ArrayAdapter from List
        arrayad = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, arrayList);

        // DataBind ListView with items from ArrayAdapter
        listview.setAdapter(arrayad);
        arrayad.notifyDataSetChanged();

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
        // add button adds new item
        go.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String new_item = type.getText().toString();
                arrayad.add(new_item);
                arrayad.notifyDataSetChanged();
            }
        });

        type.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl(type.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // when click item, displays in EditText
        type.setText(arrayad.getItem(position));
        selected = position;

        webView.getSettings().setJavaScriptEnabled(true);
        url_str = "";
        String desc = "";

        if (type.getText().toString().contains("Wedding")) {
            url_str = "https://www.catherineohara.com";

        } else if (type.getText().toString().contains("Fashion")) {
            url_str = "https://anna-tabakova.squarespace.com";
        } else {
            url_str = "https://www.crimsonphotos.ca";
        }

        t.start();

        webView.loadUrl(url_str);
        type.setText("");
        desc_text.setText(desc);
        desc_text.setVisibility(View.VISIBLE);
        arrayad.notifyDataSetChanged();
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
                /** TODO: notifcations
                 //sleep
                 try {
                 Thread.sleep(1000);                 //1000 milliseconds is one second.
                 } catch (InterruptedException ex) {
                 Thread.currentThread().interrupt();
                 }
                 ;
                 callNotification(this);
                 **/
                break;

            /** TODO add call back
             // intent to call dialer
             case call:
             Uri uri3 = Uri.parse("tel:5089511273");
             Intent i3 = new Intent(Intent.ACTION_CALL, uri3);
             if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
             return;
             }
             startActivity(i3);
             break;
             **/
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

            case R.id.delete:

                arrayList.remove(selected);
                arrayad.notifyDataSetChanged();
                break;
        }
        return true;
    }

    //thread connects to portfolio, gets response code, JSON search results,
    //places data into Log and sends messages to display data on UI
    Runnable background = new Runnable() {
        public void run() {

            if (Looper.myLooper() == null) {
                Looper.prepare();
            }

            StringBuilder builder = new StringBuilder();
            String Url = url_str+"/?format=json"; //get squarespace json
            InputStream is = null;

            try {
                URL url = new URL(Url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.e("JSON", "The response is: " + response);
                //if response code not 200, end thread
                if (response != 200) return;
                is = conn.getInputStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } catch (IOException e) {
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }

            //convert StringBuilder to String
            String readJSONFeed = builder.toString();
            Log.e("JSON", readJSONFeed);

            //decode JSON
            try {

                JSONObject obj = new JSONObject(readJSONFeed);

                String title = obj.getJSONObject("website").getString("siteTitle");
                Log.i("JSON", "title " + title);

                /**
                 String desc = obj.getJSONObject("website").getString("siteDescription");
                 Log.i("JSON", "description " + desc);
                 String email = obj.getJSONObject("website").getString("contactEmail");
                 Log.i("JSON", "email " + email);
                 String phone = obj.getJSONObject("website").getString("contactPhoneNumber");
                 Log.i("JSON", "phone " + phone);
                 **/

                Message msg = handler.obtainMessage();
                Message msg2 = handler.obtainMessage();
                msg.obj = title;
                msg2.obj =
                handler.sendMessage(msg);

            } catch (JSONException e) {
                e.getMessage();
                e.printStackTrace();
            }
        }

    };
}