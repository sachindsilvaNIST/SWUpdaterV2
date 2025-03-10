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

/**
 * Action:
 * 1. Show a screen that says "System is up-to-date.
 * 2. Offer a "Re-check" button which leads back to "OTAPackageCheckerActivity"
 *
 */




public class SystemUpToDateActivity extends Activity {

    private static final String TAG_SYSTEM_UP_TO_DATE_ACTIVITY = "SystemUpToDateActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_up_to_date);

        // Get the `ReCheck` Button UI Element ID
        Button mReCheckButton = findViewById(R.id.mSystemUpToDateReCheckButton);

        mReCheckButton.setOnClickListener(v -> {
            // Re-check for OTA Package Updates.
            // This will redirect again to `OTAPackageCheckerActivity.java` page.
            Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "RECHECK Button was pressed...");
            Intent intent = new Intent(this, OTAPackageCheckerActivity.class);
            startActivity(intent);
            finish();
        });

    }
}
