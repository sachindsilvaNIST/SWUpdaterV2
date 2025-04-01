package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.RippleDrawable;
import android.app.AlertDialog;

import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.os.UpdateEngine;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.util.UpdateConfigs;
import com.nidec.swupdater.v2.util.UpdateEngineProperties;
import com.nidec.swupdater.v2.util.UpdateEngineStatuses;
import com.nidec.swupdater.v2.util.UpdateEngineErrorCodes;
import com.nidec.swupdater.v2.UpdateConfig;
import com.nidec.swupdater.v2.UpdateManager;

import com.nidec.swupdater.v2.ui.UpdateManagerHolder;

// To Show the last checked date and time...
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Action:
 * 1. Show a screen that says "System is up-to-date.
 * 2. Offer a "Re-check" button which leads back to "OTAPackageCheckerActivity"
 *
 */

public class SystemUpToDateActivity extends Activity {

    private static final String TAG_SYSTEM_UP_TO_DATE_ACTIVITY = "SystemUpToDateActivity";


    private UpdateManager mUpdateManager;

    private ImageView mCheckermarkImage;
    private TextView mTitleText;
    private TextView mSubtitleText;
    private TextView mLastCheckedText;
    private Button mReCheckButton;

    private Button mSystemUpToDateUSBDisconnectButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_up_to_date);

        // Retrieve the shared UpdateManager Instance...
        mUpdateManager = UpdateManagerHolder.getInstance();
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY,"mUpdateManager INSTANCE --> " + mUpdateManager);

        mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);

        // Setting Update Engine Status Callback to detect the Engine STATUSCODE
        mUpdateManager.setOnEngineStatusUpdateCallback(this::onEngineStatusUpdate);

        // Get the `ReCheck` Button UI Element ID
        mCheckermarkImage = findViewById(R.id.imageViewCheckMark);
        mTitleText = findViewById(R.id.mSystemUpToDateMainTitle);
        mSubtitleText = findViewById(R.id.textViewSubtitle);
        mLastCheckedText = findViewById(R.id.textViewLastChecked);
        mReCheckButton = findViewById(R.id.mSystemUpToDateReCheckButton);
        mSystemUpToDateUSBDisconnectButton = findViewById(R.id.mSystemUpToDateUSBDisconnectButton);

        /**
         * Custom tinting the ImageView icon with custom color
         */

        tintImageView(mCheckermarkImage,"#3A7BD5");


        /**
         * Add smooth button press effect for "Verify Again" Button
         */

        setUpButtonWithPressedEffect(mReCheckButton, "#3A7BD5", "#2C63AA",10f);

        /**
         * Add smooth button press effect for "Disconnect USB" Button
         */
        setUpButtonWithPressedEffect(mSystemUpToDateUSBDisconnectButton,"#3A7BD5", "#2C63AA",10f);


        /**
         * Set the "Last Checked" data and time to current system time.
         */

        String currentTime = getCurrentTimeString();
        mLastCheckedText.setText("Last checked: " + currentTime);

        /**
         * When "Disconnect USB" Button was pressed
         */

        mSystemUpToDateUSBDisconnectButton.setOnClickListener(v -> {
            Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "DISCONNECT USB Button was pressed...");

            unMountUSBPendriveWithShellCMD("public:8,2");

        });



        mReCheckButton.setOnClickListener(v -> {
            Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "RECHECK Button was pressed...");

            /**
             * Updating the last checked time...
             */
            mLastCheckedText.setText("Last checked: " + getCurrentTimeString());


            int currentEngineStatus = mUpdateManager.getEngineStatus();
            Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "ENGINE STATUS CODE => " + currentEngineStatus);
            Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "ENGINE STATUS IN PLAINTEXT => " + UpdateEngineStatuses.getStatusText(currentEngineStatus));

            String currentEngineStatusToText = UpdaterState.getStateText(currentEngineStatus);

            if(currentEngineStatus == UpdaterState.ERROR) {
                Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "UPDATE ENGINE STATUS says ERROR!!!");
                forceSwitchingToIDLEState();
            } else if(currentEngineStatus == UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT) {
                Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "UPDATE ENGINE STATUS says UPDATED_NEED_REBOOT...");
                requiresReboot();
            } else if(currentEngineStatus == UpdaterState.IDLE) {
                switchingToIDLEState();
            } else {
                Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY,"WARNING!!! ENGINE IS IN UNEXPECTED STATE => " + currentEngineStatus);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mUpdateManager.bind();

        /**
         * Checking the Update Engine Status if status = 1 or not
         *
         */
        int engineStatusNow = mUpdateManager.getEngineStatus();
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "onResume() => ENGINE STATUS CODE => " + engineStatusNow);
        if(engineStatusNow == 11) {
            handleRollbackState();
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // For API 21+ version, smooth button feedback
             ColorStateList rippleColor = ColorStateList.valueOf(Color.parseColor(pressedColor));

             // RippleDrawable will use the normalDrawable as its content.
            RippleDrawable rippleDrawable = new RippleDrawable(rippleColor, normalDrawable, null);
            mButton.setBackground(rippleDrawable);
        } else {

            // For older APIs, StateListDrawable will be used as Button Effect.
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


    }



    /**
     * Defining function to check the current system time..
     */

    private String getCurrentTimeString() {
        /**===> SRD 2025-03-12
         * DATE AND TIME FORMAT USED : "yyyy-MM-ddd HH:mm:ss"
         * <=== SRD 2025-03-12
         */
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        return mSimpleDateFormat.format(new Date());
    }


    /**
     * Handling callback for `onEngineStatusUpdate()` to detect RAW Engine Status
     */

    private void onEngineStatusUpdate(int status) {
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "onEngineStatusUpdate() => STATUS CODE " + status);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "onEngineStatusUpdate() => STATUS IN TEXT " + UpdateEngineStatuses.getStatusText(status));

        if(status == 11) {
            // Forcibly setting the Update Engine to IDLE
            handleRollbackState();
        }
    }

    /**
     * Forcibly setting the update engine state to IDLE for status = 11
     */

    private void handleRollbackState() {
        Log.w(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "Detected ENGINE STATUS CODE = 11. SETTING TO IDLE now...");
        mUpdateManager.setUpdaterStateSilent(UpdaterState.IDLE);
    }




    private void onUpdaterStateChange(int newState) {
        int currentEngineStatus = mUpdateManager.getEngineStatus();
        String currentEngineStatusToText = UpdaterState.getStateText(currentEngineStatus);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "UpdaterStateChange state No. = " + UpdaterState.getStateText(newState) + "/" + newState);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "Current State = " + newState);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "CurrentEngineStatus No. = " + currentEngineStatus);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "CurrentEngineStatus in codeName = " + currentEngineStatusToText);

    }

    /**
     * If the engine says "UPDATED_NEED_REBOOT", transit to `RebootCheckActivity`
     */

    private void requiresReboot() {
        // mUpdateManager.setUpdaterStateSilent(UpdaterState.REBOOT_REQUIRED);
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, " Requires REBOOT!!! --> Switching to `RebootCheckActivity.java`");
        startActivity(new Intent(this,RebootCheckActivity.class));
        finish();
        return;
    }

    /**
     * If engine was in `ERROR` or want to forcibly re-check => go IDLE => OTAPackageCheckerActivity
     */
    private void forceSwitchingToIDLEState() {
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, " Update Engine says No ERROR switched to IDLE !! --> Switching to `OTAPackageCheckerActivity.java`");
        // Re-check for OTA Package Updates.
        // This will redirect again to `OTAPackageCheckerActivity.java` page.
        mUpdateManager.setUpdaterStateSilent(UpdaterState.IDLE);
        startActivity(new Intent(this,OTAPackageCheckerActivity.class));
        finish();
        return;
    }


    /**
     * Defining a function that handles and unmounts the USB Drive
     *
     * METHOD : Using Shell command "sm" method
     */

    private void unMountUSBPendriveWithShellCMD(String volumeID) {
        try {
            Process processID = Runtime.getRuntime().exec(new String[] {
                    "sm", "unmount", volumeID
            });

            int resultCode = processID.waitFor();
            if(resultCode == 0) {
                Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "The USB Pendrive has been successfully unmounted....!" + volumeID);
            } else {
                Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "UNMOUNT WAS FAILED!!, RESULT CODE = " + resultCode);
            }


        } catch (IOException e) {
            Log.e(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "IOException running sm unmount", e);
        } catch (InterruptedException e) {
            Log.e(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, "InterruptedException waiting for sm unmount", e);
        }
    }

    /**
     * If engine is IDLE => switch to `OTAPackageCheckerActivity` for new update check.
     */
    private void switchingToIDLEState() {
        Log.d(TAG_SYSTEM_UP_TO_DATE_ACTIVITY, " Update Engine says IDLE !! --> Switching to `OTAPackageCheckerActivity.java`");
        // Re-check for OTA Package Updates.
        // This will redirect again to `OTAPackageCheckerActivity.java` page.
        startActivity(new Intent(this,OTAPackageCheckerActivity.class));
        finish();
        return;
    }

}
