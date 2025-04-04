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
import android.widget.TextView;

import com.nidec.swupdater.v2.R;

import com.nidec.swupdater.v2.UpdateManager;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.UpdateConfig;


import com.nidec.swupdater.v2.util.UpdateEngineErrorCodes;
import com.nidec.swupdater.v2.util.UpdateEngineProperties;
import com.nidec.swupdater.v2.util.UpdateEngineStatuses;
import com.nidec.swupdater.v2.util.UpdateConfigs;

import com.nidec.swupdater.v2.ui.UpdateManagerHolder;


public class DownloadStateCheckActivity extends Activity {

    private static final String TAG_DOWNLOAD_STATE_CHECK_ACTIVITY = "DownloadStateCheckActivity";

    private TextView mTextViewDownloadStateCheck;



    /**
     * Creating local UpdateManager instance...
     */
    private UpdateManager mUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_state_check);


        mTextViewDownloadStateCheck = findViewById(R.id.TextViewDownloadStateCheck);


        // Retrieve the shared UpdateManager Instance.
        mUpdateManager = UpdateManagerHolder.getInstance();


        /**
         * We need to get the callback instance to check the current state of UpdateManager.
         *
         * Set the state change callback to handle if the state changes in this Phase `DownloadStateCheckActivity`
         *
         */

        mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);

    }

    @Override
    protected void onResume() {
        super.onResume();
        /** 2. Binding to UpdateEngine invokes onStatusUpdate callback,
         * persisted UpdaterState has to be loaded and prepared beforehand.
         */
        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "BINDING `DownloadStateCheckActivity`...");
        mUpdateManager.bind();


        /**
         *
         * 3. After binding, we do immediate Engine Status Sync.
         */

        synchronizeWithEngineStatus();




//        /**
//         * 3. After binding, check the UpdaterState's currentState
//         */
//
//        int currentState = mUpdateManager.getUpdaterState();
//        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "onResume() => currentState = " + UpdaterState.getStateText(currentState));
//
//        /**
//         * Handle the state
//         */
//
//        runOnUiThread(()-> handleState(currentState));


    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "UNBINDING `DownloadStateCheckActivity`...");
        mUpdateManager.unbind();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    /**
     * Check Real Engine Status Immediately (The binder state) and set
     * our ephemeral `UpdaterState` if its "DOWNLOADING"
     */

    private void synchronizeWithEngineStatus() {
        int engineStatus = mUpdateManager.getEngineStatus();
        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "synchronizeWithEngineStatus() => engineStatus No. = " + engineStatus);
        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "Engine Status => " + UpdateEngineStatuses.getStatusText(engineStatus));


        /**
         * If the engine status indicates a running update,
         * we forcibly set our ephemeral UpdaterState to `RUNNING`
         *
         */

        if(isEngineBusy(engineStatus)) {
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "Engine is in a busy/ in progress state => set UpdaterState to RUNNING");
            // we use setUpdaterStateSilent to avoid invalid transition errors.

            // NOTE : D has set this function visibility to `Public`!!
            mUpdateManager.setUpdaterStateSilent(UpdaterState.RUNNING);

        } else if(engineStatus == UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT) {
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "Engine says UPDATED_NEED_REBOOT =>  set UpdaterState to REBOOT_REQUIRED");
            mUpdateManager.setUpdaterStateSilent(UpdaterState.REBOOT_REQUIRED);
        } else {
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "Engine is not busy => Keep ephemeral state as IDLE or etc...");
        }

        /**
         * Handle the ephemeral state we ended up with
         */

        handleState(mUpdateManager.getUpdaterState());


    }

    private boolean isEngineBusy(int engineStatus) {

        return(engineStatus == UpdateEngine.UpdateStatusConstants.DOWNLOADING
        ||     engineStatus == UpdateEngine.UpdateStatusConstants.VERIFYING
        ||     engineStatus == UpdateEngine.UpdateStatusConstants.FINALIZING
        );

    }




    /**
     * This function callback is invoked, when the SWUpdaterV2's internal state changes.
     */

    private void onUpdaterStateChange(int newState) {
        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "onUpdaterStateChange state => " + UpdaterState.getStateText(newState) + "/" + newState);
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
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "CURRENT RUNNING STATE = " + state);
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "RUNNING : --> OTA Update is in progress..., --> Switching to `ProgressScreenActivity.java`");
            startActivity(new Intent(this, ProgressScreenActivity.class));
            finish();
        } else if(state == UpdaterState.REBOOT_REQUIRED) {
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "CURRENT RUNNING STATE = " + state);
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "REBOOT_REQUIRED -> Starting UpdateCompletionActivity...");
            startActivity(new Intent(this, UpdateCompletionActivity.class));
            finish();
        } else {
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "CURRENT RUNNING STATE = " + state);
            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "NO DOWNLOAD IN PROGRESS --> Switching to `OTAPackageCheckerActivity.java`");
            startActivity(new Intent(this, OTAPackageCheckerActivity.class));
            finish();
        }
    }





//    private void handleState(int state) {
//        Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "Entering `handleState()` of `DownloadStateCheckActivity.java`...");
//
//        switch (state) {
//
//            case UpdaterState.IDLE:
//                Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "IDLE...");
//                break;
//            case UpdaterState.ERROR:
//                Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "ERROR!!");
//                break;
//            case UpdaterState.PAUSED:
//                Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "PAUSED...");
//                break;
//            case UpdaterState.SLOT_SWITCH_REQUIRED:
//                Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "SLOT_SWITCH_REQUIRED...");
//                break;
//
//            case UpdaterState.RUNNING :
//            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "RUNNING : --> OTA Update is in progress..., --> Switching to `ProgressScreenActivity.java`");
//            mTextViewDownloadStateCheck.setText("RUNNING....");
//            startActivity(new Intent(this,ProgressScreenActivity.class));
//                finish();
//                break;
//            case UpdaterState.REBOOT_REQUIRED:
//                Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY,"REBOOT_REQUIRED : --> Switching to RebootCheckActivity.java" );
//                startActivity(new Intent(this, RebootCheckActivity.class));
//                finish();
//                break;
//            default:
//            Log.d(TAG_DOWNLOAD_STATE_CHECK_ACTIVITY, "NO DOWNLOAD IN PROGRESS --> Switching to `OTAPackageCheckerActivity.java`");
//            startActivity(new Intent(this, OTAPackageCheckerActivity.class));
//            finish();
//        }
//
//
//    }


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
