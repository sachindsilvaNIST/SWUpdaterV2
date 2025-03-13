package com.nidec.swupdater.v2.ui;
/**
 * Action : If the update is successfully applied, ask for reboot, show a screen that says "Update was successful. Please Reboot."
 *
 */


import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import com.nidec.swupdater.v2.R;


import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;




public class UpdateCompletionActivity extends Activity {

    private static final String TAG_UPDATE_COMPLETION_ACTIVITY = "UpdateCompletionActivity";

    private ImageView mImageViewUpdateCompletion;
    private TextView mTextViewUpdateCompletionMainText;
    private TextView mTextViewUpdateCompletionSubText;

    private Button mButtonCloseApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_completion);


        // Accessing IDs of UI Elements.
        mImageViewUpdateCompletion = findViewById(R.id.imageViewComplete);
        mTextViewUpdateCompletionMainText = findViewById(R.id.textViewUpdateCompletionMainText);
        mTextViewUpdateCompletionSubText = findViewById(R.id.textViewUpdateCompletionSubText);
        mButtonCloseApp = findViewById(R.id.buttonCloseApp);


        /**
         * Setting the background theme for "Close" Button.
         */

        GradientDrawable roundedBg = new GradientDrawable();
        roundedBg.setShape(GradientDrawable.RECTANGLE);

        /**
         * Setting button's background color to bluish tone.
         *
         */
        roundedBg.setColor(Color.parseColor("#3A7BD5"));

        /**
         * Converting 10dp to actual pixels for the corner radius.
         */

        float cornerRadiusDp = 10f;
        float cornerRadiusToPixels = cornerRadiusDp * getResources().getDisplayMetrics().density;

        roundedBg.setCornerRadius(cornerRadiusToPixels);

        /**
         * Applying the above modification to CLOSE Button.
         */

        mButtonCloseApp.setBackground(roundedBg);


        /**
         * Adding Event Listener for "CLOSE" Button.
         */

        mButtonCloseApp.setOnClickListener(v -> {
            Log.d(TAG_UPDATE_COMPLETION_ACTIVITY, "CLOSE THE APP Button was pressed to REBOOT...");

            /**
             * Shutting the App Process.
             */
            finishAffinity();

        });






    }








}











//
//import android.app.Activity;
//import android.app.AlertDialog;
//
//import android.content.Intent;
//
//import android.os.Bundle;
//
//import android.util.Log;
//
//import android.view.View;
//
//import android.widget.Button;
//import android.widget.TextView;
//
//import com.nidec.swupdater.v2.R;
////import com.nidec.swupdater.v2.util.UpdateConfigs;
////import com.nidec.swupdater.v2.UpdateConfig;
//

//
//public class UpdateCompletionActivity extends Activity {
//
//    private static final String TAG_UPDATE_COMPLETION_ACTIVITY = "UpdateCompletionActivity";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_update_completion);
//
//        // Display the message to the user indicating that the update process was completed..
//        // Requires Reboot...
//        // CODE DEVELOPMENT STATUS : 100% SUCCESS
//
//    }
//
//}
