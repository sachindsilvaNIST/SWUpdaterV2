package com.nidec.swupdater.v2.ui;

/**
 * Action :
 * 1. Show the progress (percentage) of download the OTA Package.
 * 2. If download finishes successfully, or the UpdateEngine signals "Requires Reboot", go to "UpdateCompletionActivity"
 * 3. If user presses "Cancel", then go back to "OTAPackageCheckerActivity"
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

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.UpdateManager;
import com.nidec.swupdater.v2.util.UpdateConfigs;
import com.nidec.swupdater.v2.UpdateConfig;

import org.checkerframework.checker.i18nformatter.qual.I18nChecksFormat;


public class ProgressScreenActivity extends Activity {
    private static final String TAG_PROGRESS_SCREEN_ACTIVITY = "ProgressScreenActivity";


    private ProgressBar mProgressBar;
    private Button mCancelDownloadButton;


    private final UpdateManager mUpdateManager = new UpdateManager(new UpdateEngine(), new Handler());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_screen);

        mProgressBar = findViewById(R.id.progressBar);
        mCancelDownloadButton = findViewById(R.id.buttonCancelDownload);

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
        mUpdateManager.setOnProgressUpdateCallback(null);
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
        int engineStatus = mUpdateManager.getUpdaterState();
        String engineStatusText = UpdaterState.getStateText(engineStatus);

        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "engineStatus => " + engineStatus);
        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY,"engineStatusText => " + engineStatusText);


        /**
         * Now, if the UpdateEngine is in "UPDATED_NEED_REBOOT" state,
         * we set the app state to "REBOOT_REQUIRED"
         */

        if(engineStatus == UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT) {
//            mUpdateManager.setUpdaterStateSilent(UpdaterState.REBOOT_REQUIRED);
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, " Requires REBOOT!!! --> Switching to `RebootCheckActivity.java`");
            startActivity(new Intent(this,RebootCheckActivity.class));
            finish();
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
            mProgressBar.setProgress(Integer.parseInt(percent + "%"));
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
        }

    }

}













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
//import android.view.View;
//
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.ProgressBar;
//
//
//import com.nidec.swupdater.v2.R;
////import com.nidec.swupdater.v2.util.UpdateConfigs;
////import com.nidec.swupdater.v2.UpdateConfig;
//
//
//
//
//public class ProgressScreenActivity extends Activity {
//    private static final String TAG_PROGRESS_SCREEN_ACTIVITY = "ProgressScreenActivity";
//
////    private ProgressBar mProgressBar;
////    private Button mCancelButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_progress_screen);
//
//
////        mProgressBar = findViewById(R.id.mProgressScreenBar);
////        mCancelButton = findViewById(R.id.mProgressBarCancelButton);
////
////
////        // Listen to the download progress callbacks
////        // E.g : UpdateManager.getInstance().setOnProgressUpdateCallback(this::onProgressChanged);
////
////        // ===> Todo DOUBT!!
////        mCancelButton.setOnClickListener(v -> {
////            // If "Cancel" button was pressed, cancel the download.
////            // E.g : UpdateManager.getInstance().cancelDownload();
////            // Move to "OTA Package Checker" Page --> `OTAPackageCheckerActivity.java`
////            Intent intent = new Intent(this,OTAPackageCheckerActivity.class);
////            startActivity(intent);
////            finish();
////        });
////        // ===> Todo DOUBT!!
//
//    }
//
////    private void onProgressChanged(int progress) {
////        // This is the callback from your updateManager
////        runOnUiThread(() -> mProgressBar.setProgress(progress));
////
////        if(progress >= 100) {
////            // Download was completed... requires Reboot.
////            // ALSO, COULD check the UpdateManager for status
////
////            Intent intent = new Intent(this, UpdateCompletionActivity.class);
////            startActivity(intent);
////            finish();
////        }
////
////        /**
////         * Notes:
////         * You might already have logic from SWUpdaterV1 that manages the actual engine callbacks and progress updates.
////         *
////         * Just wire them up in this activity to show the user a progress bar or logs.
////         *
////         * If the update completes and the final status from the update engine is “REBOOT_REQUIRED,” we move to UpdateCompletionActivity.
////         *
////         *
////         */
////
////    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//}
