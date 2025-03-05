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
//import com.nidec.swupdater.v2.util.UpdateConfigs;
//import com.nidec.swupdater.v2.UpdateConfig;

/**
 * Action : If the update is successfully applied, ask for reboot, show a screen that says "Update was successful. Please Reboot."
 *
 */

public class UpdateCompletionActivity extends Activity {

    private static final String TAG_UPDATE_COMPLETION_ACTIVITY = "UpdateCompletionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_completion);

        // Display the message to the user indicating that the update process was completed..
        // Requires Reboot...
        // CODE DEVELOPMENT STATUS : 100% SUCCESS

    }

}
