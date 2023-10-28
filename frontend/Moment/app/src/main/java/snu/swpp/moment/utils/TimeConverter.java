package snu.swpp.moment.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
        SimpleDateFormat formatter = new SimpleDateFormat(formatString, Locale.getDefault());
        return formatter.format(date);
    }

    public static long[] getOneDayIntervalTimestamps(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date, 3, 0, 0);  // month is 0-based
        Date startDate = calendar.getTime();

        final int MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
        calendar.add(Calendar.MILLISECOND, MILLIS_IN_A_DAY - 1);
        Date endDate = calendar.getTime();
        long start = TimeConverter.convertDateToLong(startDate);
        long end = TimeConverter.convertDateToLong(endDate);
        return new long[]{start, end};
    }
}
