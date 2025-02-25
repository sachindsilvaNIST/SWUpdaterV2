package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.app.ActivityManager;
import android.view.View;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.ui.MainActivity;
import java.util.List;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class EntrySplashActivity extends Activity {

    public String TAG_SPLASH_SCREEN = "EntrySplashActivity";


    // Splash Screen Timeout --> `2 Seconds`
    private static final int SPLASH_TIMEOUT_MS = 2000;
    private static final int POLL_INTERVAL_MS = 2000; // USB Background checks for 2 seconds


    // Storing "No USB Dialog" reference
    private AlertDialog mNoUSBDialog = null;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    // Tracking whether USB has been connected or not.
    private boolean mUSBConnected = false;


    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);


        // Making Splash Screen to full window [Set to Default]
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        // After SPLASH_TIMEOUT_MS, check for USB Connection.
        mHandler.postDelayed(()-> {
            if(isUSBDriveConnected()) {
                launchMainActivity();
            } else {
                // Display "No USB found" dialog
                showNoUSBDetectedDialog();
                startUSBPolling();
            }
        },SPLASH_TIMEOUT_MS);


    }

    // Check whether USB Pendrive -- Implementation using StorageManager
    private boolean isUSBDriveConnected() {

        try {
            StorageManager mStorageManager = getSystemService(StorageManager.class);
            if(mStorageManager != null) {
                List<StorageVolume> volumeList = mStorageManager.getStorageVolumes();

                for(StorageVolume volume : volumeList) {
                    // `.isRemovable()` checks if it's a removable volume (USB / SD card)
                    if(volume.isRemovable()) {
                        return true; // `mUSBConnected` = TRUE -- USB Pendrive is detected.
                    }
                }
            }
        } catch (Exception e) { // ERROR : Throw JavaException if USB Check interrupted.
            Log.e(TAG_SPLASH_SCREEN,"ERROR CHECKING USB Drive : ",e);
        }

       // mUSBConnected` = FALSE -- USB Pendrive is not detected.
        return false;
    }


    /**
     * Show a Dialog Box --> If USB is not connected, with spinner and `Retry` button.
      */

    private void showNoUSBDetectedDialog() {

        if(mNoUSBDialog != null && mNoUSBDialog.isShowing()) {
            // The Dialog box instance is already running.
            return;
        }


        // Inflating the custom Layout for USB Waiting connection (Loading spinner and its TextView)

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mDialogView = mInflater.inflate(R.layout.dialog_waiting_for_usb,null);

        // Get the Reference IDs for Loading Spinner and TextView from `dialog_waiting_for_usb.xml`
        ProgressBar mProgressBarWaitingForUSBConnection = mDialogView.findViewById(R.id.mProgressBarWaitingForUSBConnection);


        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("Waiting for USB Connection...");
        mBuilder.setMessage("Please insert a USB drive!");
        mBuilder.setView(mDialogView);
        mBuilder.setCancelable(false); // Ensuring the user must choose a button (Can't tap outside to dismiss.)



        // `Close App` Button to terminate the app.
        mBuilder.setNegativeButton("Close App",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Terminate SWUpdater App
//                finish();


                // Remove Any Polling Callbacks if any..
                if(mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }

                // Close the Activity stack and remove from Recents App Panel.
                finishAndRemoveTask();

            }
        });


        // Create and Show the Dialog Box
        mNoUSBDialog = mBuilder.create();
        mNoUSBDialog.setCancelable(false);
        mNoUSBDialog.show();

    }


    // Periodic check USB Connectivity -- Effectively polls for every `POLL_INTERVAL_MS`

    private void startUSBPolling() {
        mHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }


    /**
     * `pollRunnable` checks USB connectivity in the background for every `POLL_INTERVAL_MS` Milliseconds --> i.e., every 02 seconds
     *
     * If USB Connection = FOUND? Proceed
     * Otherwise, keep polling (Call `startUSBPolling()` function)
     */
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG_SPLASH_SCREEN,"pollRunnable Function was called..");
            if(!mUSBConnected) { // If mUSBConnected = TRUE
                if(isUSBDriveConnected())  { // If return value of this function is `TRUE` --> Proceed to next Activity
                    usbConnectionSuccessAndActive();
                } else {
                    // If USB still not connected, keep polling..
                    Log.d(TAG_SPLASH_SCREEN,"pollRunnable Function was called again...");

                    mHandler.postDelayed(this,POLL_INTERVAL_MS); // Every 02 seconds
                }
            }
        }
    };


    // If USB Connection = TRUE, then this function will be called once either by `Retry` Button or Auto-polling method.

    private void usbConnectionSuccessAndActive() {
        mUSBConnected = true;

        // Dismiss the `showNoUSBDetectedDialog`, if it's up
        if(mNoUSBDialog != null && mNoUSBDialog.isShowing()){
            mNoUSBDialog.dismiss();
        }


        // Display the "USB Connected" Message\
        Toast.makeText(this, "USB Connected!",Toast.LENGTH_SHORT).show();

        // Launch `MainActivity`
        launchMainActivity();
    }

    // Launch MainActivity, If USB Connection = SUCCESS, finish and terminate SplashActivity
    private void launchMainActivity() {
        Toast.makeText(this, "USB Connected!",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EntrySplashActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }


    // When "Close App" Button pressed : Clear App's cache data

    private void mClearApplicationUserData() {

        ActivityManager mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        if(mActivityManager != null){
            // Clear the user data of your app
            // NOTE <SRD> : The app's process will receive ACTION_CLEAR_APP_USER_DATA broadcast if needed...
            mActivityManager.clearApplicationUserData();
        }
    }





}
