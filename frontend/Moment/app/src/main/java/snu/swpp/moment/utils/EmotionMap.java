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

    private static final HashMap<Integer, String> reverseMap = new HashMap<>() { {
        put(0, "excited1");
        put(1, "excited2");
        put(2, "happy1");
        put(3, "happy2");
        put(4, "normal1");
        put(5, "normal2");
        put(6, "sad1");
        put(7, "sad2");
        put(8, "angry1");
        put(9, "angry2");
        put(10, "invalid");
    }};

    public static int getEmotionInt(String emotion) {
        Integer emotionInt = emotionEnumMap.getOrDefault(emotion, 10);
        return Objects.requireNonNullElse(emotionInt, 10);
    }

    public static String getEmotion(int code) {
        String emotionStr = reverseMap.getOrDefault(code, "invalid");
        return Objects.requireNonNullElse(emotionStr, "invalid");
    }
}