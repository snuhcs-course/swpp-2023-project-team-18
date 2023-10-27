package snu.swpp.moment.utils;

import java.util.HashMap;
import java.util.Objects;

public class EmotionMap {

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

    public static int getEmotionInt(String emotion) {
        Integer emotionInt = emotionEnumMap.getOrDefault(emotion, 10);
        return Objects.requireNonNullElse(emotionInt, 10);
    }
}