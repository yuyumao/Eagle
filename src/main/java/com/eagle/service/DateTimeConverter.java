package com.eagle.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeConverter {
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));

    public static String instantToIsoString(Instant instant) {
        return ISO_FORMATTER.format(instant);
    }

    public static Instant isoStringToInstant(String isoString) {
        return Instant.from(ISO_FORMATTER.parse(isoString));
    }
}