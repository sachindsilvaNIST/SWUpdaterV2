package com.nidec.swupdater.v2;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import java.io.File;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Optional;


/**
 * An Update Description..
 * It will be parsed from JSON file, which is intended to be sent from server to SWUpdater App. But this app will store it on-device.
 */


public class UpdateConfig implements Parcelable {


    // Use of Parcelable : helps to easily pass the instances between Android components like activities or services

    // Download Stream Type - Extracted from the already generated Config.json file.
    public static final int AB_INSTALL_TYPE_NON_STREAMING = 0;
    public static final int AB_INSTALL_TYPE_STREAMING = 1;

    public static String TAG_UPDATE_CONFIG = "UpdateConfig";


    public static final Parcelable.Creator<UpdateConfig> CREATOR = new Parcelable.Creator<UpdateConfig>(){

        @Override
        public UpdateConfig createFromParcel(Parcel source) {
            return new UpdateConfig(source);
        }

        @Override
        public UpdateConfig[] newArray(int size){
            return new UpdateConfig[size];
        }
    };

    // Parse UpdateConfigs from JSON file

    public static UpdateConfig fromJson(String json, String fileroot) throws JSONException {
        UpdateConfig c = new UpdateConfig();


        // Create an instance for JSON File.
        JSONObject o = new JSONObject(json);
        Log.d(TAG_UPDATE_CONFIG,"Parsing JSON Data: "+json);

        // Get the OTA-Package Name from JSON Data
        c.mName = o.getString("name");
        Log.d(TAG_UPDATE_CONFIG,"Extracted name from JSON: " + c.mName);

        if(fileroot != null){
            c.mUrl = "file://" + fileroot + "/" + o.getString("url");
            Log.d("UpdateConfig", "Constructed File URL: " + c.mUrl);

        }
        else {
            c.mUrl = o.getString("url");
            Log.d("UpdateConfig", "Extracted URL: " + c.mUrl);

        }
        switch (o.getString("ab_install_type")) {
            case AB_INSTALL_TYPE_NON_STREAMING_JSON:
                c.mAbInstallType = AB_INSTALL_TYPE_NON_STREAMING;
                Log.d("UpdateConfig", "AB Install Type: NON_STREAMING");
                break;
            case AB_INSTALL_TYPE_STREAMING_JSON:
                c.mAbInstallType = AB_INSTALL_TYPE_STREAMING;
                Log.d("UpdateConfig", "AB Install Type: STREAMING");
                break;
            default:
                Log.e("UpdateConfig", "Invalid ab_install_type value: " + o.getString("ab_install_type"));
                throw new JSONException("Invalid type, expected either "
                        + "NON_STREAMING or STREAMING, got " + o.getString("ab_install_type"));
        }

        // Parse only for A/B updates when non-A/B is implemented

        JSONObject ab = o.getJSONObject("ab_config");

        Log.d("UpdateConfig", "Extracting AB config details...");

        boolean forceSwitchSlot = ab.getBoolean("force_switch_slot");
        boolean verifyPayloadMetadata = ab.getBoolean("verify_payload_metadata");

        Log.d("UpdateConfig", "Force switch slot: " + forceSwitchSlot);
        Log.d("UpdateConfig", "Verify payload metadata: " + verifyPayloadMetadata);

        ArrayList<PackageFile> propertyFiles = new ArrayList<>();


        // Extract the datas from property_files[] Array --> `propertyFiles`
        if (ab.has("property_files")) {
            JSONArray propertyFilesJson = ab.getJSONArray("property_files");
            for (int i = 0; i < propertyFilesJson.length(); i++) {

                // Get the JSON Object at Index `i`
                JSONObject p = propertyFilesJson.getJSONObject(i);

                Log.d("UpdateConfig", "Property file " + (i + 1) + ": " + p.getString("filename") + ", offset: " + p.getLong("offset") + ", size: " + p.getLong("size"));


                // Add the extracted `filename`, `offset` and `size`
                propertyFiles.add(new PackageFile(
                        p.getString("filename"),
                        p.getLong("offset"),
                        p.getLong("size")));
            }
        }

        String authorization = ab.optString("authorization",null);
        Log.d("UpdateConfig", "Authorization: " + authorization);

        c.mAbConfig = new AbConfig(
                forceSwitchSlot,
                verifyPayloadMetadata,
                propertyFiles.toArray(new PackageFile[0]),
                authorization
        );

        Log.d("UpdateConfig", "AB config details set successfully.");

        c.mRawJson = json;
        return c;

    }



    // `STREAMING` / `NON_STREAMING` : Representation types for JSON Config files.
    private static final String AB_INSTALL_TYPE_NON_STREAMING_JSON = "NON_STREAMING";
    private static final String AB_INSTALL_TYPE_STREAMING_JSON = "STREAMING";

    // OTA Package Name which will be displayed in the UI
    private String mName;

    // OTA Package - ZIP File URL, which can be accessed either from `https://` or `file://`
    private String mUrl;

    /** non-streaming (first saves locally) OR streaming (on the fly) */
    private int mAbInstallType;

    /** A/B Update Configurations */
    private AbConfig mAbConfig;

    private String mRawJson;


    /**
     * Add Default constructor for `UpdateConfig` class to prevent warnings..
     */
    protected UpdateConfig () {

    }

    /**
     * Parcel Input Taker from JSON File.
     */
    protected UpdateConfig(Parcel input) {
        this.mName = input.readString();
        this.mUrl = input.readString();
        this.mAbInstallType = input.readInt();
        this.mAbConfig = (AbConfig) input.readSerializable();
        this.mRawJson = input.readString();
    }


    // Function to access OTA - Package Name from the JSON file.
    public String getName() {
        return mName;
    }

    // Function to access OTA - Package URL  from the JSON file.

    public String getUrl() {
        return mUrl;
    }

    public String getRawJson() {
        return mRawJson;
    }

    public int getInstallType() {
        return mAbInstallType;
    }

    public AbConfig getAbConfig(){
        return mAbConfig;
    }

    /**
     * @return File object for given URL
     */
    public File getUpdatePackageFile() {
        if(mAbInstallType != AB_INSTALL_TYPE_NON_STREAMING){
            throw new RuntimeException("Expected non-streaming install type");
        }
        if(!mUrl.startsWith("file://")) {
            throw new RuntimeException("URL is expected to start with file://");
        }
        return new File(mUrl.substring(7,mUrl.length()));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mUrl);
        dest.writeInt(mAbInstallType);
        dest.writeSerializable(mAbConfig);
        dest.writeString(mRawJson);
    }

    /**
     * File Description in OTA-Package ZIP file.
     */


    public static class PackageFile implements Serializable {
        private static final long serialVersionUID = 31043L;


        // FileName in the ZIP Archive
        private String mFileName;

        // Data definition of Update Data at the beginning of ZIP Archive File.
        private long mOffset;

        // Size of Update Data in ZIP Archive File.
        private long mSize;


        public PackageFile(String filename, long offset, long size) {
            this.mFileName = filename;
            this.mOffset = offset;
            this.mSize = size;
        }


        // Access `mFileName` in the ZIP Archive
        public String getFileName() {
            return mFileName;
        }

        // Access `mOffset` in the ZIP Archive
        public long getOffset() {
            return mOffset;
        }

        // Access `mSize` in the ZIP Archive
        public long getSize() {
            return mSize;
        }
    }

        /**
         * A/B (Seamless) Update Configurations.
         */
    public static class AbConfig implements Serializable {

            private static final long serialVersionUID = 31044L;


            // If set to TRUE, the device will boot to new slot, otherwise user manually switches slot on the screen..
            private boolean mForceSwitchSlot;

            // If set to TRUE, the device will boot to new slot, otherwise user manually switches slot on the screen..
            private boolean mVerifyPayloadMetadata;

            // ARRAY for Data defination in the Update Data at the beginning in ZIP Archive.
            private PackageFile[] mPropertyFiles;

            /**
             * SystemUpdaterSample receives the authorization token from the OTA server, in addition
             * to the package URL. It passes on the info to update_engine, so that the latter can
             * fetch the data from the package server directly with the token.
             */

            private String mAuthorization;

            public AbConfig(boolean forceSwitchSlot,
                            boolean verifyPayloadMetadata,
                            PackageFile[] propertyFiles,
                            String authorization) {
                this.mForceSwitchSlot = forceSwitchSlot;
                this.mVerifyPayloadMetadata = verifyPayloadMetadata;
                this.mPropertyFiles = propertyFiles;
                this.mAuthorization = authorization;
            }


            // Access Private `mForceSwitchSlot`
            public boolean getForceSwitchSlot() {
                return mForceSwitchSlot;
            }

            // Access Private `mVerifyPayloadMetadata`

            public boolean getVerifyPayloadMetadata() {
                return mVerifyPayloadMetadata;
            }

            // Access Private Array `mPropertyFiles[]`
            public PackageFile[] getPropertyFiles() {
                return mPropertyFiles;
            }

            // Access Private Token Authorizer `mAuthorization`

            public Optional<String> getAuthorization() {
                return mAuthorization == null ? Optional.empty() : Optional.of(mAuthorization);
            }
        }

}
