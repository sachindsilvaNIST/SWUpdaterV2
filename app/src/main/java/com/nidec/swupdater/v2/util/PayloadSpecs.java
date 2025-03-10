package com.nidec.swupdater.v2.util;


import com.nidec.swupdater.v2.PayloadSpec;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.file.Files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;



// `PayloadSpecs` is a helper class that created `PayloadSpec`

public class PayloadSpecs {

    public PayloadSpecs() {}

    /**
     * The Payload PAYLOAD_ENTRY is stored in ZIP Package to comply with Android's OTA Package format.
     * We want to find out the offset of the entry, so that we can pass it over to the A/B Updater without making an extra copy of the payload.
     *
     * According to Android docs, the entries are listed in the order in which they appear in the ZIP File.
     * So we enumerate the entries to identify the offset of the payload file.
     * http://developer.android.com/reference/java/util/zip/ZipFile.html#entries()
     */


    public PayloadSpec forNonStreaming(File packageFile) throws IOException {
        boolean payloadFound = false;
        long payloadOffset = 0;
        long payloadSize = 0;

        List<String> properties = new ArrayList<>();

        try(ZipFile zip = new ZipFile(packageFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            long offset = 0;
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();

                // ZIP Local file header has 30 bytes + filename + sizeof(extra field).
                // LINK for More : https://en.wikipedia.org/wiki/Zip_(file_format)
                long extraSize = entry.getExtra() == null ? 0 : entry.getExtra().length;
                offset += 30 + name.length() + extraSize;

                if(entry.isDirectory()) {
                    continue;
                }

                long length = entry.getCompressedSize();
                if(PackageFiles.PAYLOAD_BINARY_FILE_NAME.equals(name)) {
                    if(entry.getMethod() != ZipEntry.STORED) {
                        throw new IOException("INVALID COMPRESSION METHOD!!");
                    }
                    payloadFound = true;
                    payloadOffset = offset;
                    payloadSize = length;
                } else if(PackageFiles.PAYLOAD_PROPERTIES_FILE_NAME.equals(name)) {
                    InputStream inputStream = zip.getInputStream(entry);
                    if(inputStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while((line = br.readLine()) != null) {
                            properties.add(line);
                        }
                    }
                }
                offset += length;
            }
        }

        if(!payloadFound) {
            throw new IOException("Failed to find payload entry in the given package..");
        }

        return PayloadSpec.newBuilder()
                .url("file://" + packageFile.getAbsolutePath())
                .offset(payloadOffset)
                .size(payloadSize)
                .properties(properties)
                .build();
    }

    // Creates a `PayloadSpec` for Streaming Update

    public PayloadSpec forStreaming(String updateUrl,
                                    long offset,
                                    long size,
                                    File propertiesFile) throws IOException {

        return PayloadSpec.newBuilder()
                .url(updateUrl)
                .offset(offset)
                .size(size)
                .properties(Files.readAllLines(propertiesFile.toPath()))
                .build();
    }

    // This function converts `PayloadSpec` to a string.

    public String specToString(PayloadSpec payloadSpec) {
        return "<PayloadSpec url=" + payloadSpec.getUrl()
                + ", offset=" + payloadSpec.getOffset()
                + ", size=" + payloadSpec.getSize()
                + ", properties=" + Arrays.toString(
                payloadSpec.getProperties().toArray(new String[0]))
                + ">";
    }
}





























