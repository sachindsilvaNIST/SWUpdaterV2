package com.nidec.swupdater.v2.ui;
/**
 * Action :
 * 1. Check if the previous update requires a reboot.
 * 2. If "Yes" --> Show "UpdateCompletionActivity" with "Requires Reboot" message.
 * 3. If "No" --> Move to "DownloadStateCheckActivity" -- Checking the download state.
 *
 */

import android.app.Activity;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.UpdateEngine;

import android.util.Log;

import android.widget.TextView;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.UpdateManager;
import com.nidec.swupdater.v2.UpdaterState;

public class RebootCheckActivity extends Activity {

    private static final String TAG_REBOOT_CHECK_ACTIVITY = "RebootCheckActivity";


    private TextView mTextViewUpdaterState;

    private final UpdateManager mUpdateManager = new UpdateManager(new UpdateEngine(), new Handler());

    private boolean isRebootRequiredState = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot_check);

        mTextViewUpdaterState = findViewById(R.id.TextViewUpdaterState);


        /** 1. Firstly, we need to check whether "REBOOT_REQUIRED" state is set.
         * Or whether `UpdateManager` indicates for "Reboot needed".
         *
         *
         * Case 1 : If `UpdateManager` returns "REBOOT_REQUIRED", Move to `UpdateCompletionActivity`
         *
         * Case 2 : Otherwise, Move to next state --> `DownloadStateCheckActivity`, to check whether the current state is in "Download State" or not.
         *
         */

        mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);

        /**
         *  Synchronize with the existing Update Engine Status.
         */


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /** 2. Binding to UpdateEngine invokes onStatusUpdate callback,
         * persisted UpdaterState has to be loaded and prepared beforehand.
         */
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "BINDING `RebootCheckActivity.java`");
        mUpdateManager.bind();

        /**
         * 3. After binding, check the UpdaterState's currentState
         */

        int currentState = mUpdateManager.getUpdaterState();
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "onResume() => currentState = " + UpdaterState.getStateText(currentState));

        /**
         * Handle the state
         */

        handleState(currentState);

    }

    @Override
    protected void onPause() {
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UNBINDING --> `RebootCheckActivity.java`");
        super.onPause();
        mUpdateManager.unbind();
    }

    /**
     * This function callback is invoked, when the SWUpdaterV2's internal state changes.
     */

    private void onUpdaterStateChange(int newState) {
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UpdaterStateChange state = " + UpdaterState.getStateText(newState) + "/" + newState);
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "Current State = " + newState);

        /**
         * 4. Always run handleState on UI Thread.
         */
        runOnUiThread(() -> handleState(newState));
    }


    /**
     * This function will be called once we're bound.
     * It will check immediately and synchronizes the SWUpdaterV2's state with the current Engine Status.
     */


    /**
     * Handles the current state logic :
     *
     * Case 1 : If REBOOT_REQUIRED --> Go to `UpdateCompletionActivity`
     *
     * Case 2 : Else --> Go to `DownloadStateCheckActivity`
     */

    private void handleState(int state) {
        // Update the UI Text --> activity_reboot_check.xml
        String stateText = UpdaterState.getStateText(state);
        mTextViewUpdaterState.setText(stateText + "/" + state);

        if(state == UpdaterState.REBOOT_REQUIRED){
            Log.d(TAG_REBOOT_CHECK_ACTIVITY, "REBOOT_REQUIRED -> Starting UpdateCompletionActivity...");
            startActivity(new Intent(this, UpdateCompletionActivity.class));
            finish();
        } else {
            // For all other states, Move to `DownloadStateCheckActivity.java`
            Log.d(TAG_REBOOT_CHECK_ACTIVITY,"No Reboot Required -> Starting DownloadStateCheckActivity");
            startActivity(new Intent(this, DownloadStateCheckActivity.class));
            finish();
        }

    }

}


































