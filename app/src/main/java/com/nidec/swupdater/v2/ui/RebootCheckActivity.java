package com.nidec.swupdater.v2.ui;

import android.app.Activity;

import android.content.Intent;

import android.os.Bundle;

import com.nidec.swupdater.v2.R;

//import com.nidec.swupdater.v2.util.UpdateConfigs;
//import com.nidec.swupdater.v2.UpdateConfig;

public class RebootCheckActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot_check);

        // Reboot Logic
        if(mcheckWhetherNeedsReboot()) {
            // If the device requires reboot, Goto "Update Completion Screen".
            // Go to "Update Completion Screen" --> `UpdateCompletionActivity.java`
            Intent intent = new Intent(this, UpdateCompletionActivity.class);
            startActivity(intent);
            finish();
        } else {

            // If there is no reboot required, Goto "Download State Check Screen".
            // "Download State Check Screen" --> `DownloadStateCheckActivity.class`
            Intent intent = new Intent(this, DownloadStateCheckActivity.class);
            startActivity(intent);
            finish();
        }



    }


    // Function Implementation : Reboot Logic

    private boolean mcheckWhetherNeedsReboot() {

        // Might be done by using UpdateManager or stored state.
        // Eg ; return UpdateManager.getInstance().isRebootRequired();
        return false;
    }


}
