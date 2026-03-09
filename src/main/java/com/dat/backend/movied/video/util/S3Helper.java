package com.dat.backend.movied.video.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class S3Helper {

    public String createKey(String filename, String category) {
        String extension = filename.contains(".") ? filename.substring(filename.lastIndexOf(".")) : filename;
        return category + "/" + UUID.randomUUID() + extension;
    }

    public String finalUrl(String key, String bucket) {
        return "https://" + bucket + ".sgp1.digitaloceanspaces.com/" + key;
    }
}
