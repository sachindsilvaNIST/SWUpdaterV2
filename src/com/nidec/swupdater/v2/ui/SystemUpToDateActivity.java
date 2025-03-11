package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Intent;

import android.os.Bundle;
import android.os.UpdateEngine;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.util.UpdateConfigs;
import com.nidec.swupdater.v2.util.UpdateEngineProperties;
import com.nidec.swupdater.v2.util.UpdateEngineStatuses;
import com.nidec.swupdater.v2.util.UpdateEngineErrorCodes;
import com.nidec.swupdater.v2.UpdateConfig;
import com.nidec.swupdater.v2.UpdateManager;

import com.nidec.swupdater.v2.ui.UpdateManagerHolder;


/**
 * Action:
 * 1. Show a screen that says "System is up-to-date.
 * 2. Offer a "Re-check" button which leads back to "OTAPackageCheckerActivity"
 *
 */




public class SystemUpToDateActivity extends Activity {

    private static final String TAG_SYSTEM_UP_TO_DATE_ACTIVITY = "SystemUpToDateActivity";


    private UpdateManager mUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_up_to_date);

        // Retrieve the shared UpdateManager Instance...
        mUpdateManager = UpdateManagerHolder.getInstance();
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY,"mUpdateManager INSTANCE --> " + mUpdateManager);

        mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);

        // Get the `ReCheck` Button UI Element ID
        Button mReCheckButton = findViewById(R.id.mSystemUpToDateReCheckButton);

        mReCheckButton.setOnClickListener(v -> {
            Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "RECHECK Button was pressed...");

            int currentEngineStatus = mUpdateManager.getEngineStatus();
            String currentEngineStatusToText = UpdaterState.getStateText(currentEngineStatus);

            if(currentEngineStatus == UpdaterState.ERROR) {
                forceSwitchingToIDLEState();
            } else if(currentEngineStatus == UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT) {
                requiresReboot();
            } else if (currentEngineStatus == UpdaterState.IDLE) {
                switchingToIDLEState();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mUpdateManager.bind();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUpdateManager.unbind();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void onUpdaterStateChange(int newState) {
        int currentEngineStatus = mUpdateManager.getEngineStatus();
        String currentEngineStatusToText = UpdaterState.getStateText(currentEngineStatus);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "UpdaterStateChange state No. = " + UpdaterState.getStateText(newState) + "/" + newState);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "Current State = " + newState);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "CurrentEngineStatus No. = " + currentEngineStatus);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "CurrentEngineStatus in codeName = " + currentEngineStatusToText);

    }


    private void requiresReboot() {
        // mUpdateManager.setUpdaterStateSilent(UpdaterState.REBOOT_REQUIRED);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, " Requires REBOOT!!! --> Switching to `RebootCheckActivity.java`");
        startActivity(new Intent(this,RebootCheckActivity.class));
        finish();
        return;
    }

    private void forceSwitchingToIDLEState() {
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, " Update Engine says No ERROR switched to IDLE !! --> Switching to `OTAPackageCheckerActivity.java`");
        // Re-check for OTA Package Updates.
        // This will redirect again to `OTAPackageCheckerActivity.java` page.
        mUpdateManager.setUpdaterStateSilent(UpdaterState.IDLE);
        startActivity(new Intent(this,OTAPackageCheckerActivity.class));
        finish();
        return;
    }

    private void switchingToIDLEState() {
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, " Update Engine says IDLE !! --> Switching to `OTAPackageCheckerActivity.java`");
        // Re-check for OTA Package Updates.
        // This will redirect again to `OTAPackageCheckerActivity.java` page.
        startActivity(new Intent(this,OTAPackageCheckerActivity.class));
        finish();
        return;
    }

}
