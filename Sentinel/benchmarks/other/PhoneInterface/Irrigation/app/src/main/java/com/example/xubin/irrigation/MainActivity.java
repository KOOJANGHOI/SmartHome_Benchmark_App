package com.example.xubin.irrigation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText gatewayIP;
    private EditText inchesPerWeek;
    private EditText weatherZipCode;
    private EditText daysToWaterOn;
    private EditText inchesPerMinute;
    private Button submit_button;
    protected static Context context;
    private Helper helper = new Helper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();
        gatewayIP = (EditText) findViewById(R.id.gatewayip);
        inchesPerWeek = (EditText) findViewById(R.id.week);
        weatherZipCode = (EditText) findViewById(R.id.zip);
        daysToWaterOn = (EditText) findViewById(R.id.water);
        inchesPerMinute = (EditText) findViewById(R.id.minute);
        submit_button = (Button) findViewById(R.id.submit);

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Object> params = new ArrayList<>();
                params.add(Double.parseDouble(inchesPerWeek.getText().toString()));
                params.add(Integer.parseInt(weatherZipCode.getText().toString()));
                params.add(Integer.parseInt(daysToWaterOn.getText().toString()));
                params.add(Double.parseDouble(inchesPerMinute.getText().toString()));

                String ip = gatewayIP.getText().toString();
                new MakeRequestTask(params).execute(ip, "getIrrigationInfo");

            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private class MakeRequestTask extends AsyncTask<String, String, Void>{
        private List<Object> params;
        public MakeRequestTask(List<Object> argus) {
            this.params = argus;
        }
        @Override
        protected Void doInBackground(String... argus) {
            if (helper.httpclient == null) {
                helper.setConnection(argus[0]);
            }

            helper.makeRequest(argus[0],params, argus[1]);

            return null;
        }
    }
}
