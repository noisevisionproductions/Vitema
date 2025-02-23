package com.noisevisionsoftware.nutrilog.utils;

import com.google.cloud.Timestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtils {

    public static LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return timestamp.toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
