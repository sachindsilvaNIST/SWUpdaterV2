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
import android.view.Window;
import android.view.WindowManager;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.ui.MainActivity;
import java.util.List;
import android.util.Log;
import android.widget.Toast;


public class EntrySplashActivity extends Activity {

    String TAG_SPLASH_SCREEN = "EntrySplashActivity";


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

//        // Switch / Move to `MainActivity.kt` after Splash Event
//        new Handler().postDelayed(() -> {
//            // Initiate and trigger `EntrySplashActivity` --> `MainActivity` after 02 Seconds <2000MS>
//            Intent intent = new Intent(EntrySplashActivity.this, MainActivity.class);
//
//            startActivity(intent);
//            finish(); // Terminate the SplashActivity so that it won't be running in the back stack
//        },SPLASH_TIMEOUT_MS);

       //  SplashScreen--Entry and then check for USB Connection Status

//       new Handler().postDelayed(new Runnable() {
//           @Override
//           public void run() {
//               if(isUSBDriveConnected()) {
//                   // If USB Detected --> Proceed to `MainActivity` Screen
//                   Intent intent = new Intent(EntrySplashActivity.this, MainActivity.class);
//                   startActivity(intent);
//                   finish();
//               } else {
//                   // If No USB Detected --> Pop-up dialog box with text message
//                   showNoUSBDetectedDialog();
//               }
//           }
//       },SPLASH_TIMEOUT_MS); // Splash Screen Duration = 2000ms (02 Seconds)
//
//    }

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

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("USB Not Detected!");
        mBuilder.setMessage("Please insert a USB drive...!");


        // Add `Retry` button to manually check USB Connection.

        mBuilder.setPositiveButton("Retry",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Check USB Connection Manually
                if(isUSBDriveConnected()){
                    usbConnectionSuccessAndActive();
                } else {
                    Toast.makeText(EntrySplashActivity.this, "Waiting for USB Connection...",Toast.LENGTH_LONG).show();
                }
            }
        });


        // `Close App` Button to terminate the app.
        mBuilder.setNegativeButton("Close App",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Terminate SWUpdater App
                finish();
            }
        });


        // Create and Show the Dialog Box
        mNoUSBDialog = mBuilder.create();
        mNoUSBDialog.setCancelable(false);
        mNoUSBDialog.show();



        // DEBUG - SRD 2025-02-25 Display a Dialog box with text-message

//        new AlertDialog.Builder(EntrySplashActivity.this)
//                .setTitle("No USB Found")
//                .setMessage("USB is not connected or not detected.\n" +
//                            "Please connect USB and relaunch the app.")
//                // ** User need to pick an Option ***
//                .setCancelable(false)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // Terminate SWUpdater app
//                        finish();
//                    }
//                })
//                .setNegativeButton("Relaunch",new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        // OP 1 : Reboot the app from the beginning
////                        recreate();
//
//                        // OP 2 : Re-check state for USB Connection
//
//                        if(isUSBDriveConnected()){
//                            // Re-create intent for `Splash Screen` once USB connection is triggered.
//                            Intent intent = new Intent(EntrySplashActivity.this, MainActivity.class);
//                            startActivity(intent);
//                            finish();
//                        } else {
//                            // If USB Connection is not detected, Reinitiate the launch process.
//                            recreate();
//                        }
//
//                    }
//                }).show();



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
            if(!mUSBConnected) { // If mUSBConnected = TRUE
                if(isUSBDriveConnected())  { // If return value of this function is `TRUE` --> Proceed to next Activity
                    usbConnectionSuccessAndActive();
                } else {
                    // If USB still not connected, keep polling..
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
        Intent intent = new Intent(EntrySplashActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }







}
