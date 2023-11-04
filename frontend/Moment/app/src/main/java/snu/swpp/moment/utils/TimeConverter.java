package snu.swpp.moment.utils;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class TimeConverter {

    public static Date convertTimestampToDate(Long timestamp) {
        return new Date(timestamp * 1000);
    }

    /* 현지 시간을 기준으로 만들어진 LocalDateTime을 UTC timestamp로 변환 */
    public static long convertLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
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

    public static long[] getOneDayIntervalTimestamps(LocalDateTime now) {
        LocalDateTime start = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(),
            3, 0, 0);
        if (now.getHour() < 3) {
            start = start.minusDays(1);
        }
        LocalDateTime end = start.plusDays(1).minusSeconds(1);
        Log.d("TimeConverter", "start: " + start + ", end: " + end);

        long startTimestamp = convertLocalDateTimeToTimestamp(start);
        long endTimestamp = convertLocalDateTimeToTimestamp(end);
        Log.d("TimeConverter",
            "startTimestamp: " + startTimestamp + ", endTimestamp: " + endTimestamp);
        return new long[]{startTimestamp, endTimestamp};
    }

    public static long[] getOneMonthTimestamps(YearMonth yearMonth) {
        LocalDate startDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
        LocalDate endDate = yearMonth.atEndOfMonth().plusDays(1);

        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.of(3, 0, 0));
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.of(2, 59, 59));

        long startTimestamp = convertLocalDateTimeToTimestamp(startDateTime);
        long endTimestamp = convertLocalDateTimeToTimestamp(endDateTime);

        return new long[]{startTimestamp, endTimestamp};
    }

    public static LocalDate convertDateToLocalDate(Date date) {
        LocalDate result = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        System.out.println("date: " + date + ", result: " + result);
        System.out.println("date: " + date.getHours());
        if (date.getHours() < 3) {
            result = result.minusDays(1);
        }
        return result;
    }
}
