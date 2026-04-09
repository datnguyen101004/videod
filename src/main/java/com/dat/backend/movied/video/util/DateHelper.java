package com.dat.backend.movied.video.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;

@UtilityClass
public class DateHelper {

    public Instant toInstant(String date) {
        return Instant.parse(date);
    }
}
