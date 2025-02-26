package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.util.UpdateConfigs;
import com.nidec.swupdater.v2.UpdateConfig;

import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG_MAIN_ACTIVITY = "MainActivity";


    // BUTTON : `Apply Update`
    private Button mApplyUpdateButton;

    // BUTTON : `View Config`
    private Button mViewConfigButton;


    // TEXTVIEW : Display JSON Contents
    private TextView mTextViewConfigDetails;

    // Holds UpdateConfig objects found from the USB Drive

    private List<UpdateConfig> mConfigs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the XML of MainActivity --> `activity_main.xml`
        setContentView(R.layout.activity_main);

        // Store the IDs of UI Elements
        mApplyUpdateButton = findViewById(R.id.mApplyUpdateButton);
        mViewConfigButton = findViewById(R.id.mViewConfigButton);
        mTextViewConfigDetails = findViewById(R.id.mTextViewConfigDetails);

        // Load the JSON Configs from the USB Drive
        loadUpdateConfigsFromUSB();

        // Button Listener : `ViewConfig` to display JSON Configs
        mViewConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewConfigClick();
            }
        });
    }

    /**
     * 1. This function `loadUpdateConfigsFromUSB()` uses `UpdateConfigs.java` to scan the USB Drive for "Update" folder and find for ".json" file.
     *
     * 2. JSON Parsing will be done using `UpdateConfig.java` through `UpdateConfig.fromJson(...) method and store them in `mConfigs` object {@link mConfigs}
     */

    private void loadUpdateConfigsFromUSB() {
        Log.d(TAG_MAIN_ACTIVITY,"Loading `UpdateConfigs` from USB...");


        /**
         * This method will scan in External Storage Volumes for an "Update" directory
         *  and then it will look for *.json* file, which will be parsed using `UpdateConfig.java`'s `UpdateConfig.fromJson()`
         */
        mConfigs = UpdateConfigs.getUpdateConfigs(this);


        // Determine whether `mConfigs` object contains valid *.json object(s)
        if(mConfigs == null || mConfigs.isEmpty()) {
            Log.w(TAG_MAIN_ACTIVITY,"SORRY! No Valid JSON Config found in your USB's Update/ folder..");
            mTextViewConfigDetails.setText("Sorry! No Valid JSON Configs found :(");
        } else {
            Log.d(TAG_MAIN_ACTIVITY,"JSON Config Found! : " + mConfigs.size());
        }
    }

    /**
     * Function Implementation : onViewConfigClick()
     *
     * This function will be called when "View Config" Button `mViewConfigButton` is clicked.
     * This function will display the RAW JSON Parsed Configs.
     */
    private void onViewConfigClick() {

        // If `mConfigs` is empty / null, display a dialog-box
        if(mConfigs == null || mConfigs.isEmpty()) {
            showNoConfigJSONFoundDialog();
            return;
        }

        // DEMO TEST SRD 2025-02-26 : Display first config in the list
        UpdateConfig config = mConfigs.get(0);

        // DEMO TEST 1 : SRD 2025-02-26 : Display whole RAW JSON Configs using ALERTDIALOG BUILDER.

//        new AlertDialog.Builder(this)
//                .setTitle("JSON Config: " + config.getName())
//                .setMessage(config.getRawJson())
//                .setPositiveButton("Close",null)
//                .show();

        // DEMO TEST 2 : SRD 2025-02-26 : Display RAW JSON --> Formatted summary
        String mSummary = "Name: " + config.getName() + "\n" +
                          "URL: " + config.getUrl() + "\n" +
                          "Install Type: " +  (config.getInstallType() == UpdateConfig.AB_INSTALL_TYPE_NON_STREAMING? "NON_STREAMING" : "STREAMING") + "\n" +
                          "RAW JSON: \n" + config.getRawJson();

        new AlertDialog.Builder(this)
                .setTitle("JSON Config: " + config.getName())
                .setMessage(mSummary)
                .setPositiveButton("Close",null)
                .show();
    }

    /**
     * Function Implementation : showNoJSONConfigFoundDialog()
     *
     * This function will display a simple dialog box, if no JSON Config was found.
     */

    private void showNoConfigJSONFoundDialog() {

        new AlertDialog.Builder(this)
                .setTitle("No JSON Config")
                .setMessage("Sorry! No Valid JSON Config was found...")
                .setPositiveButton("OK", null)
                .show();
    }
}









//public class MainActivity extends Activity {
//
//    private Button mApplyButton;
//    private Button mViewConfigButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Set the Default Screen to Full Screen
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_main);
//
//
//        // Defining IDs for UI Elements
//        mApplyButton = findViewById(R.id.mApplyUpdateButton);
//        mViewConfigButton = findViewById(R.id.mViewConfigButton);
//
//
//    }
//}
