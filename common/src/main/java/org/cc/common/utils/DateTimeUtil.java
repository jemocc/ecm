package org.cc.common.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    public final static String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
    public final static String DEFAULT_DATE = "yyyyMMdd";
    public final static String DEFAULT_TIME = "HH:mm:ss";
    public final static DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME);

    public static String getCurrentDate() {
        LocalDate now = LocalDate.now();
        return now.format(DateTimeFormatter.ofPattern(DEFAULT_DATE));
    }

    /**
     * 秒转时间
     * @param ts    总秒
     */
    public static LocalTime parseTime(int ts) {
        int hours = ts / 3600;
        ts = ts - hours * 3600;
        int min = ts / 60;
        int sec = ts % 60;
        return LocalTime.of(hours, min, sec);
    }
}
