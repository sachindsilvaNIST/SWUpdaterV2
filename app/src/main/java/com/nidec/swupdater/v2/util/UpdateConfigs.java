package com.nidec.swupdater.v2.util;

import android.content.Context;
import android.util.Log;
import com.nidec.swupdater.v2.UpdateConfig;

// FOR FILE HANDLING -- TO ACCESS ZIP / JSON FILE(S)
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// FileStreaming

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.io.IOException;
import android.widget.Toast;

// Access from USB Mass Storage using StorageManager Class
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;


// This Utility class will be used to work and validate with JSON Update Configurations.
public final class UpdateConfigs {

    public static final String TAG_UPDATE_CONFIGS = "UpdateConfigs";

    /**
     * @param configs update configs
     * @return list of names
     *
     * RETURN THE LIST OF CONFIG OTA FILES AVAILABLE..
     */

    public static String[] configsToNames(List<UpdateConfig> configs) {
        return configs.stream().map(UpdateConfig::getName).toArray(String[]::new);
    }

    /**
     * @param context app context
     * @return Configs Root Directory
     */

    public static String getConfigsRoot(Context context) {
        return null;
    }
    /**
     * @param context application context
     * @return list of configs from directory {@link UpdateConfigs#getConfigsRoot}
     */

    public static List<UpdateConfig> getUpdateConfigs(Context context) {
        ArrayList<UpdateConfig> configs = new ArrayList<>();


        // Access the file from USB Mass Storage Manager
        final List<StorageVolume> volumeList = context.getSystemService(StorageManager.class).getStorageVolumes();

        // Check whether USB contains any contents (Valid files / folders..)
        if(volumeList != null && !volumeList.isEmpty()) {
            boolean bFound = false;


//            =========================== Start of StorageVolume Looping =============================
            for(final StorageVolume volume : volumeList) {

                // Get the USB Storage's Path
                // Eg : volumePath can be `file://` or `/media/sankyo/`


                // ===> DEBUG TEST SRD 2025-02-26
                String volumePath = null;
                if(volume.getDirectory() != null) {
                    volumePath = volume.getDirectory().getPath();
                } else {
                    Log.w(TAG_UPDATE_CONFIGS,"volume.getDirectory() is null. Skipping.......");
                    continue;
                }

                // <=== DEBUG TEST SRD 2025-02-26

//                final String volumePath = volume.getDirectory().getPath();

                // Eg : updateDirPath can be `file://Update/` or `/media/sankyo/Update/`
                final String updateDirPath = volumePath + File.separator + "Update" + File.separator;
                Log.d(TAG_UPDATE_CONFIGS,"DISPLAYING FILE PATH : " + updateDirPath);


                // Find JSON File in it.
                File file = new File(updateDirPath);

                Log.d(TAG_UPDATE_CONFIGS,"volumePath = " + volumePath);
                Log.d(TAG_UPDATE_CONFIGS,"isDirectory()? : " + file.isDirectory());
                Log.d(TAG_UPDATE_CONFIGS,"Does File Exists? : " + file.exists());
                Log.d(TAG_UPDATE_CONFIGS,"Can READ File? : " + file.canRead());
                Log.d(TAG_UPDATE_CONFIGS,"Absolute Path : " + file.getAbsolutePath());

                File[] fileArray = file.listFiles();
                Log.d(TAG_UPDATE_CONFIGS,"File Array[] : " + fileArray);

                if(fileArray != null) {
                    for(File f : fileArray) {
                        if(!f.isDirectory() && f.getName().endsWith(".json")) {
                            // This file will be our `config.json`
                            Log.d(TAG_UPDATE_CONFIGS,"FOUND JSON FILE : " + f.getName());

                            try {
                                String json = new String(Files.readAllBytes(f.toPath()),StandardCharsets.UTF_8);
                                configs.add(UpdateConfig.fromJson(json,f.getParent()));
                                bFound = true;
                            } catch (Exception e) {
                                Log.e(TAG_UPDATE_CONFIGS,"Can't read or parse JSON Config file --> "+ f.getName(),e);
                                throw new RuntimeException("Can't read or parse JSON Config file --> "+ f.getName(),e);
                            }

                        }
//                        if(bFound) {
//                            break;
//                        }
                    }
                } else {
                    Log.d(TAG_UPDATE_CONFIGS,"file.listFiles() returned NULL for " + updateDirPath);
                }

                if(bFound) {
                    break;
                }

            }
//            =========================== End of StorageVolume Looping ==============================
        }

        return configs;
        // This will return the CONFIG.JSON File (ArrayLists through `configs` object) from USB Mass Storage Device.

    }


    /**
     * @param filename searches by given filename
     * @param config searches in {@link UpdateConfig#getAbConfig()}
     * @return offset and size of {@code filename} in the package zip file
     *         stored as {@link UpdateConfig.PackageFile}.
     *
     *  GET THE ZIP FILE's OFFSET AND SIZE WHICH IS STORED AS {@link UpdateConfig.PackageFile}.
     */

    public static Optional<UpdateConfig.PackageFile> getPropertyFile(final String filename, UpdateConfig config) {
        return Arrays
                .stream(config.getAbConfig().getPropertyFiles())
                .filter(file -> filename.equals(file.getFileName()))
                .findFirst();
    }



    // Defining a default type of `UpdateConfig` class to prevent anonymous warning(s)
    private UpdateConfigs() {}

}
