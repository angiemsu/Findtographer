package com.example.findtographer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.example.findtographer.R.id.buttonA;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private Button photo, cust;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_stat_name);

        photo = (Button) findViewById(R.id.buttonPho);
        photo.setOnClickListener(this);
        cust = (Button) findViewById(R.id.buttonCust);
        cust.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()) {
            case R.id.buttonPho:
                intent = new Intent(this, Photographer.class);

                startActivity(intent);
                break;
            case R.id.buttonCust:
                intent = new Intent(this, Findtographer.class);
                startActivity(intent);
                break;


        }
    }
}
