package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;


import android.widget.Button;
import android.widget.TextView;

import com.nidec.swupdater.v2.R;

//import com.nidec.swupdater.v2.util.UpdateConfigs;
//import com.nidec.swupdater.v2.UpdateConfig;

/**
 * Action:
 * 1. Check if an OTA Package download is already in progress.
 * 2. If "Yes" --> Goto "ProgressScreenActivity"
 * 3. If "No" --> Goto "OTAPackageCheckerActivity"
 */


public class DownloadStateCheckActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_state_check);
    }


    /**
     * Function Implementation : Check whether the OTA Package download is in progress..
     */

    private boolean isDownloadInProgress() {
        // Query your UpdateManager or saved state in SharedPrefs, etc.\
        // Eg: return DownloadManager.isDownloading();
        return false;
    }


}
