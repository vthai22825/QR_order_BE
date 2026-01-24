package com.example.qr_order.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private static final ZoneId VN_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter VN_DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private DateTimeUtils(){}

    public static String toVnDateTime(Instant instant){
        if (instant == null){
            return null;
        }
        return instant
                .atZone(VN_ZONE)
                .format(VN_DATE_TIME);
    }

}
