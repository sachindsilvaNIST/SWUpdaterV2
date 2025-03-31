package com.nidec.swupdater.v2.ui;
/**
 * Action :
 * 1. Check if the previous update requires a reboot.
 * 2. If "Yes" --> Show "UpdateCompletionActivity" with "Requires Reboot" message.
 * 3. If "No" --> Move to "DownloadStateCheckActivity" -- Checking the download state.
 *
 */

import android.app.Activity;

import android.content.Intent;
import android.content.Context;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UpdateEngine;

import android.os.storage.StorageManager;
import android.util.Log;

import android.widget.TextView;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.UpdateManager;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.ui.UpdateManagerHolder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RebootCheckActivity extends Activity {

    private static final String TAG_REBOOT_CHECK_ACTIVITY = "RebootCheckActivity";


    private TextView mTextViewUpdaterState;

    private UpdateManager mUpdateManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot_check);

        mTextViewUpdaterState = findViewById(R.id.TextViewUpdaterState);


        /**
         * Retrieve the singleton UpdateManager..
         *
         */

        mUpdateManager = UpdateManagerHolder.getInstance();


        /** 1. Firstly, we need to check whether "REBOOT_REQUIRED" state is set.
         * Or whether `UpdateManager` indicates for "Reboot needed".
         *
         *
         * Case 1 : If `UpdateManager` returns "REBOOT_REQUIRED", Move to `UpdateCompletionActivity`
         *
         * Case 2 : Otherwise, Move to next state --> `DownloadStateCheckActivity`, to check whether the current state is in "Download State" or not.
         *
         */

        mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);

        /**
         *  Synchronize with the existing Update Engine Status.
         */


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /** 2. Binding to UpdateEngine invokes onStatusUpdate callback,
         * persisted UpdaterState has to be loaded and prepared beforehand.
         */
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "BINDING `RebootCheckActivity.java`");
        mUpdateManager.bind();

        /**
         * 3. After binding, check the UpdaterState's currentState
         */

        int currentState = mUpdateManager.getUpdaterState();
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "onResume() => currentState = " + UpdaterState.getStateText(currentState));

        /**
         * Handle the state
         */

        handleState(currentState);

    }

    @Override
    protected void onPause() {
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UNBINDING --> `RebootCheckActivity.java`");
        super.onPause();
        mUpdateManager.unbind();
    }

    /**
     * This function callback is invoked, when the SWUpdaterV2's internal state changes.
     */

    private void onUpdaterStateChange(int newState) {
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UpdaterStateChange state = " + UpdaterState.getStateText(newState) + "/" + newState);
        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "Current State = " + newState);

        /**
         * 4. Always run handleState on UI Thread.
         */
        runOnUiThread(() -> handleState(newState));
    }

    /**
     * Defining a function that handles and disconnects the USB Drive
     *
     * METHOD : Through Shell Command
     */


//    private void forciblyUnmountUSBVolume(String volumePath) {
//        try {
//            Process process = Runtime.getRuntime().exec(new String[] {"umount", volumePath});
//            int resultCode = process.waitFor();
//
//            if(resultCode == 0) {
//                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "USB VOLUME HAS BE UNMOUNTED SUCCESSFULLY.. ==> " + volumePath);
//            } else {
//                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "FAILED TO UNMOUNT VOLUME AT " + volumePath);
//                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "RESULT CODE ===> " + resultCode);
//            }
//        } catch (Exception e) {
//            Log.e(TAG_REBOOT_CHECK_ACTIVITY, "EXCEPTION OCCURED WHILE UNMOUNTING USB VOLUME : " + volumePath,e);
//        }
//    }

    /**
     * Defining a function that handles and ejects the USB Volume
     *
     * METHOD : ACTION_MEDIA_EJECT.
     */

//    private void ejectUSBVolume(String volumePath) {
//
//        Intent ejectIntent = new Intent(Intent.ACTION_MEDIA_EJECT);
//        ejectIntent.setData(Uri.parse("file://" + volumePath));
//
//        sendBroadcast(ejectIntent);
//        Log.d(TAG_REBOOT_CHECK_ACTIVITY, "REQUESTED EJECT OF USB VOLUME : " + volumePath);
//    }




    /**
     * Defining a function that handles and unmounts the USB Drive
     *
     * METHOD : unmount(String volId) from StorageManager.java
     */

//    private void forciblyUnmountUSB(Context context, String volumeId) {
//        try {
//
//            // Obtaining standard StorageManager Instance
//            StorageManager mStorageManager = getSystemService(StorageManager.class);
//
//            Method unmountMethod = mStorageManager.getClass().getDeclaredMethod("unmount", String.class);
//            unmountMethod.setAccessible(true);
//
//            // Calling unmount method with given volumeID
//            unmountMethod.invoke(mStorageManager, volumeId);
//
//            Log.d(TAG_REBOOT_CHECK_ACTIVITY, "USB PENDRIVE HAS BEEN SUCCESSFULLY UNMOUNTED...." + volumeId);
//
//
//        } catch (NoSuchMethodException e) {
//            Log.e(TAG_REBOOT_CHECK_ACTIVITY, "NoSuchMethodException: possibly the unmount(String) doesn't exist ==> " + e);
//        } catch (InvocationTargetException e) {
//            Log.e(TAG_REBOOT_CHECK_ACTIVITY, "InvocationTargetException unmounting volume " + volumeId, e);
//        } catch (IllegalAccessException e) {
//            Log.e(TAG_REBOOT_CHECK_ACTIVITY, "IllegalAccessException: On unmounting volume " + volumeId, e);
//        } catch (Exception e) {
//            Log.e(TAG_REBOOT_CHECK_ACTIVITY, "Exception: On forcibly unmounting volume " + volumeId, e);
//        }
//    }


    /**
     * Defining a function that handles and unmounts the USB Drive
     *
     * METHOD : Using Shell command "sm" method
     */

//    private void unMountUSBPendriveWithShellCMD(String volumeID) {
//        try {
//            Process processID = Runtime.getRuntime().exec(new String[] {
//                    "sm", "unmount", volumeID
//            });
//
//            int resultCode = processID.waitFor();
//            if(resultCode == 0) {
//                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "The USB Pendrive has been successfully unmounted....!" + volumeID);
//            } else {
//                Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UNMOUNT WAS FAILED!!, RESULT CODE = " + resultCode);
//            }
//
//
//        } catch (IOException e) {
//            Log.e(TAG_REBOOT_CHECK_ACTIVITY, "IOException running sm unmount", e);
//        } catch (InterruptedException e) {
//            Log.e(TAG_REBOOT_CHECK_ACTIVITY, "InterruptedException waiting for sm unmount", e);
//        }
//    }




    /**
     * This function will be called once we're bound.
     * It will check immediately and synchronizes the SWUpdaterV2's state with the current Engine Status.
     */


    /**
     * Handles the current state logic :
     *
     * Case 1 : If REBOOT_REQUIRED --> Go to `UpdateCompletionActivity`
     *
     * Case 2 : Else --> Go to `DownloadStateCheckActivity`
     */

    private void handleState(int state) {
        // Update the UI Text --> activity_reboot_check.xml
        String stateText = UpdaterState.getStateText(state);
        mTextViewUpdaterState.setText(stateText + "/" + state);

        if(state == UpdaterState.REBOOT_REQUIRED){
            Log.d(TAG_REBOOT_CHECK_ACTIVITY, "UNMOUNTING USB PENDRIVE...");

//            forciblyUnmountUSB(this, "public:8,2");



            Log.d(TAG_REBOOT_CHECK_ACTIVITY, "REBOOT_REQUIRED -> Starting UpdateCompletionActivity...");
            startActivity(new Intent(this, UpdateCompletionActivity.class));
            finish();
        } else {
            // For all other states, Move to `DownloadStateCheckActivity.java`
            Log.d(TAG_REBOOT_CHECK_ACTIVITY,"No Reboot Required -> Starting DownloadStateCheckActivity");
            startActivity(new Intent(this, DownloadStateCheckActivity.class));
            finish();
        }

    }

}


































