package com.nidec.swupdater.v2.util;

// This Utility Class will be used for OTA Package.
public final class PackageFiles {

    // Directory used to perform updates.
    public static final String OTA_PACKAGE_DIR = "/data/ota_package";

    // Update payload, it will be passed to {@code UpdateEngine#applyPayload}
    public static final String PAYLOAD_BINARY_FILE_NAME = "payload.bin";


    /**
     * Currently, when calling {@code UpdateEngine#applyPayload} to perform actions
     * that don't require network access (e.g. change slot), update_engine still
     * talks to the server to download/verify file.
     * {@code update_engine} might throw error when rebooting if {@code UpdateEngine#applyPayload}
     * is not supplied right headers and tokens.
     * This behavior might change in future android versions.
     *
     * To avoid extra network request in {@code update_engine}, this file has to be
     * downloaded and put in {@code OTA_PACKAGE_DIR}.
     */

    public static final String PAYLOAD_METADATA_FILE_NAME = "payload_metadata.bin";

    public static final String PAYLOAD_PROPERTIES_FILE_NAME = "payload_properties.txt";

    /**
     * The ZIP Entry in an A/B OTA Package, which will be used by update_verifier.
     */

    public static final String CARE_MAP_FILE_NAME = "care_map.txt";

    public static final String METADATA_FILE_NAME = "metadata";


    /**
     * The ZIP file that claims the compatibility of the update package to check against the Android Framework to ensure that the package can be installed on the device.
     */
    public static final String COMPATIBILITY_ZIP_FILE_NAME = "compatibility.zip";

    private PackageFiles() {}

}
