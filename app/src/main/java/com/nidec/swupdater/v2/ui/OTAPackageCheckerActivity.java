package com.nidec.swupdater.v2.ui;

/**
 * Action :
 * 1. Scans the USB pendrive's Update/ folder for the new OTA Configruations.
 * 2. If a new OTA is found => Goto "OTAPackageAvailableActivity"
 * 3. If no OTA Update --> Goto "SystemUpToDateActivity"
 *
 *
 * NOTE : "15, NEW_ROOTFS_VERIFICATION_ERROR" ==> might show no new update or same update found.
 */

import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Intent;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.UpdateManager;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.UpdateConfig;

import com.nidec.swupdater.v2.util.UpdateConfigs;
import com.nidec.swupdater.v2.util.UpdateEngineProperties;
import com.nidec.swupdater.v2.util.UpdateEngineStatuses;
import com.nidec.swupdater.v2.util.UpdateEngineErrorCodes;
import com.nidec.swupdater.v2.util.SelectedUpdateConfigHolder;

import com.nidec.swupdater.v2.ui.UpdateManagerHolder;


import java.util.List;


public class OTAPackageCheckerActivity extends Activity {

    private static final String TAG_OTA_PACKAGE_CHECKER_ACTIVITY = "OTAPackageCheckerActivity";

    private UpdateManager mUpdateManager;

    // Indeterminate Loading Spinner until the Update Engine Status Code = IDLE.
    private ProgressDialog mWaitingSpinner;


    // Avoid Scanning for New updates (checkForNewUpdate()) multiple times in the same session.
    private boolean whetherScannedForNewUpdates = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_package_checker);

        /**
         * Setting Gradient Background
         */
        setGradientBackground();

        // Retrieve the shared UpdateManager Instance..
        mUpdateManager = UpdateManagerHolder.getInstance();
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY,"mUpdateManager INSTANCE --> " + mUpdateManager);


        mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);
        mUpdateManager.setOnEngineStatusUpdateCallback(this::onEngineStatusUpdate);

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Bind the update engine
        mUpdateManager.bind();

        // Immediately check the update engine status
        int currentEngineStatus = mUpdateManager.getEngineStatus();
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "onResume() => currentEngineStatus CODE = " + currentEngineStatus);
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "onResume() => currentEngineStatus IN TEXT = " + UpdateEngineStatuses.getStatusText(currentEngineStatus));

        /**
         *  If Update Engine is stable, Hide the Loading Spinner
         *  If unstable (i.e, if status code = 11), Show the Loading Spinner until the status code = 0 (IDLE)
         */

        if(isStableEngineStatus(currentEngineStatus)) {
            hideWaitingSpinner();
            // Check immediately for new OTA Updates.
            checkForNewUpdate();
        } else {
            showWaitingSpinner("Processing the update setup... Please wait");
        }
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
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "UpdaterStateChange state = " + UpdaterState.getStateText(newState) + "/" + newState);
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "Current State = " + newState);

        runOnUiThread(() -> handleState(newState));
    }

    /**
     *  If Current Engine Status is stable (IDLE, REBOOT_REQUIRED, ERROR) etc, Hide the Loading Spinner and continue the flow.
     *  If Unstable (Status Code = 11), Render the Loading Spinner until Status Code set to IDLE.
     *
     */
    private void onEngineStatusUpdate(int status) {
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "onEngineStatusUpdate() => CURRENT STATUS CODE = " + status);
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "onEngineStatusUpdate() => CURRENT STATUS IN TEXT = " + UpdateEngineStatuses.getStatusText(status));

        if(isStableEngineStatus(status)) {
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "onEngineStatusUpdate() => HIDING Waiting Spinner....");
            hideWaitingSpinner();
            checkForNewUpdate();
        } else {
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "onEngineStatusUpdate() => SHOWING Waiting Spinner....");
            showWaitingSpinner("Processing the update setup... Please wait");
        }
    }



    private void handleState(int state) {
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "Entering `handleState()` of `OTAPackageCheckerActivity.java`...");

        if(state == UpdaterState.RUNNING) {
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "CURRENT RUNNING STATE = " + state);
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "RUNNING : --> OTA Update is in progress..., --> Switching to `ProgressScreenActivity.java`");
            startActivity(new Intent(this, ProgressScreenActivity.class));
            finish();
        } else if(state == UpdaterState.REBOOT_REQUIRED) {
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "CURRENT RUNNING STATE = " + state);
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "REBOOT_REQUIRED -> Starting UpdateCompletionActivity...");
            startActivity(new Intent(this, UpdateCompletionActivity.class));
            finish();
        }
    }

    /**
     *  Defining Gradient Background
     */

    private void setGradientBackground() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int[] colors = {Color.parseColor("#E0F7FA"), Color.parseColor("#FFFFFF")};
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            gradientDrawable.setCornerRadius(0f);
            getWindow().getDecorView().setBackground(gradientDrawable);
        }
    }


    /**
     * MAIN LOGIC :
     *
     * 1. Using UpdateConfigs.getUpdateConfigs(this) to list available .json OTA configs from USB
     * 2. If none is new => SystemUpToDateActivity.java
     * 3. If there is new OTA => Move to "OTAPackageAvailableActivity.java"
     */

    private void checkForNewUpdate() {

        if(whetherScannedForNewUpdates) {
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "whetherScannedForNewUpdates is already TRUE, Already scanned for new updates....");
            return;
        }

        // Setting the switch to "TRUE" if the scanning (once) was triggered.
        whetherScannedForNewUpdates = true;
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "CHECKING USB PENDRIVE FOR NEW OTA UPDATES>...");


        /**
         * Load JSON Update Configs and get the list of possible updates from USB.
         */
        List<UpdateConfig> configs = UpdateConfigs.getUpdateConfigs(this);


        if(configs == null || configs.isEmpty()) {
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "NO .JSON CONFIG FOUND!!! NO UPDATE ---> Moving to SystemUpToDateActivity...");
            goToSystemUpToDateActivity();
            return;
        }



        /**
         *  Find non-identical config
         */
        UpdateConfig newConfig = findNewerorDifferentUpdate(configs);
        if(newConfig != null) {
            // Found a new update => move to OTAPackageAvailableActivity
            // We possibly store "newConfig" somewhere if we want to pass it along.


            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "NEW CONFIG FOUND!! => Moving to OTAPackageAvailableActivity");
            goToOTAPackageAvailableActivity();

        } else {
            // Means we found .JSON, but not "NEW" or INVALID or IDENTICAL.
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "FOUND CONFIGS, BUT NONE IS NEW.. => System Up-to date...");
            goToSystemUpToDateActivity();
        }

    }

    /**
     * Defining the `isStableEngineStatus()` to check whether Current Engine Status is IDLE, UPDATED_NEED_REBOOT or ERROR State.
     * If the status code hits = 11, show the loading spinner, until the engine updates to IDLE / UPDATED_NEED_REBOOT or ERROR State.
     */

    private boolean isStableEngineStatus(int status) {

        if(status != 11) {
            return true;
        }
        return false;
    }

    /**
     * Showing the indeterminate loading spinner
     */
    private void showWaitingSpinner(String message) {
        if(mWaitingSpinner == null) {
            mWaitingSpinner = new ProgressDialog(this);
            mWaitingSpinner.setIndeterminate(true);
            mWaitingSpinner.setCancelable(false);
        }

        mWaitingSpinner.setMessage(message);
        if(!mWaitingSpinner.isShowing()) {
            mWaitingSpinner.show();
        }

    }

    /**
     * Hide the loading spinner if engine status was updated to IDLE / UPDATED_NEED_REBOOT / ERROR or other states.
     */

    private void hideWaitingSpinner() {
        if(mWaitingSpinner != null && mWaitingSpinner.isShowing()) {
            mWaitingSpinner.dismiss();
        }
    }




    /**
     *
     * This method tries to find a config which might be NEW or DIFFERENT from current system's build
     *
     */

    private UpdateConfig findNewerorDifferentUpdate(List<UpdateConfig> configs) {
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "LISTED CONFIGS ARE : " + configs.get(0));
        SelectedUpdateConfigHolder.setSelectedConfig(configs.get(0));
        return configs.get(0);
        // Eg : "S gpn600_001-AAL-AA-07009-01"
//        String localBuild = Build.DISPLAY.trim();
//        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "Local Build is : " + localBuild);
//
//
//        for(UpdateConfig cfg : configs) {
//
//            String configName = cfg.getName().trim();
//            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "CHECKING CONFIG NAME = " + configName);
//
//
//            // Compare if they are identical configs or not.
//            if(!configName.equals(localBuild)) {
//                // We will treat any mis-match as DIFFERENT or NEWER VERSION..
//                SelectedUpdateConfigHolder.setSelectedConfig(cfg);
//                return cfg;
//            }
//
//        }
//        // If they are identical, return "null"
//        return null;
    }


    private UpdateConfig findNewerUpdate(List<UpdateConfig> configs) {
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "findNewerUpdate() => Found configs are --> " + configs.get(0));
        return configs.get(0);
    }


    private void goToSystemUpToDateActivity() {
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "NO CONFIG FOUND!!!  --> Switching to SystemUpToDateActivity.java");
        startActivity(new Intent(this, SystemUpToDateActivity.class));
        finish();
    }

    private void goToOTAPackageAvailableActivity() {
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "CONFIG AVAILABLE...--> Switching to OTAPackageAvailableActivity.java");
        startActivity(new Intent(this, OTAPackageAvailableActivity.class));
        finish();
    }




}
