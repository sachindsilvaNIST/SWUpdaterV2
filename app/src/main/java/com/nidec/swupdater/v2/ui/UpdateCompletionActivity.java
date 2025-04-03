package com.nidec.swupdater.v2.ui;



import android.app.Activity;

import android.content.res.ColorStateList;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;


import android.content.Intent;

import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.nidec.swupdater.v2.R;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Action : If the update is successfully applied, ask for reboot, show a screen that says "Update was successful. Please Reboot."
 *
 *  "Update Completed Successfully!"
 *  "Please reboot your device to complete the update.
 *
 *  Date and Time Format --> Updated on : <date/time>
 */


public class UpdateCompletionActivity extends Activity {

    private static final String TAG_UPDATE_COMPLETION_ACTIVITY = "UpdateCompletionActivity";

    private ImageView mImageViewUpdateCompletion;
    private TextView mTextViewUpdateCompletionMainText;
    private TextView mTextViewUpdateCompletionSubText;
    private TextView mTextViewUpdatedTimeInfo;
    private TextView mTextViewRebootInstructions;

//    private Button mButtonCloseApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_completion);

        /**
         * Setting Gradient Background
         */
        setGradientBackground();

        /**
         *  Accessing parent / root layout of UpdateCompletion Activity's XML
         */
        LinearLayout updateCompletionRootLayout = (LinearLayout) findViewById(R.id.updateCompletionRootLayout);
        if(updateCompletionRootLayout != null) {
            updateCompletionRootLayout.setGravity(Gravity.CENTER);
        }


        // Accessing IDs of UI Elements.
        mImageViewUpdateCompletion = findViewById(R.id.imageViewComplete);
        mTextViewUpdateCompletionMainText = findViewById(R.id.textViewUpdateCompletionMainText);
        mTextViewUpdateCompletionSubText = findViewById(R.id.textViewUpdateCompletionSubText);
//        mButtonCloseApp = findViewById(R.id.buttonCloseApp);

        /**
         * Custom tinting the ImageView icon with custom color
         */

        tintImageView(mImageViewUpdateCompletion,"#3A7BD5");

        /**
         * Create a pressed effect for the buttons.
         */

        /**
         * Congratulatory message to the user that the update was successful, please reboot
         */
        mTextViewUpdateCompletionMainText.setText("Update Completed Successfully!");
        mTextViewUpdateCompletionMainText.setTextSize(22);
        mTextViewUpdateCompletionMainText.setTextColor(Color.BLACK);
        mTextViewUpdateCompletionMainText.setTypeface(null, Typeface.BOLD);
        mTextViewUpdateCompletionMainText.setGravity(Gravity.CENTER);


        /**
         * Update completion date and time along with the instructions
         */

        mTextViewUpdateCompletionSubText.setText("Please reboot your device to complete the update");
        mTextViewUpdateCompletionSubText.setTextSize(16);
        mTextViewUpdateCompletionSubText.setTextColor(Color.DKGRAY);
        mTextViewUpdateCompletionSubText.setGravity(Gravity.CENTER);

        /**
         * Updated Time Information
         *
         */
        String currentTime = getCurrentTimeString();

        mTextViewUpdatedTimeInfo = new TextView(this);
        mTextViewUpdatedTimeInfo.setText("Updated on : " + currentTime);
        mTextViewUpdatedTimeInfo.setTextSize(12);
        mTextViewUpdatedTimeInfo.setTextColor(Color.GRAY);
        mTextViewUpdatedTimeInfo.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams updatedTimeInfoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mTextViewUpdatedTimeInfo.setLayoutParams(updatedTimeInfoParams);

        /**
         * Adding Updated Time Information to Update Completion's Root Layout
         */

        if(updateCompletionRootLayout != null) {
            updateCompletionRootLayout.addView(mTextViewUpdatedTimeInfo);
        }


        mTextViewRebootInstructions = new TextView(this);
        mTextViewRebootInstructions.setText("To Reboot, Turn OFF & ON the device by pressing power button.");
        mTextViewRebootInstructions.setTextSize(16);
        mTextViewRebootInstructions.setTextColor(Color.parseColor("#3A7BD5"));
        mTextViewRebootInstructions.setGravity(Gravity.CENTER);

        // Adding margins
        LinearLayout.LayoutParams instructParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        instructParams.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        mTextViewRebootInstructions.setLayoutParams(instructParams);

        /**
         * Adding reboot instructions to Update Completion's Root Layout
         */
        if(updateCompletionRootLayout != null) {
            updateCompletionRootLayout.addView(mTextViewRebootInstructions);
        }


        /**
         * Subtle pulse animation on the checkmark icon to draw attention.
         */

        startPulseAnimation(mImageViewUpdateCompletion);


//        setUpButtonWithPressedEffect(mButtonCloseApp,"#3A7BD5", "#2C63AA",10f );


//        /**
//         * Setting the background theme for "Close" Button.
//         */
//
//        GradientDrawable roundedBg = new GradientDrawable();
//        roundedBg.setShape(GradientDrawable.RECTANGLE);
//
//        /**
//         * Setting button's background color to bluish tone.
//         *
//         */
//        roundedBg.setColor(Color.parseColor("#3A7BD5"));
//
//        /**
//         * Converting 10dp to actual pixels for the corner radius.
//         */
//
//        float cornerRadiusDp = 10f;
//        float cornerRadiusToPixels = cornerRadiusDp * getResources().getDisplayMetrics().density;
//
//        roundedBg.setCornerRadius(cornerRadiusToPixels);
//
//        /**
//         * Applying the above modification to CLOSE Button.
//         */
//
//        mButtonCloseApp.setBackground(roundedBg);


        /**
         * Adding Event Listener for "CLOSE" Button.
         */

//        mButtonCloseApp.setOnClickListener(v -> {
//            Log.d(TAG_UPDATE_COMPLETION_ACTIVITY, "CLOSE THE APP Button was pressed to REBOOT...");
//
//            /**
//             * Shutting the App Process.
//             */
//            finishAffinity();
//
//        });

    }

    /**
     *  Adding gradient background
     */
    private void setGradientBackground() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int[] colors = {Color.parseColor("#E0F7FA"), Color.parseColor("#FFFFFF")};
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            gradientDrawable.setCornerRadius(0f);
            getWindow().getDecorView().setBackground(gradientDrawable);
        }
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
     * Current System Time
     */

    private String getCurrentTimeString() {

        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return mSimpleDateFormat.format(new Date());
    }


    /**
     * Defining the pulse animation function
     */

    private void startPulseAnimation(final ImageView imageView) {
        // Scaling down to 90% and back to 100% - repetitive for subtle pulse effect

        imageView.animate()
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(600)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        imageView.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .setDuration(600)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Repeating the animation continuously.
                                        startPulseAnimation(imageView);
                                    }
                                })
                                .start();
                    }
                })
                .start();
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


}


