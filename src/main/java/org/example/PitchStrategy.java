package org.example;

import java.util.HashMap;
import java.util.Map;

public class PitchStrategy {
    public static PitchResult pitch(Batter batter, boolean ballIsOK) {
        Map<String, Double> battingAverages = calculateBattingAverages(batter);

        // 找出打擊率最高的區域
        String bestZone = findHighestBattingAverageZone(battingAverages);

        // 根據是否可以投壞球來決定最終落點
        String worstZone;
        if (ballIsOK) {
            worstZone = findLowestBattingAverageZone(battingAverages);
        } else {
            worstZone = findLowestBattingAverageInStrikeZone(battingAverages);
        }

        return new PitchResult(bestZone, worstZone);
    }

    private static Map<String, Double> calculateBattingAverages(Batter batter) {
        Map<String, Double> battingAverages = new HashMap<>();
        Map<String, Integer> pitches = batter.pitchBreakdown();
        Map<String, Integer> hits = batter.baseHitsBreakdown();

        for (Map.Entry<String, Integer> entry : pitches.entrySet()) {
            String zone = entry.getKey();
            int totalPitches = entry.getValue();
            int totalHits = hits.getOrDefault(zone, 0);
            double average = totalPitches > 0 ? (double) totalHits / totalPitches : 0.0;
            battingAverages.put(zone, average);
        }

        return battingAverages;
    }

    private static String findHighestBattingAverageZone(Map<String, Double> battingAverages) {
        return battingAverages.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("5"); // 預設值
    }

    private static String findLowestBattingAverageZone(Map<String, Double> battingAverages) {
        return battingAverages.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("1"); // 預設值
    }

    private static String findLowestBattingAverageInStrikeZone(Map<String, Double> battingAverages) {
        return battingAverages.entrySet()
                .stream()
                .filter(e -> !e.getKey().startsWith("x")) // 只考慮好球區
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("1"); // 預設值
    }
}
