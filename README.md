
## 主要類別定義

```java
// 打者資料類別
class Batter {
    private Map<String, Integer> pitchBreakdown;
    private Map<String, Integer> baseHitsBreakdown;
    
    public Batter(Map<String, Integer> pitchBreakdown, Map<String, Integer> baseHitsBreakdown) {
        this.pitchBreakdown = pitchBreakdown;
        this.baseHitsBreakdown = baseHitsBreakdown;
    }
    
    public Map<String, Integer> getPitchBreakdown() {
        return pitchBreakdown;
    }
    
    public Map<String, Integer> getBaseHitsBreakdown() {
        return baseHitsBreakdown;
    }
}

// 投球建議類別
class PitchSuggestion {
    private String startZone;
    private String endZone;
    
    public PitchSuggestion(String startZone, String endZone) {
        this.startZone = startZone;
        this.endZone = endZone;
    }
    
    @Override
    public String toString() {
        return "(" + startZone + ", " + endZone + ")";
    }
}
```

## 投球策略實作

```java
public class PitchStrategy {
    
    public static PitchSuggestion pitch(Batter batter, boolean ballIsOK) {
        // 計算所有區域打擊率
        Map<String, Double> battingAverages = calculateBattingAverages(batter);
        
        // 找出打擊率最高區域
        String bestZone = findHighestBattingAverageZone(battingAverages);
        
        // 找出最終落點
        String worstZone;
        if (ballIsOK) {
            worstZone = findLowestBattingAverageZone(battingAverages);
        } else {
            worstZone = findLowestBattingAverageInStrikeZone(battingAverages);
        }
        
        return new PitchSuggestion(bestZone, worstZone);
    }
    
    private static Map<String, Double> calculateBattingAverages(Batter batter) {
        Map<String, Double> battingAverages = new HashMap<>();
        Map<String, Integer> pitches = batter.getPitchBreakdown();
        Map<String, Integer> hits = batter.getBaseHitsBreakdown();
        
        for (String zone : pitches.keySet()) {
            int totalPitches = pitches.get(zone);
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
            .orElse("1");
    }
    
    private static String findLowestBattingAverageZone(Map<String, Double> battingAverages) {
        return battingAverages.entrySet()
            .stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("1");
    }
    
    private static String findLowestBattingAverageInStrikeZone(Map<String, Double> battingAverages) {
        return battingAverages.entrySet()
            .stream()
            .filter(e -> !e.getKey().startsWith("x")) // 只考慮好球區
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("1");
    }
}
```

## 使用範例

```java
public class Main {
    public static void main(String[] args) {
        // 建立測試資料
        Map<String, Integer> pitchBreakdown = new HashMap<>();
        Map<String, Integer> baseHitsBreakdown = new HashMap<>();
        
        // 設置測試數據
        pitchBreakdown.put("1", 100);
        pitchBreakdown.put("5", 100);
        pitchBreakdown.put("x1", 50);
        
        baseHitsBreakdown.put("1", 20);  // 打擊率 0.2
        baseHitsBreakdown.put("5", 40);  // 打擊率 0.4
        baseHitsBreakdown.put("x1", 5);  // 打擊率 0.1
        
        Batter batter = new Batter(pitchBreakdown, baseHitsBreakdown);
        
        // 測試不同情況
        PitchSuggestion suggestion1 = PitchStrategy.pitch(batter, false);
        System.out.println("Cannot pitch ball: " + suggestion1);  // 預期輸出: (5, 1)
        
        PitchSuggestion suggestion2 = PitchStrategy.pitch(batter, true);
        System.out.println("Can pitch ball: " + suggestion2);     // 預期輸出: (5, x1)
    }
}
```

## 程式特點

1. **物件導向設計**
- 使用Batter類別封裝打者資料
- 使用PitchSuggestion類別表示投球建議

2. **資料處理**
- 使用Map儲存打擊數據
- 使用Stream API進行資料過濾和查找

3. **彈性設計**
- 可以輕易擴展新的投球策略
- 支援不同的打擊區域配置

4. **錯誤處理**
- 使用Optional避免空指針異常
- 提供預設值處理異常情況

這個Java實作提供了完整的投球策略分析系統，可以根據打者數據和比賽情況給出最佳的投球建議。
