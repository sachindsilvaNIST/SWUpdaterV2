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

public class OTAPackageCheckerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_package_checker);

        boolean mUpdateAvailable = checkForOTAUpdate();

        // Check whether OTA Package Update available or not
        // IF `!mUpdateAvailable` --> TRUE => `mUpdateAvailable` --> FALSE
        if(!mUpdateAvailable) {
            Intent intent = new Intent(this,SystemUpToDateActivity.class);
            startActivity(intent);
        } else {
            // Display the OTA Package Available screen --> `OTAPackageAvailableActivity.java`
            Intent intent = new Intent(this,OTAPackageAvailableActivity.class);
            startActivity(intent);
        }
        finish();

    }

    private boolean checkForOTAUpdate() {
        // Possibly use your existing SWUpdater V1 logic to see if an update is found.
        // E.G: Fetch config from update server or parse JSON from local storage.

        return false;
    }
}
