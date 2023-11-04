package snu.swpp.moment.utils;

import static org.junit.Assert.*;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;

public class TimeConverterTest {

    private int hourDiff; // 시차

    @Before
    public void setUp() {
        ZoneId localZoneId = ZoneId.systemDefault();
        ZoneId gmtZoneId = ZoneId.of("GMT");

        ZonedDateTime localZonedDateTime = LocalDateTime.now().atZone(localZoneId);
        ZonedDateTime gmtZonedDateTime = localZonedDateTime.withZoneSameInstant(gmtZoneId);

        // Calculate the hour difference
        hourDiff = localZonedDateTime.getHour() - gmtZonedDateTime.getHour();
    }

    @Test
    public void convertTimestampToDate() {
        long timestamp = 1698999459L; // GMT 2023.11.3 8:17:39
        Date convertedDate = TimeConverter.convertTimestampToDate(timestamp);

        // 로컬 시차 반영
        Date answer = new Date(2023 - 1900, 11 - 1, 3, 8 + hourDiff, 17, 39);
        assertEquals(answer.getTime(), convertedDate.getTime());
    }

    @Test
    public void convertLocalDateTimeToTimestamp() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 11, 3, 8 + hourDiff, 17, 39);
        long convertedTimestamp = TimeConverter.convertLocalDateTimeToTimestamp(dateTime);
        assertEquals(1698999459, convertedTimestamp);
    }

    @Test
    public void updateDateFromThree_before3() {
        LocalDate date = LocalDate.of(2023, 11, 3);
        int hour = 2;
        LocalDate convertedDate = TimeConverter.updateDateFromThree(date, hour);
        LocalDate answer = LocalDate.of(2023, 11, 2);
        assertTrue(convertedDate.isEqual(answer));
    }

    @Test
    public void updateDateFromThree_after3() {
        LocalDate date = LocalDate.of(2023, 11, 3);
        int hour = 12;
        LocalDate convertedDate = TimeConverter.updateDateFromThree(date, hour);
        LocalDate answer = LocalDate.of(2023, 11, 3);
        assertTrue(convertedDate.isEqual(answer));
    }

    @Test
    public void formatLocalDate() {
        LocalDate date = LocalDate.of(2023, 11, 3);
        String formatString = "yyyy. MM. dd.";
        String formattedDate = TimeConverter.formatLocalDate(date, formatString);
        assertEquals("2023. 11. 03.", formattedDate);
    }

    @Test
    public void formatDate() {
        Date date = new Date(2023 - 1900, 11 - 1, 3, 8, 17, 39);
        String formatString = "yyyy.MM.dd. HH:mm";
        String formattedDate = TimeConverter.formatDate(date, formatString);
        assertEquals("2023.11.03. 08:17", formattedDate);
    }

    @Test
    public void getOneDayIntervalTimestamps_before3() {
        LocalDateTime date = LocalDateTime.of(2023, 11, 3, 1, 17, 39);
        long[] timestamps = TimeConverter.getOneDayIntervalTimestamps(date);

        LocalDateTime startDate = LocalDateTime.of(2023, 11, 2, 3, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 11, 3, 2, 59, 59);

        long answer1 = TimeConverter.convertLocalDateTimeToTimestamp(startDate);
        long answer2 = TimeConverter.convertLocalDateTimeToTimestamp(endDate);

        assertEquals(answer1, timestamps[0]);
        assertEquals(answer2, timestamps[1]);
    }

    @Test
    public void getOneDayIntervalTimestamps_after3() {
        LocalDateTime date = LocalDateTime.of(2023, 11, 3, 8, 17, 39);
        long[] timestamps = TimeConverter.getOneDayIntervalTimestamps(date);

        LocalDateTime startDate = LocalDateTime.of(2023, 11, 3, 3, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 11, 4, 2, 59, 59);

        long answer1 = TimeConverter.convertLocalDateTimeToTimestamp(startDate);
        long answer2 = TimeConverter.convertLocalDateTimeToTimestamp(endDate);

        assertEquals(answer1, timestamps[0]);
        assertEquals(answer2, timestamps[1]);
    }
}