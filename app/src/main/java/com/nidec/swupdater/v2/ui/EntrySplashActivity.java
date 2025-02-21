package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.nidec.swupdater.v2.R;
import com.nidec.swupdater.v2.ui.MainActivity;



public class EntrySplashActivity extends Activity {

    // Splash Screen Timeout --> `2 Seconds`
    private static final int SPLASH_TIMEOUT_MS = 2000;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);


        // Making Splash Screen to full window [Set to Default]
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        // Switch / Move to `MainActivity.kt` after Splash Event
        new Handler().postDelayed(() -> {
            // Initiate the `MainActivity`
            Intent intent = new Intent(EntrySplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Terminate the SplashActivity so that it won't be running in the back stack
        },SPLASH_TIMEOUT_MS);

    }
}
