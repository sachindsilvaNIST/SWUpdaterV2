package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;


import com.nidec.swupdater.v2.R;
//import com.nidec.swupdater.v2.util.UpdateConfigs;
//import com.nidec.swupdater.v2.UpdateConfig;


/**
 * Action :
 * 1. Show the progress (percentage) of download the OTA Package.
 * 2. If download finishes successfully, or the UpdateEngine signals "Requires Reboot", go to "UpdateCompletionActivity"
 * 3. If user presses "Cancel", then go back to "OTAPackageCheckerActivity"
 */


public class ProgressScreenActivity extends Activity {
    private static final String TAG_PROGRESS_SCREEN_ACTIVITY = "ProgressScreenActivity";

    private ProgressBar mProgressBar;
    private Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_screen);


        mProgressBar = findViewById(R.id.mProgressScreenBar);
        mCancelButton = findViewById(R.id.mProgressBarCancelButton);


        // Listen to the download progress callbacks
        // E.g : UpdateManager.getInstance().setOnProgressUpdateCallback(this::onProgressChanged);

        // ===> Todo DOUBT!!
        mCancelButton.setOnClickListener(v -> {
            // If "Cancel" button was pressed, cancel the download.
            // E.g : UpdateManager.getInstance().cancelDownload();
            // Move to "OTA Package Checker" Page --> `OTAPackageCheckerActivity.java`
            Intent intent = new Intent(this,OTAPackageCheckerActivity.class);
            startActivity(intent);
            finish();
        });
        // ===> Todo DOUBT!!

    }

    private void onProgressChanged(int progress) {
        // This is the callback from your updateManager
        runOnUiThread(() -> mProgressBar.setProgress(progress));

        if(progress >= 100) {
            // Download was completed... requires Reboot.
            // ALSO, COULD check the UpdateManager for status

            Intent intent = new Intent(this, UpdateCompletionActivity.class);
            startActivity(intent);
            finish();
        }

        /**
         * Notes:
         * You might already have logic from SWUpdaterV1 that manages the actual engine callbacks and progress updates.
         *
         * Just wire them up in this activity to show the user a progress bar or logs.
         *
         * If the update completes and the final status from the update engine is “REBOOT_REQUIRED,” we move to UpdateCompletionActivity.
         *
         *
         */

    }
















}
