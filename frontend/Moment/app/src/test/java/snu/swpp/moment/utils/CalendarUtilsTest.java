package snu.swpp.moment.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import net.bytebuddy.asm.Advice.Local;
import org.junit.Before;
import org.junit.Test;
import snu.swpp.moment.ui.main_monthview.CalendarDayState;
import snu.swpp.moment.utils.CalendarUtilsKt;
import snu.swpp.moment.data.model.StoryModel;

public class CalendarUtilsTest {

    @Test
    public void fillEmptyStory() {
        List<CalendarDayState> storyList = new ArrayList<>();
        storyList.add(new CalendarDayState(
            LocalDate.of(2023, 11, 3),
            "title1",
            "content1",
            EmotionMap.getEmotionInt("excited1"),
            new ArrayList<>(),
            2,
            false
        ));
        storyList.add(new CalendarDayState(
            LocalDate.of(2023, 11, 18),
            "title2",
            "content2",
            EmotionMap.getEmotionInt("excited2"),
            new ArrayList<>(),
            4,
            false
        ));
        YearMonth yearMonth = YearMonth.of(2023, 11);
        List<CalendarDayState> result = CalendarUtilsKt.fillEmptyStory(storyList, yearMonth);
        assertEquals(31, result.size());
        for (int i = 0; i < 31; i++) {
            if (i == 2 || i == 17) {
                assertFalse(result.get(i).isEmotionInvalid());
            } else {
                assertTrue(result.get(i).isEmotionInvalid());
            }
        }
    }

}
