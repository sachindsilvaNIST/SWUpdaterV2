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

        this.mTextViewUpdaterState = findViewById(R.id.TextViewUpdaterState);


        /** 1. Firstly, we need to check whether "REBOOT_REQUIRED" state is set.
         * Or whether `UpdateManager` indicates for "Reboot needed".
         *
         *
         * Case 1 : If `UpdateManager` returns "REBOOT_REQUIRED", Move to `UpdateCompletionActivity`
         *
         * Case 2 : Otherwise, Move to next state --> `DownloadStateCheckActivity`, to check whether the current state is in "Download State" or not.
         *
         */

        this.mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);

        isRebootRequired();
    }


    private void isRebootRequired() {
        // Check the UpdaterState is "REBOOT_REQUIRED"

        if(isRebootRequiredState) {
            /**
             * Case 1 : If `UpdateManager` returns "REBOOT_REQUIRED", Move to `UpdateCompletionActivity`
             */

            Log.d(TAG_REBOOT_CHECK_ACTIVITY, "REBOOT is required ---> Moving to UpdateCompletionActivity...");
            startActivity(new Intent(this, UpdateCompletionActivity.class));
            finish();
        } else {
            /**Case 2 : Otherwise, Move to next state --> `DownloadStateCheckActivity`, to check whether the current state is in "Download State" or not.\
             */

            Log.d(TAG_REBOOT_CHECK_ACTIVITY, "No Reboot required ---> Moving to DownloadStateCheckActivity...");
            startActivity(new Intent(this, DownloadStateCheckActivity.class));
            finish();
        }
    }


    /**
     * Invoked when SWUpdaterV2 app changes its state.
     *
     * The value of {@code state} will be one in {@link UpdaterState}
     *
     */

    private void onUpdaterStateChange(int state) {
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "onUpdaterStateChange state = " + UpdaterState.getStateText(state) + "/" + state);

        runOnUiThread(() -> {
            setUiUpdaterState(state);

            if(state == UpdaterState.IDLE) {
                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UpdateState.IDLE has been invoked...");

            } else if(state == UpdaterState.RUNNING) {
                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UpdateState.RUNNING has been invoked...");

            } else if(state == UpdaterState.PAUSED) {
                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UpdateState.PAUSED has been invoked...");

            } else if(state == UpdaterState.ERROR) {
                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UpdateState.ERROR has been invoked...");

            } else if(state == UpdaterState.SLOT_SWITCH_REQUIRED) {
                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UpdateState.SLOT_SWITCH_REQUIRED has been invoked...");

            } else if(state == UpdaterState.REBOOT_REQUIRED) {
                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UpdateState.REBOOT_REQUIRED has been invoked...");
            }
        });
    }

    /**
     * @param state --> SWUpdaterV2's state
     */

    private void setUiUpdaterState(int state) {

        /**
         * Note : the `state` value for "REBOOT_REQUIRED" = "5".
         * {@link UpdaterState.STATE_MAP}
         */
        String stateText = UpdaterState.getStateText(state);

        if(stateText.equals("REBOOT_REQUIRED")) {
            isRebootRequiredState = true;
        } else {
            isRebootRequiredState = false;
        }
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "`stateText` = " + stateText);
        mTextViewUpdaterState.setText(stateText + "/" + state);
    }

}


































