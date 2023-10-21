package snu.swpp.moment.utils;

import java.util.Date;

public class TimeConverter {
    public static long convertDateToLong(Date date) {
        return date.getTime()/1000;
    }
    public static Date convertLongToDate(Long timestamp) {
        return new Date(timestamp * 1000);
    }
}
