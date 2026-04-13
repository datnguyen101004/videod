package com.dat.backend.movied.common.helper;

import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class RedisHelper {

    public String parseHostNameFromUrl(String url) {
        // ex: redis://localhost:6379
        String[] urlParts = url.split(":");
        String host = urlParts[1];
        host = host.substring(2);
        return host;
    }

    public String parsePortFromUrl(String url) {
        // ex: redis://localhost:6379
        String[] urlParts = url.split(":");
        return urlParts[2];
    }
}
