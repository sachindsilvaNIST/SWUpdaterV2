package com.nidec.swupdater.v2.util;

import android.content.Context;

import android.util.Log;

import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;

import java.io.File;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


// This Utility Class will be used for working with JSON Update Configurations.
public final class UpdateConfigs {

    private static final String TAG_UPDATE_CONFIGS = "UpdateConfigs";
    /**
     * @param configs update configs
     * @return list of names
     */

    public static String[] configsToNames(List<UpdateConfig> configs) {
        return configs.stream().map(UpdateConfig::getName).toArray(String[]::new);
    }

    /**
     * @param context app context
     * @return configs root directory
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

        final List<StorageVolume> volumeList = context.getSystemService(StorageManager.class).getStorageVolumes();

        if((volumeList != null) && !volumeList.isEmpty()) {
            boolean bFound = false;
            for(final StorageVolume volume : volumeList) {

                // Get the USB Storage Path
                final String volumePath = volume.getDirectory().getPath();
                Log.d(TAG_UPDATE_CONFIGS,"VOLUME PATH : " + volumePath);

                final String updateDirPath = volumePath + File.separator + "Update" + File.separator;
                Log.d(TAG_UPDATE_CONFIGS, "`UPDATE` DIRECTORY PATH : " + updateDirPath);

                // Look for required into this `/Update/` Directory.
                File file = new File(updateDirPath);
                File[] fileArray = file.listFiles();

                if(fileArray != null) {
                    for(File f : fileArray) {
                        if(!f.isDirectory() && f.getName().endsWith(".json")) {
                            Log.d(TAG_UPDATE_CONFIGS,"FOUND JSON FILE : " + f.getName().endsWith(".json"));

                            try {
                                String json = new String(Files.readAllBytes(f.toPath()),StandardCharsets.UTF_8);
                                configs.add(UpdateConfig.fromJson(json, f.getParent()));
                                bFound = true;
                            } catch (Exception e) {
                                Log.e(TAG_UPDATE_CONFIGS,"Can't read/parse CONFIG File --> " + f.getName(),e);
                                throw new RuntimeException("Can't read/parse CONFIG File --> " + f.getName(),e);
                            }
                        }
                        if(bFound) {break;}
                    }
                }
                // IF `fileArray` is NULL,
                else {
                        Log.e(TAG_UPDATE_CONFIGS,"FileArray returned NULL!!");
                }
                if(bFound) {break;}
            }
        }

        return configs;
    }



    /**
     * @param filename searches by given filename
     * @param config searches in {@link UpdateConfig#getAbConfig()}
     * @return offset and size of {@code filename} in the package zip file
     *         stored as {@link UpdateConfig.PackageFile}.
     */

    public static Optional<UpdateConfig.PackageFile> getPropertyFile(final String filename, UpdateConfig config) {
        return Arrays
                .stream(config.getAbConfig().getPropertyFiles())
                .filter(file -> filename.equals(file.getFilename()))
                .findFirst();
    }


    // To prevent anonymous constructor warnings..
    private UpdateConfigs() {}
}
