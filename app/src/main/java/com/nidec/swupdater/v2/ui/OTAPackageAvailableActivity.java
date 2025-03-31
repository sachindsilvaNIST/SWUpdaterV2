package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;


import android.content.Intent;
import android.content.res.ColorStateList;

import android.graphics.drawable.StateListDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.UpdateEngine;
import android.os.Build;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

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

import java.util.List;


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

    private ProgressDialog mSpinnerDialog;

//    Update Icon
    private ImageView mImageViewUpdateIcon;

//    Main Title
    private TextView mTextViewOTAPackageAvailableMainTitle;

//    Sub Title
    private TextView mTextViewOTAPackageAvailableSubtitle;

//    "Yes" Button
    private Button mUpdateYesButton;

//    "No" Button
    private Button mUpdateNoButton;

//    "View Config" Button
    private Button mViewConfigButton;

//   ===> DEBUGGING PURPOSE : SRD 2025-03-13
//    private TextView mTextViewInfo;
//   <=== DEBUGGING PURPOSE : SRD 2025-03-13


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_package_available);


        /**
         * Loading Spinner before actual activity state(s) begin... for few seconds...
         *
         */
        showingLoadingSpinner("Checking for updates...");
        new Handler().postDelayed(() -> hideLoadingSpinner(),1000);


        // Retrieve the shared UpdateManager Instance...
        mUpdateManager = UpdateManagerHolder.getInstance();
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "mUpdateManager INSTANCE => " + mUpdateManager);


        // Get the UI Elements IDs.


        mImageViewUpdateIcon = findViewById(R.id.imageViewUpdateIcon);
        mTextViewOTAPackageAvailableMainTitle = findViewById(R.id.textViewOTAPackageAvailableMainTitle);
        mTextViewOTAPackageAvailableSubtitle = findViewById(R.id.textViewOTAPackageAvailableSubtitle);
        mUpdateYesButton = findViewById(R.id.mOTAPackageAvailableYesButton);
        mUpdateNoButton = findViewById(R.id.mOTAPackageAvailableNoButton);
        mViewConfigButton = findViewById(R.id.mOTAPackageAvailableViewConfigButton);

        /**
         * Custom tinting the ImageView icon with custom color
         */
        tintImageView(mImageViewUpdateIcon, "#3A7BD5");



        /**
         * Setting the "Yes", "No" and "View Config" buttons background to rounded...
         */


        /**
         * Create a pressed effect for "Yes" Button => Nidec green (#009B4A)
         */

        setUpButtonWithPressedEffect(mUpdateYesButton, "#009B4A", "#00793C", 10f);

        /**
         * Create a pressed effect for "No" Button => Red (#F44336)
         */

        setUpButtonWithPressedEffect(mUpdateNoButton, "#F44336", "#D32F2F", 10f);

        /**
        * DEBUGGING PURPOSE ONLY ==> Create a pressed effect for "View Config" Button => Nidec green (#009B4A)
        */

        setUpButtonWithPressedEffect(mViewConfigButton, "#009B4A", "#00793C", 10f);

        // "Yes" => Nidec green (#009B4A)
//        customStyleButton(mUpdateYesButton, "#009B4A");


        // "No" => Red (#F44336)
//        customStyleButton(mUpdateNoButton,"#F44336");
//        customStyleButton(mViewConfigButton,"#009B4A");




//   ===> DEBUGGING PURPOSE : SRD 2025-03-13
//        mTextViewInfo = findViewById(R.id.mOTAPackageAvailableMainTitle);
//   <=== DEBUGGING PURPOSE : SRD 2025-03-13


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
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "Available Update : " + selected.getName());

//   ===> DEBUGGING PURPOSE : SRD 2025-03-13
//            mTextViewInfo.setText("Available Update : " + selected.getName());
//   <=== DEBUGGING PURPOSE : SRD 2025-03-13

        } else {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "No config was set!!");

//   ===> DEBUGGING PURPOSE : SRD 2025-03-13
//            mTextViewInfo.setText("No config was set!!");
//   <=== DEBUGGING PURPOSE : SRD 2025-03-13

        }

        /**
         * if "YES" button was pressed ==> Confirm and Apply..
         */

        mUpdateYesButton.setOnClickListener((View v) -> {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY,"OK Button was pressed....");
            checkForNewUpdate();
        });

        /**
         * If "CANCEL(NO)" button was pressed ==> GoTo "SystemUpToDateActivity.java --> DEBUG PURPOSE...
         */
        mUpdateNoButton.setOnClickListener((View v) -> {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY,"NO Button was pressed....");
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
     * Defining Loading Spinner function
     */
    private void showingLoadingSpinner(String message) {
        if(mSpinnerDialog == null) {
            mSpinnerDialog = new ProgressDialog(this);
            mSpinnerDialog.setIndeterminate(true);
            mSpinnerDialog.setCancelable(false);
        }
        mSpinnerDialog.setMessage(message);
        mSpinnerDialog.show();
    }


    /**
     * Hiding Loading Spinner after few seconds
     */

    private void hideLoadingSpinner() {
        if(mSpinnerDialog != null && mSpinnerDialog.isShowing()) {
            mSpinnerDialog.dismiss();
        }
    }

    /**
     * When "CANCEL" button was pressed, display the loading spinner with message for 1 seconds.
     */
    private void showCancelingSpinnerMessage() {
        showingLoadingSpinner("Cancelling the update...");
        new Handler().postDelayed(() -> {
            hideLoadingSpinner();
        },1000);
    }


    /**
     * When "YES" Button under Apply Update AlertDialog Section was triggered, display "Applying update..." loading spinner message for 2 seconds.
     */

    private void showInitiatingUpdateLoadingSpinner() {
        showingLoadingSpinner("Initiating update...");
        new Handler().postDelayed(() -> {
            hideLoadingSpinner();
        },2000);
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


    /**
     * Defining custom tint color for ImageView Icon.
     */

    private void tintImageView(ImageView imageView, String colorInHex) {
        int colorInt = Color.parseColor(colorInHex);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setImageTintList(ColorStateList.valueOf(colorInt));
        }
    }

    /**
     * Creating a button background that shows the pressed effect along with rounded corners
     */

    private void setUpButtonWithPressedEffect(Button mButton, String normalColor, String pressedColor, float cornerRadiusDp) {
        /**
         * Converting 10dp to actual pixels for the corner radius.
         *
         * */

        float cornerRadiusToPixels = cornerRadiusDp * getResources().getDisplayMetrics().density;

        /**
         * Normal state of button before press effect
         */
        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setShape(GradientDrawable.RECTANGLE);
        normalDrawable.setCornerRadius(cornerRadiusToPixels);
        normalDrawable.setColor(Color.parseColor(normalColor));

        /**
         * Pressed state of button after press effect
         */

        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setShape(GradientDrawable.RECTANGLE);
        pressedDrawable.setCornerRadius(cornerRadiusToPixels);
        pressedDrawable.setColor(Color.parseColor(pressedColor));

        /**
         * State list drawable for pressed effect...
         */
        StateListDrawable states = new StateListDrawable();

        // Pressed state (when android:state_pressed = true)
        states.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);

        // Default state
        states.addState(new int[]{}, normalDrawable);

        /**
         * Add the above effects to the button.
         */

        mButton.setBackground(states);

    }





    /**
     * Defining custom style button background
     * BUTTONS : "Yes", "No", "View Config"
     */


    private void customStyleButton(Button button, String colorInHex){

        GradientDrawable roundedBg = new GradientDrawable();
        roundedBg.setShape(GradientDrawable.RECTANGLE);

        /**
         * Setting the custom color passed through `colorInHex` parameter
         */
        roundedBg.setColor(Color.parseColor(colorInHex));

        /**
         * Converting 10dp to actual pixels for the corner radius.
         */
        float cornerRadiusDp = 10f;
        float cornerRadiusToPixels = cornerRadiusDp * getResources().getDisplayMetrics().density;
        roundedBg.setCornerRadius(cornerRadiusToPixels);

        /**
         * Applying the above modifications to Custom Buttons `button` parameter.
         */
        button.setBackground(roundedBg);

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

    /**
     * MAIN LOGIC :
     *
     * 1. Using UpdateConfigs.getUpdateConfigs(this) to list available .json OTA configs from USB
     * 2. If none is new => SystemUpToDateActivity.java
     * 3. If there is new OTA => Move to "OTAPackageAvailableActivity.java"
     */

    private void checkForNewUpdate() {
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "CHECKING USB PENDRIVE FOR NEW OTA UPDATES>...");

        /**
         * Load JSON Update Configs and get the list of possible updates from USB.
         */
        List<UpdateConfig> configs = UpdateConfigs.getUpdateConfigs(this);


        if(configs == null || configs.isEmpty()) {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "NO .JSON CONFIG FOUND!!! NO UPDATE ---> Moving to SystemUpToDateActivity...");
            goToSystemUpToDateActivity();
            return;
        } else {
            Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY,"CONFIG FOUND!! Heading to yesButtonWasClicked() function...");
            yesButtonWasClicked();
        }

    }

    private void goToSystemUpToDateActivity() {
        Log.d(TAG_OTA_PACKAGE_AVAILABLE_ACTIVITY, "NO CONFIGS FOUND!!! ==> Switching to SystemUpToDateActivity.java");
        startActivity(new Intent(this, SystemUpToDateActivity.class));
        finish();
    }

}
