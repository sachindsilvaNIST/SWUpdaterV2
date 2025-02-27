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



public class OTAPackageAvailableActivity extends Activity {

    private static final String TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY = "OTAPackageAvailableActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_package_available);


        Button mUpdateYesButton = findViewById(R.id.mOTAPackageAvailableYesButton);
        Button mUpdateNoButton = findViewById(R.id.mOTAPackageAvailableNoButton);


        // IF "YES" BUTTON WAS PRESSED : If the OTA Update Available --> Start the download process.
        // E.g : UpdateManager.getInstance().startDownload()
        // Goto Progress Screen Activity --> `ProgressScreenActivity.java`
        mUpdateYesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProgressScreenActivity.class);
            startActivity(intent);
            finish();

        });

        // ===> Todo ---> DOUBT!!!!
        // IF "NO" BUTTON WAS PRESSED : Cancel the update --> Back to "OTA Package Checker" Screen
        // E.g : UpdateManager.getInstance().cancelDownload()
        // Back to "OTA Package Checker" Screen --> `OTAPackageCheckerActivity.java`
        // <=== Todo ---> DOUBT!!!!
        mUpdateNoButton.setOnClickListener(v -> {
            Intent intent = new Intent(this,OTAPackageCheckerActivity.class);
            startActivity(intent);
            finish();

        });



    }
























}
