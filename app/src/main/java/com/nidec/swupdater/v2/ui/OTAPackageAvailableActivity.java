package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import com.nidec.swupdater.v2.R;


import com.nidec.swupdater.v2.UpdateConfig;
import com.nidec.swupdater.v2.UpdateManager;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.PayloadSpec;
import com.nidec.swupdater.v2.UpdateManager;

import com.nidec.swupdater.v2.util.UpdateConfigs;
import com.nidec.swupdater.v2.util.UpdateEngineErrorCodes;
import com.nidec.swupdater.v2.util.UpdateEngineProperties;
import com.nidec.swupdater.v2.util.UpdateEngineStatuses;
import com.nidec.swupdater.v2.util.SelectedUpdateConfigHolder;

import com.nidec.swupdater.v2.ui.UpdateManagerHolder;


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

    private TextView mTextViewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_package_available);


        // Retrieve the shared UpdateManager Instance...
        mUpdateManager = UpdateManagerHolder.getInstance();


        // Get the UI Elements IDs.
        mUpdateYesButton = findViewById(R.id.mOTAPackageAvailableYesButton);
        mUpdateNoButton = findViewById(R.id.mOTAPackageAvailableNoButton);
        mTextViewInfo = findViewById(R.id.mOTAPackageAvailableMainTitle);

        /**
         * ===> DEBUG PURPOSE
         * DISPLAY THE INFO FROM `util/SelectedUpdateConfigHolder.java`
         * <===
         */

        UpdateConfig selected = SelectedUpdateConfigHolder.getSelectedConfig();
        if(selected != null) {
            mTextViewInfo.setText("Available Update : " + selected.getName());

        } else {
            mTextViewInfo.setText("No config set!!");
        }

        /**
         * if "YES" button was pressed ==> Confirm and Apply..
         */

        mUpdateYesButton.setOnClickListener((View v) -> {
            yesButtonWasClicked();
        });

        /**
         * If "CANCEL(NO)" button was pressed ==> GoTo "SystemUpToDateActivity.java --> DEBUG PURPOSE...
         */
        mUpdateNoButton.setOnClickListener((View v) -> {
            noButtonWasClicked();
        });

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



    // When "YES" button was pressed.
    private void yesButtonWasClicked() {
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
                .setMessage("Do you really want to apply this update?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                    /**
                     * APPLY THE OTA PACKAGE UPDATE...
                     */

                    applySelectedUpdate(selected);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();




    }

    // When "CANCEL" Button was pressed.
    private void noButtonWasClicked() {

        /**
         * ===> DEBUG PURPOSE
         * MOVE TO "SystemUpToDateActivity.java"
         * <===
         */

        startActivity(new Intent(this, SystemUpToDateActivity.class));
        finish();
    }

    private void applySelectedUpdate(UpdateConfig config) {
        try {
            mUpdateManager.applyUpdate(this,config);

            // Move to "ProgressScreenActivity.java" to show the progress rate in percentage..
            startActivity(new Intent(this,ProgressScreenActivity.class));
            finish();

        } catch (UpdaterState.InvalidTransitionException e) {
            Log.e(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "FAILED TO APPLY THE UPDATE!!!" + config.getName(),e);
        }
    }































//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_ota_package_available);
//
//
//        Button mUpdateYesButton = findViewById(R.id.mOTAPackageAvailableYesButton);
//        Button mUpdateNoButton = findViewById(R.id.mOTAPackageAvailableNoButton);
//
//
//        // IF "YES" BUTTON WAS PRESSED : If the OTA Update Available --> Start the download process.
//        // E.g : UpdateManager.getInstance().startDownload()
//        // Goto Progress Screen Activity --> `ProgressScreenActivity.java`
//        mUpdateYesButton.setOnClickListener(v -> {
//            Intent intent = new Intent(this, ProgressScreenActivity.class);
//            startActivity(intent);
//            finish();
//
//        });
//
//        // ===> Todo ---> DOUBT!!!!
//        // IF "NO" BUTTON WAS PRESSED : Cancel the update --> Back to "OTA Package Checker" Screen
//        // E.g : UpdateManager.getInstance().cancelDownload()
//        // Back to "OTA Package Checker" Screen --> `OTAPackageCheckerActivity.java`
//        // <=== Todo ---> DOUBT!!!!
//        mUpdateNoButton.setOnClickListener(v -> {
//            Intent intent = new Intent(this,OTAPackageCheckerActivity.class);
//            startActivity(intent);
//            finish();
//
//        });
//
//
//
//    }
























}
