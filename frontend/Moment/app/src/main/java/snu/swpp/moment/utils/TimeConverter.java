package snu.swpp.moment.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeConverter {

    public static long convertDateToLong(Date date) {
        return date.getTime() / 1000;
    }

    public static Date convertLongToDate(Long timestamp) {
        return new Date(timestamp * 1000);
    }

    public static LocalDate updateDateFromThree(LocalDate date, int hour) {
        if (hour < 3) {
            date = date.minusDays(1);
        }
        return date;
    }

    public static LocalDate getToday() {
        LocalDate today = LocalDate.now();
        int hour = LocalTime.now().getHour();
        return updateDateFromThree(today, hour);
    }

    public static String formatLocalDate(LocalDate date, String formatString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString);
        return date.format(formatter);
    }

    public static String formatDate(Date date, String formatString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString);
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()
            .format(formatter);
    }
}
