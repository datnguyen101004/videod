package com.dat.backend.movied.video.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
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

    public File convertMultiPartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
