package com.nidec.swupdater.v2.ui;

import android.os.Handler;
import android.os.UpdateEngine;

import com.nidec.swupdater.v2.UpdateManager;


/**
 * Singleton class holder for the shared UpdateManager instance.
 * This will ensure the same ephemeral state is used to share across multiple activities.
 */

public final class UpdateManagerHolder {

    private static volatile UpdateManager sInstance;


    // To prevent instantiation.
    private UpdateManagerHolder() {}

    public static UpdateManager getInstance() {
        if(sInstance == null) {
            synchronized (UpdateManagerHolder.class) {
                if(sInstance == null) {
                    // We create only one instance.
                    sInstance = new UpdateManager(new UpdateEngine(), new Handler());
                }
            }
        }
        return sInstance;
    }


}
