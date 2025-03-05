package com.nidec.swupdater.v2.util;

import android.os.UpdateEngine;

import android.util.SparseArray;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/** `UpdateEngineErrorCodes` is a helper class used to work with UpdateEngine's error codes.
 * Many Error codes are defined in {@link UpdateEngine.ErrorCodeConstants}, but you can find more in system/update_engine/common/error_code.h
 */

public class UpdateEngineErrorCodes {

    /**
     * Error code from the UpdateEngine.
     * Values must agree with the ones in system/update_engine/common/error_code.h
     */

    public static final int UNKNOWN = -1;
    public static final int UPDATED_BUT_NOT_ACTIVE = 52;
    public static final int USER_CANCELLED = 48;

    private static final SparseArray<String> CODE_TO_NAME_MAP = new SparseArray<>();

    static {
        CODE_TO_NAME_MAP.put(0, "SUCCESS");
        CODE_TO_NAME_MAP.put(1, "ERROR");
        CODE_TO_NAME_MAP.put(4, "FILESYSTEM_COPIER_ERROR");
        CODE_TO_NAME_MAP.put(5, "POST_INSTALL_RUNNER_ERROR");
        CODE_TO_NAME_MAP.put(6, "PAYLOAD_MISMATCHED_TYPE_ERROR");
        CODE_TO_NAME_MAP.put(7, "INSTALL_DEVICE_OPEN_ERROR");
        CODE_TO_NAME_MAP.put(8, "KERNEL_DEVICE_OPEN_ERROR");
        CODE_TO_NAME_MAP.put(9, "DOWNLOAD_TRANSFER_ERROR");
        CODE_TO_NAME_MAP.put(10, "PAYLOAD_HASH_MISMATCH_ERROR");
        CODE_TO_NAME_MAP.put(11, "PAYLOAD_SIZE_MISMATCH_ERROR");
        CODE_TO_NAME_MAP.put(12, "DOWNLOAD_PAYLOAD_VERIFICATION_ERROR");
        CODE_TO_NAME_MAP.put(15, "NEW_ROOTFS_VERIFICATION_ERROR");
        CODE_TO_NAME_MAP.put(20, "DOWNLOAD_STATE_INITIALIZATION_ERROR");
        CODE_TO_NAME_MAP.put(26, "DOWNLOAD_METADATA_SIGNATURE_MISMATCH");
        CODE_TO_NAME_MAP.put(48, "USER_CANCELLED");
        CODE_TO_NAME_MAP.put(52, "UPDATED_BUT_NOT_ACTIVE");
    }


    /**
     * Completion codes returned by Update Engine indicating that the update was successfully applied...
     */

    private static final Set<Integer> SUCCEEDED_COMPLETION_CODES = new HashSet<>(
            Arrays.asList(UpdateEngine.ErrorCodeConstants.SUCCESS,
                    // UPDATED_BUT_NOT_ACTIVE is returned when the payload is
                    // successfully applied but the
                    // device won't switch to the new slot after the next boot.
                    UPDATED_BUT_NOT_ACTIVE));


    /**
     * Checks if the Update succeeded using errorCode
     */

    public static boolean isUpdateSucceeded(int errorCode) {
        return SUCCEEDED_COMPLETION_CODES.contains(errorCode);
    }

    /**
     * Converts error code to error name
     */

    public static String getCodeName(int errorCode) {
        return CODE_TO_NAME_MAP.get(errorCode);
    }

    private UpdateEngineErrorCodes() {}
    
}
