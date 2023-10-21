package snu.swpp.moment.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeConverter {
    public static long convertDateToLong(Date date) {
        return date.getTime()/1000;
    }
    public static Date convertLongToDate(Long timestamp) {
        return new Date(timestamp * 1000);
    }
    public static String getPageDate(int position) {
        LocalDate today = LocalDate.now();
        int currentHour = LocalTime.now().getHour();
        if (currentHour < 3) {
            today = today.minusDays(1);
        }
        LocalDate pageDate = today.minusDays(position);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");
        return pageDate.format(formatter);
    }
}
