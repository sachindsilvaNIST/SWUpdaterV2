package com.nidec.swupdater.v2.ui;


import android.app.Activity;
import android.app.AlertDialog;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;


import com.nidec.swupdater.v2.R;
//import com.nidec.swupdater.v2.util.UpdateConfigs;
//import com.nidec.swupdater.v2.UpdateConfig;


public class MainActivity extends Activity {
    private static final String TAG_MAIN_ACTIVITY = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Display the XML of `MainActivity` --> `activity_main.xml`
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this,RebootCheckActivity.class);
        startActivity(intent);
        finish();
    }

}
