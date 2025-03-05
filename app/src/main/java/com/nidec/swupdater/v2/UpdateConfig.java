package com.nidec.swupdater.v2;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.File;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Optional;


/**
 * An update description. It will be parsed from JSON, which is intended to
 * be sent from server to the update app, but in this sample app it will be stored on the device.
 */


public class UpdateConfig implements Parcelable{

    private static final String TAG_UPDATE_CONFIG = "UpdateConfig";

    public static final int AB_INSTALL_TYPE_NON_STREAMING = 0;
    public static final int AB_INSTALL_TYPE_STREAMING = 1;


    public static final Creator<UpdateConfig> CREATOR = new Creator<UpdateConfig>() {
        @Override
        public UpdateConfig createFromParcel(Parcel source) {
            return new UpdateConfig(source);
        }

        @Override
        public UpdateConfig[] newArray(int size) {
            return new UpdateConfig[size];
        }
    };


    // PARSE Update Config from JSON File.

    public static UpdateConfig fromJson(String json, String fileroot) throws JSONException {

        UpdateConfig c = new UpdateConfig();

        JSONObject o = new JSONObject(json);
        Log.d(TAG_UPDATE_CONFIG,"Parsing JSON Data : " + json);

        c.mName = o.getString("name");
        Log.d(TAG_UPDATE_CONFIG,"Extracted Name from JSON : " + c.mName);

        if(fileroot != null) {
            c.mUrl = "file://" + fileroot + "/" + o.getString("url");

            Log.d(TAG_UPDATE_CONFIG,"Constructed file URL : " + c.mUrl);

        } else {
            c.mUrl = o.getString("url");
            Log.d(TAG_UPDATE_CONFIG,"Extracted URL : " + c.mUrl);
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

        // Todo : Parse only for A/B Updates, when non-A/B is implemented.
        JSONObject ab = o.getJSONObject("ab_config");
        Log.d(TAG_UPDATE_CONFIG, "Extracting AB Config details...");


        boolean forceSwitchSlot = ab.getBoolean("force_switch_slot");
        boolean verifyPayloadMetadata = ab.getBoolean("verify_payload_metadata");


        Log.d(TAG_UPDATE_CONFIG, "Force Switch Slot : " + forceSwitchSlot);
        Log.d(TAG_UPDATE_CONFIG, "Verify Payload metadata : " + verifyPayloadMetadata);



        ArrayList<PackageFile> propertyFiles = new ArrayList<>();

        if(ab.has("property_files")) {
            JSONArray propertyFilesJson = ab.getJSONArray("property_files");
            for(int i = 0; i < propertyFilesJson.length(); i++) {
                JSONObject p = propertyFilesJson.getJSONObject(i);
                Log.d(TAG_UPDATE_CONFIG, "Property file " + (i + 1) + ": " + p.getString("filename") + ", offset: " + p.getLong("offset") + ", size: " + p.getLong("size"));

                propertyFiles.add(new PackageFile(
                        p.getString("filename"),
                        p.getLong("offset"),
                        p.getLong("size")
                        ));
            }
        }


        String authorization = ab.optString("authorization",null);

        Log.d(TAG_UPDATE_CONFIG,"Authorization : " + authorization);

        c.mAbConfig = new AbConfig(
                forceSwitchSlot,
                verifyPayloadMetadata,
                propertyFiles.toArray(new PackageFile[0]),
                authorization
        );

        Log.d(TAG_UPDATE_CONFIG,"AB Config Details set successfully..");


        c.mRawJson = json;
        return c;
    }


    /**
     * These strings represenst types in JSON Config files
     */

    private static final String AB_INSTALL_TYPE_NON_STREAMING_JSON = "NON_STREAMING";
    private static final String AB_INSTALL_TYPE_STREAMING_JSON = "STREAMING";

    // The Config Name will be visible on the UI
    private String mName;

    // Update ZIP File URI, it can be https:// or file://
    private String mUrl;

    /** Non-streaming (first saves locally) OR streaming (on the fly) */

    private int mAbInstallType;

    // A/B Update Configurations
    private AbConfig mAbConfig;

    private String mRawJson;

    protected UpdateConfig() {}



    protected UpdateConfig(Parcel input) {
        this.mName = input.readString();
        this.mUrl = input.readString();
        this.mAbInstallType = input.readInt();
        this.mAbConfig = (AbConfig) input.readSerializable();
        this.mRawJson = input.readString();
    }

    public UpdateConfig(String name, String url, int installType) {
        this.mName = name;
        this.mUrl = url;
        this.mAbInstallType = installType;
    }

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getRawJson() {
        return mRawJson;
    }

    public int getInstallType() {
        return mAbInstallType;
    }

    public AbConfig getAbConfig() {
        return mAbConfig;
    }



    /**
     * @return File object for given url
     *
     * OUTPUT : Get the File object for a given URL
     */

    public File getUpdatePackageFile() {
        if(mAbInstallType != AB_INSTALL_TYPE_NON_STREAMING) {
            throw new RuntimeException("Expected non-streaming install type");
        }

        if(!mUrl.startsWith("file://")) {
            throw new RuntimeException("url is expected to start with file://");
        }
        return new File(mUrl.substring(7, mUrl.length()));
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


    // Description of a file in an OTA-package ZIP File.

    public static class PackageFile implements Serializable {
        private static final long serialVersionUID = 31043L;

        // FileName in the ZIP Archive
        private String mFilename;

        // This defines the beginning of Update Data in the ZIP Archive.
        private long mOffset;

        // Size of Update Data in the ZIP Archive.
        private long mSize;


        public PackageFile(String filename, long offset, long size) {
            this.mFilename = filename;
            this.mOffset = offset;
            this.mSize = size;
        }

        public String getFilename() {
            return mFilename;
        }

        public long getOffset() {
            return mOffset;
        }

        public long getSize() {
            return mSize;
        }

    }

// A/B (Seamless) Update Data Configurations.

    public static class AbConfig implements Serializable {

        private static final long serialVersionUID = 31044L;


        /**
         * If set TRUE, the device will boot to new slot, otherwise user manually
         * switches slot on the screen.
         */

        private boolean mForceSwitchSlot;


        /**
         * If set TRUE, the device will boot to new slot, otherwise user manually
         * switches slot on the screen.
         */
        private boolean mVerifyPayloadMetadata;

        // Defines beginning of Update Data in the ZIP Archive;
        private PackageFile[] mPropertyFiles;


        /**
         * SWUpdaterV2 will receive the authorization token from the OTA server, in addition
         * to the package URL. It passes on the info to update_engine, so that the latter can
         * fetch the data from the package server directly with the token.
         */

        private String mAuthorization;

        public AbConfig(boolean forceSwitchSlot, boolean verifyPayloadMetadata, PackageFile[] propertyFiles, String authorization) {
            this.mForceSwitchSlot = forceSwitchSlot;
            this.mVerifyPayloadMetadata = verifyPayloadMetadata;
            this.mPropertyFiles = propertyFiles;
            this.mAuthorization = authorization;
        }


        public boolean getForceSwitchSlot() {
            return mForceSwitchSlot;
        }

        public boolean getVerifyPayloadMetadata() {
            return mVerifyPayloadMetadata;
        }

        public PackageFile[] getPropertyFiles() {
            return mPropertyFiles;
        }

        public Optional<String> getAuthorization() {
            return mAuthorization == null? Optional.empty() : Optional.of(mAuthorization);
        }
    }

}
