package com.example.findtographer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static android.R.attr.tag;
import static android.R.attr.visible;
import static android.view.View.VISIBLE;
import static com.example.findtographer.R.id.fab;
import static com.example.findtographer.R.id.web;
import static com.example.findtographer.R.id.web1;

public class Photographer extends AppCompatActivity {

    private WebView webview;
    private EditText url;
    private TextView enter;
    private TextView siteDesc;
    private FloatingActionButton fab;


    //messages from background thread contain data for UI
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String title = (String) msg.obj;

           while (siteDesc.getText().toString() == "")
               siteDesc.append(title); //use json info for profile
            enter.setVisibility(View.GONE);
            url.setVisibility(View.GONE);
            siteDesc.setVisibility(View.VISIBLE);


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photographer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_stat_name);

        webview = (WebView) findViewById(web1);
        webview.setWebViewClient(new WebViewClient());

        enter = (TextView) findViewById(R.id.enterLink);

        // background thread is json parser
        final Thread t = new Thread(background);



        url = (EditText) findViewById(R.id.portURL);
        siteDesc = (TextView) findViewById(R.id.siteDesc);

        url.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {

                /*TODO: progress bar */

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    webview.getSettings().setJavaScriptEnabled(true);
                    webview.loadUrl(url.getText().toString());
                    webview.setVisibility(VISIBLE);

                    return true;
                }
                return false;
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Profile created", Snackbar.LENGTH_SHORT)
                        .setDuration(500)
                        .setAction("Action", null).show();

                t.start();

                /* TODO: intent to profile view */
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.browse:
                intent = new Intent(this, Findtographer.class);
                startActivity(intent);
                break;

            case R.id.back:
                intent = new Intent(this, Login.class);
                startActivity(intent);
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
            String Url = url.getText().toString()+"/?format=json"; //get squarespace json
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
                msg.obj = title;
                handler.sendMessage(msg);



            } catch (JSONException e) {
                e.getMessage();
                e.printStackTrace();
            }
        }

    };
}

