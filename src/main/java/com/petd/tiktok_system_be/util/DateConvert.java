package com.petd.tiktok_system_be.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateConvert {

    public static String toLocalDateTime(Long timestamp) {
        ZonedDateTime vnTime = Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        return vnTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
