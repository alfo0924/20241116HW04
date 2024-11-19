package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PitchStrategy {

    public static Map<String, Integer> loadDataFromCsv(String filename) {
        Map<String, Integer> data = new HashMap<>();
        try (InputStream is = PitchStrategy.class.getClassLoader().getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            // 跳過標題行
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                data.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Error loading data from " + filename, e);
        }
        return data;
    }

    public static PitchResult pitch(Batter batter, boolean ballIsOK) {
        Map<String, Double> battingAverages = calculateBattingAverages(batter);

        // 過濾無效區域
        battingAverages.keySet().removeIf(zone -> !isValidZone(zone));

        // 如果沒有有效數據，返回預設值
        if (battingAverages.isEmpty()) {
            return new PitchResult("5", ballIsOK ? "x3" : "1");
        }

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
            if (!isValidZone(zone)) {
                continue;
            }
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
                .filter(e -> isValidZone(e.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("5"); // 預設值為5號位置
    }

    private static String findLowestBattingAverageZone(Map<String, Double> battingAverages) {
        return battingAverages.entrySet()
                .stream()
                .filter(e -> isValidZone(e.getKey()))
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("x3"); // 預設值為x3
    }

    private static String findLowestBattingAverageInStrikeZone(Map<String, Double> battingAverages) {
        return battingAverages.entrySet()
                .stream()
                .filter(e -> isValidZone(e.getKey()) && !e.getKey().startsWith("x"))
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("1"); // 預設值為1號位置
    }

    public static boolean isValidZone(String zone) {
        if (zone == null || zone.isEmpty()) {
            return false;
        }
        // 檢查壞球區域 (x1-x4)
        if (zone.startsWith("x")) {
            return zone.matches("x[1-4]");
        }
        // 檢查好球區域 (1-9)
        return zone.matches("[1-9]");
    }

    public static boolean isStrikeZone(String zone) {
        return isValidZone(zone) && !zone.startsWith("x");
    }

    // 根據球種判斷適合的區域組合
    public static PitchResult getPitchByType(String pitchType, boolean ballIsOK) {
        return switch (pitchType) {
            case "四縫線快速球" -> new PitchResult("5", "1");
            case "卡特球" -> new PitchResult("5", ballIsOK ? "x3" : "1");
            case "伸卡球" -> new PitchResult("5", ballIsOK ? "x2" : "2");
            case "二縫線快速球" -> new PitchResult("5", "3");
            case "快指叉球" -> new PitchResult("5", ballIsOK ? "x1" : "1");
            case "指叉球" -> new PitchResult("5", ballIsOK ? "x4" : "7");
            case "曲球" -> new PitchResult("5", "2");
            case "滑球" -> new PitchResult("5", ballIsOK ? "x2" : "3");
            case "變速球" -> new PitchResult("5", "7");
            default -> new PitchResult("5", ballIsOK ? "x3" : "1");
        };
    }

    // 讀取球種資料
    public static List<PitchType> loadPitchTypes(String filename) {
        List<PitchType> pitchTypes = new ArrayList<>();
        try (InputStream is = PitchStrategy.class.getClassLoader().getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            // 跳過標題行
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                PitchType type = new PitchType(
                        parts[0],
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4])
                );
                pitchTypes.add(type);
            }
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Error loading pitch types from " + filename, e);
        }
        return pitchTypes;
    }
}