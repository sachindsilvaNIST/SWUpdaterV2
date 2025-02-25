package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.Window;
import android.view.WindowManager;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.ui.MainActivity;
import java.util.List;
import android.util.Log;



public class EntrySplashActivity extends Activity {

    // Splash Screen Timeout --> `2 Seconds`
    private static final int SPLASH_TIMEOUT_MS = 2000;
    String TAG_SPLASH_SCREEN = "EntrySplashActivity";

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

       // Delay for SplashScreen and then check for USB Connection Status

       new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {
               if(isUSBDriveConnected()) {
                   // If USB Detected --> Proceed to `MainActivity` Screen
                   Intent intent = new Intent(EntrySplashActivity.this, MainActivity.class);
                   startActivity(intent);
                   finish();
               } else {
                   // If No USB Detected --> Pop-up dialog box with text message
                   showNoUSBDetectedDialog();
               }
           }
       },SPLASH_TIMEOUT_MS); // Splash Screen Duration = 2000ms (02 Seconds)

    }

    // Check whether USB Pendrive -- Implementation using StorageManager
    private boolean isUSBDriveConnected() {
        boolean mUSBFound = false;

        try {
            StorageManager mStorageManager = getSystemService(StorageManager.class);

            if(mStorageManager != null) {
                List<StorageVolume> volumeList = mStorageManager.getStorageVolumes();

                for(StorageVolume volume : volumeList) {
                    // `.isRemovable()` checks if it's a removable volume (USB / SD card)
                    if(volume.isRemovable()) {
                        mUSBFound = true;
                        break;
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG_SPLASH_SCREEN,"ERROR CHECKING USB Drive : ",e);
        }

        // Return the boolean value of USB Detection.
        return mUSBFound;
    }


    /** If USB Connection / Detection = FALSE? :
     * On OK :  Finish the Activity and termiate the app process.
     * On RELAUNCH : Try Again Functionality (Recreate instance or re-check)
      */

    private void showNoUSBDetectedDialog() {

        // Display a Dialog box with text-message

        new AlertDialog.Builder(EntrySplashActivity.this)
                .setTitle("No USB Found")
                .setMessage("USB is not connected or not detected.\n" +
                            "Please connect USB and relaunch the app.")
                // ** User need to pick an Option ***
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Terminate SWUpdater app
                        finish();
                    }
                })
                .setNegativeButton("Relaunch",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // OP 1 : Reboot the app from the beginning
//                        recreate();

                        // OP 2 : Re-check state for USB Connection

                        if(isUSBDriveConnected()){
                            // Re-create intent for `Splash Screen` once USB connection is triggered.
                            Intent intent = new Intent(EntrySplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If USB Connection is not detected, Reinitiate the launch process.
                            recreate();
                        }

                    }
                }).show();
    }






}
