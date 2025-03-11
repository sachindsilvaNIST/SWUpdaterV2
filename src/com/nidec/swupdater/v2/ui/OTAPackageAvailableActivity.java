package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.UpdateEngine;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import com.nidec.swupdater.v2.R;

import com.nidec.swupdater.v2.UpdateConfig;
import com.nidec.swupdater.v2.UpdateManager;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.PayloadSpec;

import com.nidec.swupdater.v2.ui.UpdateManagerHolder;

import com.nidec.swupdater.v2.util.UpdateConfigs;
import com.nidec.swupdater.v2.util.UpdateEngineErrorCodes;
import com.nidec.swupdater.v2.util.UpdateEngineProperties;
import com.nidec.swupdater.v2.util.UpdateEngineStatuses;
import com.nidec.swupdater.v2.util.SelectedUpdateConfigHolder;




/**
 * Action :
 * 1. Show an update is available
 * 2. If "Yes" was pressed, Start the download --> call applyUpdate(..) ==> Go to "ProgressScreenActivity.java"
 * 3. If "No" was pressed, Goto "SystemUpdateWasCancelled.java" ---> For demo purpose, goto "SystemUpToDateActivity.java.
 *
 */



public class OTAPackageAvailableActivity extends Activity {

    private static final String TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY = "OTAPackageAvailableActivity";


    private UpdateManager mUpdateManager;

    private Button mUpdateYesButton;
    private Button mUpdateNoButton;
    private Button mViewConfigButton;
    private TextView mTextViewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_package_available);


        // Retrieve the shared UpdateManager Instance...
        mUpdateManager = UpdateManagerHolder.getInstance();
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "mUpdateManager INSTANCE => " + mUpdateManager);


        // Get the UI Elements IDs.
        mUpdateYesButton = findViewById(R.id.mOTAPackageAvailableYesButton);
        mUpdateNoButton = findViewById(R.id.mOTAPackageAvailableNoButton);
        mViewConfigButton = findViewById(R.id.mOTAPackageAvailableViewConfigButton);
        mTextViewInfo = findViewById(R.id.mOTAPackageAvailableMainTitle);

        /**
         * ===> DEBUG PURPOSE
         * DISPLAY THE INFO FROM `util/SelectedUpdateConfigHolder.java`
         * <===
         */

        UpdateConfig selected = SelectedUpdateConfigHolder.getSelectedConfig();
        if(selected != null) {
            /** ==> DEBUG PURPOSE - SRD
             * Show the .JSON Path and raw JSON Contents.
             *
             * <===
             */
            mTextViewInfo.setText("Available Update : " + selected.getName());

        } else {
            mTextViewInfo.setText("No config was set!!");
        }

        /**
         * if "YES" button was pressed ==> Confirm and Apply..
         */

        mUpdateYesButton.setOnClickListener((View v) -> {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY,"OK Button was pressed....");
            yesButtonWasClicked();
        });

        /**
         * If "CANCEL(NO)" button was pressed ==> GoTo "SystemUpToDateActivity.java --> DEBUG PURPOSE...
         */
        mUpdateNoButton.setOnClickListener((View v) -> {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY,"CANCEL Button was pressed....");
            noButtonWasClicked();
        });

        /**
         * If "VIEW CONFIG" button was pressed ==> DISPLAY JSON CONFIG --> DEBUG PURPOSE...
         */
        mViewConfigButton.setOnClickListener((View v) -> {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "VIEW CONFIG Button was pressed...");
            OnViewConfigClick();
        });




        mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);
        mUpdateManager.setOnEngineCompleteCallback(this::onEnginePayloadApplicationComplete);
        mUpdateManager.setOnEngineStatusUpdateCallback(this::onEngineStatusUpdate);
        mUpdateManager.setOnProgressUpdateCallback(this::onProgressUpdate);

    }


    /**
     * Handling Update Engine states or progress on this screen (If Needed - SRD 2025-03-10)
     */

    @Override
    protected void onResume() {
        super.onResume();

        // Bind the update engine
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

    /**
     * DEFINING ALL CALLBACKS
     */

    private void onUpdaterStateChange(int newState) {
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "onUpdateStateChange => " + UpdaterState.getStateText(newState));

        runOnUiThread(() -> handleState(newState));

    }

    private void onEnginePayloadApplicationComplete(int errorCode) {
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "onEnginePayloadApplicationComplete() => " + errorCode);
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "ENGINE PAYLOAD CODE NAME " + UpdateEngineErrorCodes.getCodeName(errorCode));
    }

    private void onEngineStatusUpdate(int status) {
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "onEngineStatusUpdate() => " + status);
    }

    private void onProgressUpdate(double progress) {
        int progressRate = (int) (progress * 100);
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "onProgressUpdate() => " + progressRate + "%");
    }


    private void handleState(int updaterState) {

        // If signaled by `REBOOT_REQUIRED`,or engine-complete callback,,

        if(updaterState == UpdaterState.REBOOT_REQUIRED) {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "Updater State says REBOOT_REQUIRED.... --> Switching to RebootCheckActivity.java");
            startActivity(new Intent(this,RebootCheckActivity.class));
            finish();
        } else if (updaterState == UpdaterState.SLOT_SWITCH_REQUIRED) {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "Updater State says SLOT_SWITCH_REQUIRED.... --> Switching to RebootCheckActivity.java");
            startActivity(new Intent(this,RebootCheckActivity.class));
            finish();
        } else if (updaterState == UpdaterState.ERROR) {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "Updater State says ERROR.... --> Switching to SystemUpToDateActivity.java");
            startActivity(new Intent(this,SystemUpToDateActivity.class));
            finish();
        }

    }




    // When "YES" button was pressed.
    private void yesButtonWasClicked() {
        int currentEngineStatus = mUpdateManager.getEngineStatus();
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "Current Update State ==> " + UpdaterState.getStateText(currentEngineStatus));
        UpdateConfig selected = SelectedUpdateConfigHolder.getSelectedConfig();
        if(selected == null) {
            // No Config found... ==> Fallback ==> Goto "SystemUpToDateActivity.java"
            Log.e(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY,"NO CONFIGS FOUND FROM THE SELECTED UPDATE CONFIG...");
            startActivity(new Intent(this, SystemUpToDateActivity.class));
            finish();
            return;
        }

        /**
         * Display the confirmation dialog..
         */

        new AlertDialog.Builder(this)
                .setTitle("Apply Software Update")
                .setMessage("Do you really want apply this update?\n" + selected.getName())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok,(dialog, which) -> {

                    /**
                     * Replication from SWUpdaterV1 - SRD
                     */
                    uiResetWidgets();
                    uiResetEngineText();

                    /**
                     * Apply the update..
                     */
                    applySelectedUpdate(selected);


                })
                .setNegativeButton(android.R.string.cancel,null)
                .show();

//        new AlertDialog.Builder(this)
//                .setTitle("Apply Software Update")
//                .setMessage("Do you really want to apply this update?")
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
//                    /**
//                     * APPLY THE OTA PACKAGE UPDATE...
//                     */
//
//                    applySelectedUpdate(selected);
//                })
//                .setNegativeButton(android.R.string.cancel, null)
//                .show();

    }

    // When "CANCEL" Button was pressed.
    private void noButtonWasClicked() {

        /**
         * ===> DEBUG PURPOSE
         * MOVE TO "SystemUpToDateActivity.java"
         * <===
         */
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "USER PRESSED CANCEL BUTTON!!! ==> Switching to SystemUpToDateActivity.java...");
        startActivity(new Intent(this, SystemUpToDateActivity.class));
        finish();
    }

    private void applySelectedUpdate(UpdateConfig config) {
        try {
            int currentEngineStatus = mUpdateManager.getEngineStatus();
            String currentEngineStatusToText = UpdaterState.getStateText(currentEngineStatus);
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "CURRENT ENGINE STATUS ===> " + currentEngineStatusToText);
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "applyUpdate() WAS CALLED FOR " + config.getName());

            mUpdateManager.applyUpdate(this,config);

            // Move to "ProgressScreenActivity.java" to show the progress rate in percentage..
            startActivity(new Intent(this,ProgressScreenActivity.class));
            finish();

        } catch (UpdaterState.InvalidTransitionException e) {
            Log.e(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "FAILED TO APPLY THE UPDATE!!!" + config.getName(),e);
            showApplyErrorDialog(e);
        }
    }

    /**
     * Small Dialog box for "CANT APPPLY THE UPDATE" of CATCH Block..
     */

    private void showApplyErrorDialog(Exception e) {
        new AlertDialog.Builder(this)
                .setTitle("Failed to Apply Update")
                .setMessage("Cannot apply update : " + e.getMessage())
                .setPositiveButton(android.R.string.ok,null)
                .show();
    }


    /**
     * This function will be used to disable the certain widget buttons.
     */

    private void uiResetWidgets(){
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "uiResetWidgets() was invoked ==> DISABLING THE UI BUTTONS...");
        mUpdateYesButton.setEnabled(false);
        mUpdateNoButton.setEnabled(false);
    }


    /**
     * Clearing Engine status or error code..
     */
    private void uiResetEngineText() {
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "uiResetEngineText() was invoked ==> CLEARING ENGINE TEXTS...");

    }


    /**
     * This is a helper function "build debug info" about the config file name and display the RAW JSON.
     */

    private void OnViewConfigClick() {
        UpdateConfig config = SelectedUpdateConfigHolder.getSelectedConfig();
        new AlertDialog.Builder(this)
                .setTitle(config.getName())
                .setMessage(config.getRawJson())
                .setPositiveButton(android.R.string.cancel,(dialog, id) -> dialog.dismiss())
                .show();
    }

}
