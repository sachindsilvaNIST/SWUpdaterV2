package com.nidec.swupdater.v2.util;

import com.nidec.swupdater.v2.UpdateConfig;




/**
 * This class is a simple holder for the  config(s) retrieved from `OTAPackageCheckerActivity.java`.
 *
 */


public final class SelectedUpdateConfigHolder {

    private static UpdateConfig sSelectedConfig;


    // To prevent Instantiation.
    private SelectedUpdateConfigHolder() {

    }

    public static void setSelectedConfig(UpdateConfig config) {
        sSelectedConfig = config;
    }


    public static UpdateConfig getSelectedConfig() {
        return sSelectedConfig;
    }

}
