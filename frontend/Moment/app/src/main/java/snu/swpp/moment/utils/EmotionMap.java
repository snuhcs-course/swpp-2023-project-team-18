package snu.swpp.moment.utils;

import java.util.HashMap;

public class EmotionMap {
    private static HashMap<String, Integer> emotionEnumMap = new HashMap<String, Integer>();


    public static int getEmotionInt(String emotion){
        initialize();
        return emotionEnumMap.getOrDefault(emotion, 10);
    }

    private static void initialize() {
        emotionEnumMap.put("excited1", 0);
        emotionEnumMap.put("excited2", 1);
        emotionEnumMap.put("happy1", 2);
        emotionEnumMap.put("happy2", 3);
        emotionEnumMap.put("normal1", 4);
        emotionEnumMap.put("normal2", 5);
        emotionEnumMap.put("sad1", 6);
        emotionEnumMap.put("sad2", 7);
        emotionEnumMap.put("angry1", 8);
        emotionEnumMap.put("angry2", 9);
        emotionEnumMap.put("null", 10);
    }
}