package snu.swpp.moment.utils;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class EmotionMapTest {

    private static final HashMap<String, Integer> emotionEnumMap = new HashMap<>() {{
        put("excited1", 0);
        put("excited2", 1);
        put("happy1", 2);
        put("happy2", 3);
        put("normal1", 4);
        put("normal2", 5);
        put("sad1", 6);
        put("sad2", 7);
        put("angry1", 8);
        put("angry2", 9);
        put("invalid", 10);
    }};

    @Test
    public void getEmotionInt() {
        for (Map.Entry<String, Integer> entry : emotionEnumMap.entrySet()) {
            int answer = entry.getValue();
            assertEquals(answer, EmotionMap.getEmotionInt(entry.getKey()));
        }
    }

    @Test
    public void getEmotionString() {
        for (Map.Entry<String, Integer> entry : emotionEnumMap.entrySet()) {
            String answer = entry.getKey();
            assertEquals(answer, EmotionMap.getEmotionString(entry.getValue()));
        }
    }
}