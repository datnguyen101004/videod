package com.dat.backend.movied.video.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Base64Helper {

    public String encodeCursor(Map<String, Object> key) {
        String base64 = key.get("createdAt").toString() + "_" + key.get("id").toString();
        return Base64.getEncoder().encodeToString(base64.getBytes());
    }

    public Map<String, Object> decodeCursor(String base64) {
        String decoded = new String(Base64.getDecoder().decode(base64));
        String[] parts = decoded.split("_");
        // Parse createdAt từ string sang instant
        Instant createdAt = Instant.parse(parts[0]);
        Map<String, Object> key = new HashMap<String, Object>();
        key.put("createdAt", createdAt);
        key.put("id", parts[1]);
        return key;
    }
}
