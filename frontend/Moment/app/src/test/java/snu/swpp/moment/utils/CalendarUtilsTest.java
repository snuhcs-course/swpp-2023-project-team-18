package snu.swpp.moment.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import snu.swpp.moment.utils.CalendarUtilsKt;
import snu.swpp.moment.data.model.StoryModel;

public class CalendarUtilsTest {

    @Test
    public void fillEmptyStory() {
        List<StoryModel> storyList = new ArrayList<>();
        storyList.add(new StoryModel(
            1,
            "excited1",
            3,
            "story1",
            "story1",
            new ArrayList<>(),
            1698999459L,
            false,
            false
        )); // GMT 2023.11.3 8:17:39
        storyList.add(new StoryModel(
            2,
            "excited2",
            3,
            "story2",
            "story2",
            new ArrayList<>(),
            1699083804L,
            false,
            false
        )); // GMT 2023.11.4 7:43:24
        YearMonth yearMonth = YearMonth.of(2023, 11);
        List<StoryModel> result = CalendarUtilsKt.fillEmptyStory(storyList, yearMonth);
        assertEquals(31, result.size());
        for (int i = 0; i < 31; i++) {
            if (i == 2 || i == 3) {
                assertFalse(result.get(i).isEmpty());
            } else {
                assertTrue(result.get(i).isEmpty());
            }
        }
    }

}
