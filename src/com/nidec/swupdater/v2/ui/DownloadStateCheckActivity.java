package com.nidec.swupdater.v2.ui;
/**
 * Action:
 * 1. Check if an OTA Package download is already in progress.
 * 2. If "Yes" --> Goto "ProgressScreenActivity"
 * 3. If "No" --> Goto "OTAPackageCheckerActivity"
 */

import android.app.Activity;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.UpdateEngine;

import android.util.Log;

import com.nidec.swupdater.v2.R;

import com.nidec.swupdater.v2.UpdateManager;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.UpdateConfig;


import com.nidec.swupdater.v2.util.UpdateEngineErrorCodes;
import com.nidec.swupdater.v2.util.UpdateEngineProperties;
import com.nidec.swupdater.v2.util.UpdateEngineStatuses;
import com.nidec.swupdater.v2.util.UpdateConfigs;


public class DownloadStateCheckActivity extends Activity {

    private static final String TAG_DOWNLOAD_STATE_CHECK_ACTIVITY = "DownloadStateCheckActivity";



    /**
     * Creating local UpdateManager intance...
     */
    private final UpdateManager mUpdateManager = new UpdateManager(new UpdateEngine(), new Handler());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_state_check);

        /**
         * We need to get the callback instance to check the current state of UpdateManager.
         *
         * Set the state change callback to handle if the state changes in this Phase `DownloadStateCheckActivity`
         *
         */

        this.mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);

    }

    @Override
    protected void onResume() {
        super.onResume();
        /** 2. Binding to UpdateEngine invokes onStatusUpdate callback,
         * persisted UpdaterState has to be loaded and prepared beforehand.
         */
        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "BINDING `DownloadStateCheckActivity`...");
        this.mUpdateManager.bind();

        /**
         * Check if we are in a "RUNNING" or "DOWNLOADING" State.
         */
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "UNBINDING `DownloadStateCheckActivity`...");
        this.mUpdateManager.unbind();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }



    /**
     * This function callback is invoked, when the SWUpdaterV2's internal state changes.
     */

    private void onUpdaterStateChange(int newState) {
        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "onUpdaterStateChange state = " + UpdaterState.getStateText(newState) + "/" + newState);
        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "Current State = " + newState);

        /**
         * Run the `handleState` on UI Thread.
         */

        runOnUiThread(()-> handleState(newState));
    }


    /**
     *
     * Case 1 : If the current state is in "RUNNING", i.e, OTA Software Update is in progress,
     *          then, transition to `ProgressScreenActivity.java`
     *
     * Case 2 : Otherwise,
     *                  Go to `OTAPackageCheckerActivity.java`
     */

    private void handleState(int state) {
        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "Entering `handleState()` of `DownloadStateCheckActivity.java`...");

        if(state == UpdaterState.RUNNING) {
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "RUNNING : --> OTA Update is in progress..., --> Switching to `ProgressScreenActivity.java`");
            startActivity(new Intent(this, ProgressScreenActivity.class));
            finish();
        } else if(state == UpdaterState.REBOOT_REQUIRED) {
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "REBOOT_REQUIRED -> Starting UpdateCompletionActivity...");
            startActivity(new Intent(this, UpdateCompletionActivity.class));
            finish();
        } else {
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "NO DOWNLOAD IN PROGRESS --> Switching to `OTAPackageCheckerActivity.java`");
            startActivity(new Intent(this, OTAPackageCheckerActivity.class));
            finish();
        }
    }
}







































//package com.nidec.swupdater.v2.ui;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//
//import android.content.Intent;
//
//import android.os.Bundle;
//
//import android.util.Log;
//
//
//import android.widget.Button;
//import android.widget.TextView;
//
//import com.nidec.swupdater.v2.R;
//
////import com.nidec.swupdater.v2.util.UpdateConfigs;
////import com.nidec.swupdater.v2.UpdateConfig;
//
///**
// * Action:
// * 1. Check if an OTA Package download is already in progress.
// * 2. If "Yes" --> Goto "ProgressScreenActivity"
// * 3. If "No" --> Goto "OTAPackageCheckerActivity"
// */
//
//
//public class DownloadStateCheckActivity extends Activity{
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_download_state_check);
//    }
//
//
//    /**
//     * Function Implementation : Check whether the OTA Package download is in progress..
//     */
//
//    private boolean isDownloadInProgress() {
//        // Query your UpdateManager or saved state in SharedPrefs, etc.\
//        // Eg: return DownloadManager.isDownloading();
//        return false;
//    }
//
//
//}
