package com.nidec.swupdater.v2.ui;

/**
 * Action :
 * 1. Show the progress (percentage) of download the OTA Package.
 * 2. If download finishes successfully, or the UpdateEngine signals "Requires Reboot", go to "UpdateCompletionActivity"
 * 3. If user presses "Cancel", then go back to "OTAPackageCheckerActivity" / "UpdateCompletionActivity"
 */

import android.app.Activity;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.UpdateEngine;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.UpdateManager;
import com.nidec.swupdater.v2.util.UpdateConfigs;
import com.nidec.swupdater.v2.UpdateConfig;

import com.nidec.swupdater.v2.ui.UpdateManagerHolder;


public class ProgressScreenActivity extends Activity {
    private static final String TAG_PROGRESS_SCREEN_ACTIVITY = "ProgressScreenActivity";


    private ProgressBar mProgressBar;
    private Button mCancelDownloadButton;

    private TextView mProgressScreenPercentDisplay;


    private UpdateManager mUpdateManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_screen);

        mProgressBar = findViewById(R.id.progressBar);
        mCancelDownloadButton = findViewById(R.id.buttonCancelDownload);
        mProgressScreenPercentDisplay = findViewById(R.id.TextViewProgressScreenPercentDisplay);


        // Retrieve the shared UpdateManager Instance..
        mUpdateManager = UpdateManagerHolder.getInstance();

        /**
         * Set the callbacks for Update Status Changes and Progress Status Changes
         */

        mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);
        mUpdateManager.setOnProgressUpdateCallback(this::onProgressChanged);


        /**
         * Callback for Engine - complete callback..
         */

//        mUpdateManager.setOnEngineCompleteCallback(this::onEngineComplete);


        /**
         * BUTTON : `Cancel Update` Action Listener
         */

        mCancelDownloadButton.setOnClickListener((View v) -> {

            // If the user cancels the OTA Update.

            try {
                mUpdateManager.cancelRunningUpdate();

            } catch (UpdaterState.InvalidTransitionException e) {
                Log.e(TAG_PROGRESS_SCREEN_ACTIVITY, "FAILED TO CANCEL THE UPDATE... : ",e);
            }

            /**
             *  If user presses "Cancel Update" button, then go back to "OTAPackageCheckerActivity.java"
             *
             */

            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "USER has pressed `Cancel Update` BUTTON, SWITCHING --> OTAPackageCheckerActivity.java");

            startActivity(new Intent(this, OTAPackageCheckerActivity.class));
            finish();
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Bind `ProgressScreenActivity.java`, so that we can get the UpdateEngine status updates.
        mUpdateManager.bind();

        /**
         * Manually synchronize with the UpdateEngine, in case the update is
         * already done or in REBOOT_REQUIRED state.
         */

        synchronizeEngineState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUpdateManager.unbind();

        // Remove callbacks to prevent memory leaks
        mUpdateManager.setOnStateChangeCallback(null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * This function ensures that we don't show "IDLE", if the UpdateEngine has already completed the download process,
     * and at "REBOOT_REQUIRED" or some other state(s)...
     *
     * We forcibly read the engine status and update the internal UpdaterState accordingly..
     */

    private void synchronizeEngineState() {

        int currentEngineStatus = mUpdateManager.getEngineStatus();
        String currentEngineStatusToText = UpdaterState.getStateText(currentEngineStatus);
        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Engine Status Code => " + currentEngineStatus);
        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Engine Status Code (TEXT) => " + currentEngineStatusToText);

        /**
         * Now, if the UpdateEngine is in "UPDATED_NEED_REBOOT" state,
         * we set the app state to "REBOOT_REQUIRED"
         */

        if(currentEngineStatus == UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT) {
//            mUpdateManager.setUpdaterStateSilent(UpdaterState.REBOOT_REQUIRED);
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, " Requires REBOOT!!! --> Switching to `RebootCheckActivity.java`");
            startActivity(new Intent(this,RebootCheckActivity.class));
            finish();
            return;
        }


        /**
         * The SWUpdaterV2 app has to check the current state to see if we should move on or not.
         */
        checkIfComplete(mUpdateManager.getUpdaterState());
    }

    /**
     * This function will be called when the SWUpdaterV2's Internal State changes.
     */

    private void onUpdaterStateChange(int newState) {

        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "onUpdaterStateChange => " + UpdaterState.getStateText(newState));

        runOnUiThread(() -> checkIfComplete(newState));

    }


    /**
     * This function will be called on every download progress update (0.0 to 1.0).
     */

    private void onProgressChanged(double progress) {
        runOnUiThread(() -> {
            int percent = (int) (100 * progress);
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Current Download Progress => " + percent + "%");
            mProgressBar.setProgress(percent);
            mProgressScreenPercentDisplay.setText(percent + "%");
            if(percent == 99) {
                percent = percent + 1;
                int currentUpdateState = mUpdateManager.getUpdaterState();
                mProgressScreenPercentDisplay.setText(percent + "%");
                checkIfComplete(currentUpdateState);
            }
            if(percent == 100) {
                int currentUpdateState = mUpdateManager.getUpdaterState();
                mProgressScreenPercentDisplay.setText("Processing. Please wait...");
                checkIfComplete(currentUpdateState);
            }
        });
    }

    /**
     *
     * If the update is complete or the engine status is `REBOOT_REQUIRED`,
     *      Switch to `RebootCheckActivity.java`
     *
     */

    private void checkIfComplete(int updaterState) {

        // On 100% progress, signaled by `REBOOT_REQUIRED`,or engine-complete callback.

        if(updaterState == UpdaterState.REBOOT_REQUIRED) {
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Updater State says REBOOT_REQUIRED.... --> Switching to RebootCheckActivity.java");
            startActivity(new Intent(this,RebootCheckActivity.class));
            finish();
        } else if (updaterState == UpdaterState.SLOT_SWITCH_REQUIRED) {
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Updater State says SLOT_SWITCH_REQUIRED.... --> Switching to RebootCheckActivity.java");
            startActivity(new Intent(this,RebootCheckActivity.class));
            finish();
        } else if (updaterState == UpdaterState.ERROR) {
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Updater State says ERROR.... --> Switching to SystemUpToDateActivity.java");
            startActivity(new Intent(this,SystemUpToDateActivity.class));
            finish();
        }

    }

}