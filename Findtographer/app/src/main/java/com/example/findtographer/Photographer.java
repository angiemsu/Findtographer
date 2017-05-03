package com.example.findtographer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import static android.view.View.VISIBLE;
import static com.example.findtographer.R.id.fab;
import static com.example.findtographer.R.id.web;
import static com.example.findtographer.R.id.web1;

public class Photographer extends AppCompatActivity {

    private WebView webview;
    private EditText url;
    private FloatingActionButton fab;

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

        url= (EditText) findViewById(R.id.portURL);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Creating profile...",Snackbar.LENGTH_SHORT)
                        .setDuration(500)
                        .setAction("Action", null ).show();


                webview.getSettings().setJavaScriptEnabled(true);
                webview.loadUrl(url.getText().toString());

                webview.setVisibility(VISIBLE);
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

}
