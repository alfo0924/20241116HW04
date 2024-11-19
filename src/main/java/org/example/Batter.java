package org.example;

import java.util.HashMap;
import java.util.Map;

public record Batter(Map<String, Integer> pitchBreakdown, Map<String, Integer> baseHitsBreakdown) {

    // 建構子，確保傳入的 Map 不為 null，並建立新的 Map 物件
    public Batter {
        pitchBreakdown = pitchBreakdown != null ? new HashMap<>(pitchBreakdown) : new HashMap<>();
        baseHitsBreakdown = baseHitsBreakdown != null ? new HashMap<>(baseHitsBreakdown) : new HashMap<>();
    }

    // 計算特定區域的打擊率
    public double getBattingAverage(String zone) {
        if (!pitchBreakdown.containsKey(zone)) {
            return 0.0;
        }
        int pitches = pitchBreakdown.get(zone);
        int hits = baseHitsBreakdown.getOrDefault(zone, 0);
        return pitches > 0 ? (double) hits / pitches : 0.0;
    }

    // 取得所有區域的打擊率
    public Map<String, Double> getAllBattingAverages() {
        Map<String, Double> averages = new HashMap<>();
        for (String zone : pitchBreakdown.keySet()) {
            averages.put(zone, getBattingAverage(zone));
        }
        return averages;
    }

    // 取得最高打擊率的區域
    public String getHighestAverageZone() {
        return getAllBattingAverages().entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("5"); // 預設值
    }

    // 取得最低打擊率的區域
    public String getLowestAverageZone() {
        return getAllBattingAverages().entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("1"); // 預設值
    }

    // 取得好球區最低打擊率的區域
    public String getLowestAverageStrikeZone() {
        return getAllBattingAverages().entrySet()
                .stream()
                .filter(e -> !e.getKey().startsWith("x"))
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("1"); // 預設值
    }

    // 檢查區域是否有效
    public boolean isValidZone(String zone) {
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

    // 取得總投球次數
    public int getTotalPitches() {
        return pitchBreakdown.values().stream().mapToInt(Integer::intValue).sum();
    }

    // 取得總安打數
    public int getTotalHits() {
        return baseHitsBreakdown.values().stream().mapToInt(Integer::intValue).sum();
    }

    // 取得整體打擊率
    public double getOverallBattingAverage() {
        int totalPitches = getTotalPitches();
        return totalPitches > 0 ? (double) getTotalHits() / totalPitches : 0.0;
    }

    // 檢查是否為空數據
    public boolean isEmpty() {
        return pitchBreakdown.isEmpty() || baseHitsBreakdown.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Batter(總投球數: %d, 總安打數: %d, 整體打擊率: %.3f)",
                getTotalPitches(), getTotalHits(), getOverallBattingAverage());
    }
}