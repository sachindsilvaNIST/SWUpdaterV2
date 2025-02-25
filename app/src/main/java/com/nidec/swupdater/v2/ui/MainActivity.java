package com.nidec.swupdater.v2.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


import com.nidec.swupdater.v2.R;

public class MainActivity extends Activity {

    private Button mApplyButton;
    private Button mViewConfigButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the Default Screen to Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        // Defining IDs for UI Elements
        mApplyButton = findViewById(R.id.mApplyUpdateButton);
        mViewConfigButton = findViewById(R.id.mViewConfigButton);


    }
}
