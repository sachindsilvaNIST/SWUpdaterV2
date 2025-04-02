package com.nidec.swupdater.v2.ui;

/**
 * Action :
 * 1. Show the progress (percentage) of download the OTA Package.
 * 2. If download finishes successfully, or the UpdateEngine signals "Requires Reboot", go to "UpdateCompletionActivity"
 * 3. If user presses "Cancel", then go back to "OTAPackageCheckerActivity" / "UpdateCompletionActivity"
 * 4. When the Progress Rate hits 99%, FINALIZE the Engine Update - SUCCESS.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;

import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UpdateEngine;

import android.transition.Transition;
import android.transition.AutoTransition;
import android.transition.TransitionManager;

import android.util.Log;

import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.UpdaterState;
import com.nidec.swupdater.v2.UpdateManager;
import com.nidec.swupdater.v2.util.UpdateConfigs;
import com.nidec.swupdater.v2.UpdateConfig;

import com.nidec.swupdater.v2.ui.UpdateManagerHolder;
import com.nidec.swupdater.v2.util.UpdateEngineStatuses;


public class ProgressScreenActivity extends Activity {
    private static final String TAG_PROGRESS_SCREEN_ACTIVITY = "ProgressScreenActivity";


    // ImageView : Download Icon
    private ImageView mImageViewDownloadIcon;

    // TextView for MainTitle
    private TextView mTextViewProgressTitle;

    //TextView for Subtitle
    private TextView mTextViewProgressSubtitle;

    // Progress Bar : Loading Spinner
    private ProgressBar mProgressBar;

    // "Cancel Update" Button.
    private Button mCancelDownloadButton;

    // TextView to display the Download Progress in percentage %...
    private TextView mProgressScreenPercentDisplay;


    private UpdateManager mUpdateManager;

    private ProgressDialog mSpinnerDialog;

    // Storing the current engine status to detect the "FINALIZING"
    private int mCurrentEngineStatus = UpdateEngine.UpdateStatusConstants.IDLE;

    // A flag to ensure finalizing animation is started only once.
    private boolean isFinalizingAnimationStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_screen);


        /**
         * Retrieving IDs of UI Elements...
         */
        mImageViewDownloadIcon = findViewById(R.id.imageViewDownloadIcon);
        mTextViewProgressTitle = findViewById(R.id.textViewProgressTitle);
        mTextViewProgressSubtitle = findViewById(R.id.textViewProgressSubtitle);
        mProgressBar = findViewById(R.id.progressBar);
        mCancelDownloadButton = findViewById(R.id.buttonCancelDownload);
        mProgressScreenPercentDisplay = findViewById(R.id.TextViewProgressScreenPercentDisplay);


        /**
         * Custom tinting the ImageView icon with custom color
         */
        tintImageView(mImageViewDownloadIcon,"#3A7BD5");


        /**
         * Tinting Loading Spinner's color to bluish tone.
         *
         */

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mProgressBar.setIndeterminateTintList(
                    ColorStateList.valueOf(Color.parseColor("#3A7BD5"))
            );
        }


        /**
         *  Creating a pressed effect for "Cancel Update" Button
         */

        setUpButtonWithPressedEffect(mCancelDownloadButton, "#3A7BD5", "#2C63AA", 10f);

//        /**
//         * Setting the "Cancel Update" button background to rounded...
//         *
//         */
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
//        float cornerRadiusDp = 10f;
//        float cornerRadiusToPixels = cornerRadiusDp * getResources().getDisplayMetrics().density;
//        roundedBg.setCornerRadius(cornerRadiusToPixels);
//
//        /**
//         * Applying the above modification to "Cancel Update" Button.
//         */
//
//        mCancelDownloadButton.setBackground(roundedBg);

        // Retrieve the shared UpdateManager Instance..
        mUpdateManager = UpdateManagerHolder.getInstance();

        /**
         * Set the callbacks for Update Status Changes and Progress Status Changes
         */

        mUpdateManager.setOnStateChangeCallback(this::onUpdaterStateChange);
        mUpdateManager.setOnEngineStatusUpdateCallback(this::onEngineStatusUpdate);
        mUpdateManager.setOnProgressUpdateCallback(this::onProgressChanged);


        /**
         * Callback for Engine - complete callback..
         */

//        mUpdateManager.setOnEngineCompleteCallback(this::onEngineComplete);


        /**
         * BUTTON : `Cancel Update` Action Listener
         */

        mCancelDownloadButton.setOnClickListener((View v) -> {

            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "USER HAD PRESSED `Cancel Update` Button!!!!");

            /**
             * When "Cancel Update" was pressed, show the Custom Confirmation Dialog box.
             */

            showModernCancelConfirmationDialog();


            // If the user cancels the OTA Update, Display a Confirmation Box (Yes / No) whether user wants to cancel the update.

//            new AlertDialog.Builder(this)
//                    .setTitle("User Request to Terminate Update")
//                    .setMessage("Do you really want to cancel this update?\n")
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
//
//                        /**
//                         * Cancelling the Update Engine
//                         */
//                        handleUpdateEngineCancelButton();
//                    })
//                    .setNegativeButton(android.R.string.cancel,null)
//                    .show();

        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Bind `ProgressScreenActivity.java`, so that we can get the UpdateEngine status updates.
        mUpdateManager.bind();

        /**
         * Manually synchronize with the UpdateEngine, in case the update is
         * already done or in REBOOT_REQUIRED state.
         */

        synchronizeEngineState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUpdateManager.unbind();

        // Remove callbacks to prevent memory leaks
        mUpdateManager.setOnStateChangeCallback(null);
        mUpdateManager.setOnEngineStatusUpdateCallback(null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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
     * Defining Loading Spinner function.
     */

    private void showingLoadingSpinner(String message) {
        if(mSpinnerDialog == null) {
            mSpinnerDialog = new ProgressDialog(this);
            mSpinnerDialog.setIndeterminate(true);
            mSpinnerDialog.setCancelable(false);
        }
        mSpinnerDialog.setMessage(message);
        mSpinnerDialog.show();
    }


    /**
     * Hiding the Loading Spinner after few seconds.
     */

    private void hideLoadingSpinner() {
        if(mSpinnerDialog != null && mSpinnerDialog.isShowing()) {
            mSpinnerDialog.dismiss();
        }
    }


    /**
     * Display the Loading Spinner, once the OTA Update is complete and the update engine initiates for "REBOOT_REQUIRED" FLAG
     */
    private void showApplyingUpdateLoadingSpinner() {
        showingLoadingSpinner("Applying updates...");
        new Handler().postDelayed(() -> {
            hideLoadingSpinner();
        },2000);
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
//    private void setUpButtonWithPressedEffect(Button mButton, String normalColor, String pressedColor, float cornerRadiusDp) {
//        /**
//         * Converting 10dp to actual pixels for the corner radius.
//         *
//         * */
//
//        float cornerRadiusToPixels = cornerRadiusDp * getResources().getDisplayMetrics().density;
//
//        /**
//         * Normal state of button before press effect
//         */
//        GradientDrawable normalDrawable = new GradientDrawable();
//        normalDrawable.setShape(GradientDrawable.RECTANGLE);
//        normalDrawable.setCornerRadius(cornerRadiusToPixels);
//        normalDrawable.setColor(Color.parseColor(normalColor));
//
//        /**
//         * Pressed state of button after press effect
//         */
//
//        GradientDrawable pressedDrawable = new GradientDrawable();
//        pressedDrawable.setShape(GradientDrawable.RECTANGLE);
//        pressedDrawable.setCornerRadius(cornerRadiusToPixels);
//        pressedDrawable.setColor(Color.parseColor(pressedColor));
//
//        /**
//         * State list drawable for pressed effect...
//         */
//        StateListDrawable states = new StateListDrawable();
//
//        // Pressed state (when android:state_pressed = true)
//        states.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
//
//        // Default state
//        states.addState(new int[]{}, normalDrawable);
//
//        /**
//         * Add the above effects to the button.
//         */
//
//        mButton.setBackground(states);
//
//    }







    /**
     * This function ensures that we don't show "IDLE", if the UpdateEngine has already completed the download process,
     * and at "REBOOT_REQUIRED" or some other state(s)...
     *
     * We forcibly read the engine status and update the internal UpdaterState accordingly..
     */

    private void synchronizeEngineState() {

        int currentEngineStatus = mUpdateManager.getEngineStatus();
        String currentEngineStatusToText = UpdaterState.getStateText(currentEngineStatus);
        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Engine Status Code => " + currentEngineStatus);
        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Engine Status Code (TEXT) => " + currentEngineStatusToText);

        /**
         * Now, if the UpdateEngine is in "UPDATED_NEED_REBOOT" state,
         * we set the app state to "REBOOT_REQUIRED"
         */

        if(currentEngineStatus == UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT) {
//            mUpdateManager.setUpdaterStateSilent(UpdaterState.REBOOT_REQUIRED);
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, " Requires REBOOT!!! --> Switching to `RebootCheckActivity.java`");
            startActivity(new Intent(this,RebootCheckActivity.class));
            finish();
            return;
        }


        /**
         * The SWUpdaterV2 app has to check the current state to see if we should move on or not.
         */
        checkIfComplete(mUpdateManager.getUpdaterState());
    }

    /**
     * This function will be called when the SWUpdaterV2's Internal State changes.
     */

    private void onUpdaterStateChange(int newState) {

        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "onUpdaterStateChange => " + UpdaterState.getStateText(newState));

        runOnUiThread(() -> checkIfComplete(newState));

    }

    /**
     * Callback for RAW Update Engine Status Change
     * Function : onEngineStatusUpdate()
     */

    private void onEngineStatusUpdate(int status) {
        mCurrentEngineStatus = status;
        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "onEngineStatusUpdate IN PLAINTEXT ==> " + UpdateEngineStatuses.getStatusText(status));
        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "onEngineStatusUpdate IN CODE ==> " + status);
    }


    /**
     * This function will be called on every download progress update (0.0 to 1.0).
     */

    private void onProgressChanged(double progress) {
        runOnUiThread(() -> {
            int percent = (int) (100 * progress);
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "CURRENT DOWNLOAD PROGRESS RATE => " + percent + "%");
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Current Engine Status => " + mCurrentEngineStatus);


            /**
             * If the Current Engine Status = FINALIZING, override the `mProgressScreenPercentDisplay.setText()` to retain at 99%.
             */

            if(mCurrentEngineStatus == UpdateEngine.UpdateStatusConstants.FINALIZING) {
                handleFinalizingState();
                return;
            }

            /**
             * Handle the Progress Rate State for the percent <= 99%
             */
            mProgressBar.setProgress(percent);
            if(percent < 99) {
                if(mCurrentEngineStatus == UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT) { // "UPDATED_NEED_REBOOT"
                    mProgressBar.setProgress(100);
                    mProgressScreenPercentDisplay.setText("100%");
                    return;
                }
                // Show the progress rate in normal percentage format
                mProgressScreenPercentDisplay.setText(percent + "%");
            } else if(percent >= 99 && percent < 100) {
                // Treating this progress rate part as "FINALIZING"
                handleFinalizingState();
            } else {
                // Progress Rate will be 100%
                Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "HIT 100%... Current Engine Status => " + mCurrentEngineStatus);
                mProgressScreenPercentDisplay.setText("Installing... Please wait");

            }

        });
    }

    /**
     * Handling "FINALIZING" state when the progress rate hits 99%.
     */

    private void handleFinalizingState() {
        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Initiating ===> handleFinalizingState() Function....");
        mProgressScreenPercentDisplay.setText("Updating...");
//        mProgressBar.setProgress(99);
        mProgressBar.setProgress(100); // DEBUG TEST


        /**
         * Adding smooth Fade out transition for subtitle(Set to Invisible state)
         */
        smoothlyHideProgressSubtitle();


        // Disable "Cancel Update" Button when progress rate is at 99% and set color to gray.
        disableCancelUpdateButton();

        // Start a subtle rotation animation on the download icon, if not already started...
        if(!isFinalizingAnimationStarted) {
            isFinalizingAnimationStarted = true;
            mImageViewDownloadIcon.animate()
                    .rotationBy(360)
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(2000)
                    .start();
        }
    }

    /**
     * Smooth Fade out transition of ProgressScreen Subtitle
     */
    private void smoothlyHideProgressSubtitle() {
        // If the subtitle is already hidden, do nothing and return the function.
        if(mTextViewProgressSubtitle.getVisibility() != View.VISIBLE) {
            return;
        }

        mTextViewProgressSubtitle.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction(() -> {

                    // After ProgressScreen Subtitle Fade out, Smooth Transition of "ProgressScreenDisplay" and "Cancel Update" Button.

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        ViewGroup parent = (ViewGroup) mTextViewProgressSubtitle.getParent();
                        Transition transition = new AutoTransition();
                        transition.setDuration(400);
                        TransitionManager.beginDelayedTransition(parent, transition);

                    }
                    mTextViewProgressSubtitle.setVisibility(View.GONE);
                });

    }




//    private void onProgressChanged(double progress) {
//        runOnUiThread(() -> {
//            int percent = (int) (100 * progress);
//            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "CURRENT DOWNLOAD PROGRESS RATE => " + percent + "%");
//            mProgressBar.setProgress(percent);
//
//            if(percent < 99) {
//                // Show the progress rate in normal percentage format
//                mProgressScreenPercentDisplay.setText(percent + "%");
//            } else if(percent >= 99 && percent < 100) {
//                // At 99%, show "Loading, finalizing..." instead of a numeric value.
//                mProgressScreenPercentDisplay.setText("Finalizing, please wait...");
//
//                /**
//                 * Fade out and hide the subtitle --> "Please wait. Your system is updating..." if visible.
//                 */
//                if(mTextViewProgressSubtitle.getVisibility() == View.VISIBLE) {
//                    mTextViewProgressSubtitle.animate()
//                            .alpha(0)
//                            .setDuration(500)
//                            .withEndAction(() -> mTextViewProgressSubtitle.setVisibility(View.GONE));
//                }
//
//                // Disable "Cancel Update" Button when progress rate is at 99% and set color to gray.
//
//                disableCancelUpdateButton();
//
//                // Start a subtle rotation animation on the download icon, if not already started...
//                if(!isFinalizingAnimationStarted) {
//                    isFinalizingAnimationStarted = true;
//                    mImageViewDownloadIcon.animate()
//                            .rotationBy(360)
//                            .setInterpolator(new LinearInterpolator())
//                            .setDuration(2000)
//                            .start();
//                }
//
//            } else {
//                /**
//                 * When the progress rate hits 100%, show "Updating... Please wait
//                 */
//                mProgressScreenPercentDisplay.setText("Updating... Please wait");
//            }
//
//
//        });
//    }


//    private void onProgressChanged(double progress) {
//        runOnUiThread(() -> {
//            int percent = (int) (100 * progress);
//            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Current Download Progress => " + percent + "%");
//            mProgressBar.setProgress(percent);
//            mProgressScreenPercentDisplay.setText(percent + "%");
//            if(percent == 99) {
//                percent = percent + 1;
//                int currentUpdateState = mUpdateManager.getUpdaterState();
//                mProgressScreenPercentDisplay.setText(percent + "%");
//                checkIfComplete(currentUpdateState);
//            }
//            if(percent == 100) {
//                int currentUpdateState = mUpdateManager.getUpdaterState();
//                mProgressScreenPercentDisplay.setText("Processing. Please wait...");
//                checkIfComplete(currentUpdateState);
//            }
//        });
//    }



    /**
     * Defining a function to disable "Cancel Update" button and change its default color to gray.
     */

    private void disableCancelUpdateButton() {
        mCancelDownloadButton.setEnabled(false);
        GradientDrawable disabledBackground = new GradientDrawable();
        disabledBackground.setShape(GradientDrawable.RECTANGLE);
        disabledBackground.setColor(Color.parseColor("#B0B0B0"));
        float cornerRadiusToPixels = 10f * getResources().getDisplayMetrics().density;
        disabledBackground.setCornerRadius(cornerRadiusToPixels);

        mCancelDownloadButton.setBackground(disabledBackground);
    }


    /**
     * -------------------------------------
     * CUSTOM CONFIRMATION DIALOG BOX
     *
     * Display custom confirmation dialog box when user presses "Cancel Update" Button.
     *
     */

    private void showModernCancelConfirmationDialog() {

        /**
         * Creating Rounded Container Background
         */

        int containerPadding = (int) (16 * getResources().getDisplayMetrics().density);
        GradientDrawable containerBackground = new GradientDrawable();
        containerBackground.setColor(Color.WHITE);
        // Set the rounded corners
        containerBackground.setCornerRadius(40 * getResources().getDisplayMetrics().density);

        // Creating parent layout
        LinearLayout parentLayout = new LinearLayout(this);
        parentLayout.setOrientation(LinearLayout.VERTICAL);
        parentLayout.setPadding(containerPadding, containerPadding, containerPadding, containerPadding);
        parentLayout.setBackground(containerBackground);




//        // Creating parent layout
//        LinearLayout parentLayout = new LinearLayout(this);
//        parentLayout.setOrientation(LinearLayout.VERTICAL);
//        int padding = (int) (16 * getResources().getDisplayMetrics().density);
//        parentLayout.setPadding(padding, padding, padding, padding);
//        parentLayout.setBackgroundColor(Color.WHITE);

        /**
         * Confirmation Dialog - Title
         */
        TextView titleView = new TextView(this);
        titleView.setText("Terminate Update");
        titleView.setTextSize(20);
        titleView.setTextColor(Color.BLACK);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setPadding(0, 0, 0, containerPadding / 2);
        parentLayout.addView(titleView);

        /**
         * Confirmation Dialog - Message
         */
        TextView messageView = new TextView(this);
        messageView.setText("Do you really want to cancel this update?");
        messageView.setTextSize(16);
        messageView.setTextColor(Color.DKGRAY);
        messageView.setPadding(0, 0, 0, containerPadding);
        parentLayout.addView(messageView);

        /**
         * Confirmation Dialog - Horizontal LinearLayout for Buttons.
         */
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.END);
//        buttonLayout.setPadding(0, containerPadding / 2,0, 0);

        /**
         * Confirmation Dialog - "Cancel" Button --> Dismiss the Dialog
         */
        Button cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setTextColor(Color.parseColor("#3A7BD5"));
        cancelButton.setBackground(createRoundedDrawable("#FFFFFF", 8));
        cancelButton.setPadding(containerPadding, containerPadding /2, containerPadding, containerPadding /2);
        // Setting the margin
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        int marginRightForCancelButton = (int) (8 * getResources().getDisplayMetrics().density);
        int marginTopForCancelButton = (int) (8 * getResources().getDisplayMetrics().density);
        int marginBottomForCancelButton = (int) (8 * getResources().getDisplayMetrics().density);
        cancelParams.setMargins(0, marginTopForCancelButton, marginRightForCancelButton, marginBottomForCancelButton);
        cancelButton.setLayoutParams(cancelParams);






        /**
         * Confirmation Dialog - "OK" Button --> Initiate Update Engine Cancel Process.
         */
        Button okButton = new Button(this);

        /**
         * Apply the ripple animation for "OK" Button (When pressed)
         */
        setUpButtonWithPressedEffect(okButton, "#3A7BD5", "#2C63AA", 8f);

        okButton.setText("OK");
        okButton.setTextColor(Color.WHITE);
        okButton.setBackground(createRoundedDrawable("#3A7BD5", 8));
        okButton.setPadding(containerPadding, containerPadding / 2, containerPadding, containerPadding / 2);
        LinearLayout.LayoutParams okParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int marginTopForOKButton = (int) (8 * getResources().getDisplayMetrics().density);
        int marginBottomForOKButton = (int) (8 * getResources().getDisplayMetrics().density);
        okParams.setMargins(0,marginTopForOKButton,0,marginBottomForOKButton);

        okButton.setLayoutParams(okParams);

        okButton.setOnClickListener(v -> {

            // Cancel the update running and navigate back.
            handleUpdateEngineCancelButton();
        });

        /**
         * Adding buttons to the button layout
         */

        buttonLayout.addView(cancelButton);
        buttonLayout.addView(okButton);

        parentLayout.addView(buttonLayout);

        /**
         * Building AlertDialog with custom view.
         */
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(parentLayout)
                .create();


        /**
         * Remove default background for modern look
         */
        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(null);
        }
        dialog.show();

        /**
         * Setting the Event Listener for "Cancel" Button, when pressed dismiss dialog.
         */

        cancelButton.setOnClickListener(v -> dialog.dismiss());

    }

    /**
     *  Function to create a custom rounded corner radius for Buttons.
     *  Function  : createRoundedDrawable()
     */

    private GradientDrawable createRoundedDrawable(String colorHex, int cornerRadiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);

        float cornerRadiusToPixels = cornerRadiusDp * getResources().getDisplayMetrics().density;
        drawable.setCornerRadius(cornerRadiusToPixels);
        drawable.setColor(Color.parseColor(colorHex));
        return drawable;
    }




    /**
     * Handle "Cancel Update" Button --> Initiating Update Engine = CANCEL
     */

    private void handleUpdateEngineCancelButton() {
        try {
            mUpdateManager.cancelRunningUpdate();
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "OTA UPDATE GOT PAUSED AND UDPATE ENGINE WAS SET TO IDLE....");

        } catch (UpdaterState.InvalidTransitionException e) {
            Log.e(TAG_PROGRESS_SCREEN_ACTIVITY, "FAILED TO CANCEL THE UPDATE... : ",e);
        }

        /**
         *  If user presses "Cancel Update" --> "OK" button, then go back to "OTAPackageCheckerActivity.java"
         *
         */

        Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "USER has pressed `Cancel Update` BUTTON, SWITCHING --> OTAPackageCheckerActivity.java");

        startActivity(new Intent(this, OTAPackageCheckerActivity.class));
        finish();
    }




    /**
     *
     * If the update is complete or the engine status is `REBOOT_REQUIRED`,
     *      Switch to `RebootCheckActivity.java`
     *
     */

    private void checkIfComplete(int updaterState) {

        // On 100% progress, signaled by `REBOOT_REQUIRED`,or engine-complete callback.

        if(updaterState == UpdaterState.REBOOT_REQUIRED) {
            // Commented by Sachin.R.Dsilva 2025-04-01, for bug fixes and improvement phase operation. WILL BE UNCOMMENTED LATER
//            showApplyingUpdateLoadingSpinner();
            mProgressBar.setProgress(100);
            mProgressScreenPercentDisplay.setText("100%");
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Updater State says REBOOT_REQUIRED.... --> Switching to RebootCheckActivity.java");
            startActivity(new Intent(this,RebootCheckActivity.class));
            finish();
        } else if (updaterState == UpdaterState.SLOT_SWITCH_REQUIRED) {
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Updater State says SLOT_SWITCH_REQUIRED.... --> Switching to RebootCheckActivity.java");
            startActivity(new Intent(this,RebootCheckActivity.class));
            finish();
        } else if (updaterState == UpdaterState.ERROR) {
            Log.d(TAG_PROGRESS_SCREEN_ACTIVITY, "Updater State says ERROR.... --> Switching to SystemUpToDateActivity.java");
            startActivity(new Intent(this,SystemUpToDateActivity.class));
            finish();
        }

    }

}