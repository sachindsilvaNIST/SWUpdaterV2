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

import android.content.Intent;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_package_checker);

        // Retrieve the shared UpdateManager Instance..
        mUpdateManager = UpdateManagerHolder.getInstance();
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY,"mUpdateManager INSTANCE --> " + mUpdateManager);


        mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Bind the update engine
        mUpdateManager.bind();

        // Check immediately for new OTA Updates.
        checkForNewUpdate();
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
     * MAIN LOGIC :
     *
     * 1. Using UpdateConfigs.getUpdateConfigs(this) to list available .json OTA configs from USB
     * 2. If none is new => SystemUpToDateActivity.java
     * 3. If there is new OTA => Move to "OTAPackageAvailableActivity.java"
     */

    private void checkForNewUpdate() {
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

            goToOTAPackageAvailableActivity();
        } else {
            // Means we found .JSON, but not "NEW" or INVALID or IDENTICAL.
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "FOUND CONFIGS, BUT NONE IS NEW.. => System Up-to date...");
            goToSystemUpToDateActivity();
        }

    }

    /**
     *
     * This method tries to find a config which might be NEW or DIFFERENT from current system's build
     *
     */

    private UpdateConfig findNewerorDifferentUpdate(List<UpdateConfig> configs) {
        // Eg : "S gpn600_001-AAL-AA-07009-01"
        String localBuild = Build.DISPLAY.trim();
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "Local Build is : " + localBuild);


        for(UpdateConfig cfg : configs) {

            String configName = cfg.getName().trim();
            Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "CHECKING CONFIG NAME = " + configName);


            // Compare if they are identical configs or not.
            if(!configName.equals(localBuild)) {
                // We will treat any mis-match as DIFFERENT or NEWER VERSION..
                SelectedUpdateConfigHolder.setSelectedConfig(cfg);
                return cfg;
            }

        }
        // If they are identical, return "null"
        return null;
    }


    private UpdateConfig findNewerUpdate(List<UpdateConfig> configs) {
        Log.d(TAG_OTA_PACKAGE_CHECKER_ACTIVITY, "findNewerUpdate() => Found configs are --> " + configs.get(0));
        return configs.get(0);
    }


    private void goToSystemUpToDateActivity() {
        startActivity(new Intent(this, SystemUpToDateActivity.class));
        finish();
    }

    private void goToOTAPackageAvailableActivity() {
        startActivity(new Intent(this, OTAPackageAvailableActivity.class));
        finish();
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
//
//import com.nidec.swupdater.v2.R;
////import com.nidec.swupdater.v2.util.UpdateConfigs;
////import com.nidec.swupdater.v2.UpdateConfig;


//
//public class OTAPackageCheckerActivity extends Activity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_ota_package_checker);
//
//
//
//
////        boolean mUpdateAvailable = checkForOTAUpdate();
////
////        // Check whether OTA Package Update available or not
////        // IF `!mUpdateAvailable` --> TRUE => `mUpdateAvailable` --> FALSE
////        if(!mUpdateAvailable) {
////            Intent intent = new Intent(this,SystemUpToDateActivity.class);
////            startActivity(intent);
////        } else {
////            // Display the OTA Package Available screen --> `OTAPackageAvailableActivity.java`
////            Intent intent = new Intent(this,OTAPackageAvailableActivity.class);
////            startActivity(intent);
////        }
////        finish();
//
//    }
//
////    private boolean checkForOTAUpdate() {
////        // Possibly use your existing SWUpdater V1 logic to see if an update is found.
////        // E.G: Fetch config from update server or parse JSON from local storage.
////
////        return false;
////    }
//}
