package com.nidec.swupdater.v2.util;

public final class UpdateEngineProperties {

    /**
     * The property indicating that the update_engine shouldn't switch slot when the device reboots.
     */

    public static final String PROPERTY_DISABLE_SWITCH_SLOT_ON_REBOOT = "SWITCH_SLOT_ON_REBOOT=0";


    /**
     * The property to skip post-installation.
     * https://source.android.com/devices/tech/ota/ab/#post-installation
     */

    public static final String PROPERTY_SKIP_POST_INSTALL = "RUN_POST_INSTALL=0";

    private UpdateEngineProperties() {}
}
