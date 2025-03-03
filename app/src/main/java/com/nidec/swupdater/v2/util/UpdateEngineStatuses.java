package com.nidec.swupdater.v2.util;

import android.util.SparseArray;


/**
 * This helper class is used to work with update_engine's error codes.
 *
 * Many error codes are defined in UpdateEngine.UpdateStatusConstants, but you can find more under,
 * system/update_engine/common/error_code.h
 */


public final class UpdateEngineStatuses {

    private static final SparseArray<String> STATUS_MAP = new SparseArray<>();

    static {
        STATUS_MAP.put(0, "IDLE");
        STATUS_MAP.put(1, "CHECKING_FOR_UPDATE");
        STATUS_MAP.put(2, "UPDATE_AVAILABLE");
        STATUS_MAP.put(3, "DOWNLOADING");
        STATUS_MAP.put(4, "VERIFYING");
        STATUS_MAP.put(5, "FINALIZING");
        STATUS_MAP.put(6, "UPDATED_NEED_REBOOT");
        STATUS_MAP.put(7, "REPORTING_ERROR_EVENT");
        STATUS_MAP.put(8, "ATTEMPTING_ROLLBACK");
        STATUS_MAP.put(9, "DISABLED");
    }


    /**
     * Converts StatusCode to StatusName
     */

    public static String getStatusText(int status) {
        return STATUS_MAP.get(status);
    }

    private UpdateEngineStatuses() {}
}



























