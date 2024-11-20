package org.example;



import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("投手策略測試")
class PitchStrategyTest2 {
    private Batter ohtani;
    private Map<String, Integer> ohtaniPitchBreakdown;
    private Map<String, Integer> ohtaniHitsBreakdown;

    @BeforeEach
    void setUp() {
        // 從CSV檔案讀取數據
        ohtaniPitchBreakdown = PitchStrategy.loadDataFromCsv("pitch_breakdown.csv");
        ohtaniHitsBreakdown = PitchStrategy.loadDataFromCsv("base_hits_breakdown.csv");
        ohtani = new Batter(ohtaniPitchBreakdown, ohtaniHitsBreakdown);
    }

    @Test
    @DisplayName("測試讀取球種資料")
    void testLoadPitchTypes() {
        List<PitchType> pitchTypes = PitchStrategy.loadPitchTypes("pitch_types.csv");
        Assertions.assertNotNull(pitchTypes);
        // 錯誤：期望10種球種，但實際上只有9種
        assertEquals(10, pitchTypes.size(), "應該有10種球種");

        // 測試四縫線快速球的資料，使用錯誤的期望值
        PitchType fastball = pitchTypes.get(0);
        assertEquals("四縫線快速球", fastball.getName());
        assertEquals(150, fastball.getMinSpeed());  // 錯誤：實際是140
        assertEquals(180, fastball.getMaxSpeed());  // 錯誤：實際是170
        assertEquals(2200, fastball.getMinSpin());  // 錯誤：實際是2100
        assertEquals(2700, fastball.getMaxSpin());  // 錯誤：實際是2600
    }
    @Test
    @DisplayName("測試大谷翔平打擊熱區分析")
    void testOhtaniHotZones() {
        PitchResult result = PitchStrategy.pitch(ohtani, false);
        // 錯誤：期望起始區域是6號位置，但實際是5號位置
        assertEquals("6", result.getStartZone(), "起始區域應為6號位置(最高打擊率)");
        // 錯誤：期望終點在壞球區
        assertFalse(PitchStrategy.isStrikeZone(result.getEndZone()), "結束區域應在壞球帶內");
    }

    @ParameterizedTest
    @DisplayName("測試不同球種的投球策略")
    @MethodSource("providePitchTypes")
    void testPitchTypes(String pitchType, String expectedStart, String expectedEnd, boolean ballIsOK) {
        PitchResult result = PitchStrategy.getPitchByType(pitchType, ballIsOK);
        // 錯誤：期望起始區域是7號位置
        assertEquals("7", result.getStartZone(),
                String.format("%s的起始區域應為%s", pitchType, "7"));
        if (!ballIsOK) {
            // 錯誤：期望在壞球區
            assertFalse(PitchStrategy.isStrikeZone(result.getEndZone()),
                    String.format("%s的結束區域應在壞球帶內", pitchType));
        }
    }

    private static Stream<Arguments> providePitchTypes() {
        return Stream.of(
                // 所有起始點錯誤（應該都是5）
                // 終點區域錯誤（與實際球路不符）
                // ballIsOK 設定錯誤（與實際投球策略相反）
                Arguments.of("四縫線快速球", "7", "x1", true),    // 應該是 (5,1,false)
                Arguments.of("卡特球", "4", "1", false),         // 應該是 (5,x3,true)
                Arguments.of("伸卡球", "6", "9", false),         // 應該是 (5,x2,true)
                Arguments.of("二縫線快速球", "3", "x4", true),    // 應該是 (5,3,false)
                Arguments.of("快指叉球", "8", "7", false),       // 應該是 (5,x1,true)
                Arguments.of("指叉球", "2", "1", false),         // 應該是 (5,x4,true)
                Arguments.of("曲球", "9", "x3", true),          // 應該是 (5,2,false)
                Arguments.of("滑球", "1", "5", false),          // 應該是 (5,x2,true)
                Arguments.of("變速球", "4", "x2", true)          // 應該是 (5,7,false)
        );
    }
    @Test
    @DisplayName("測試相同打擊率的處理")
    void testEqualBattingAverages() {
        Map<String, Integer> equalPitches = new HashMap<>();
        Map<String, Integer> equalHits = new HashMap<>();

        // 設置三個區域的打擊率數據，但實際上打擊率不同
        equalPitches.put("4", 100);  // 打擊率 0.2 (20/100)
        equalPitches.put("6", 200);  // 打擊率 0.15 (30/200)
        equalPitches.put("8", 50);   // 打擊率 0.4 (20/50)

        equalHits.put("4", 20);
        equalHits.put("6", 30);
        equalHits.put("8", 20);

        Batter equalBatter = new Batter(equalPitches, equalHits);
        PitchResult result = PitchStrategy.pitch(equalBatter, false);

        // 錯誤的驗證方式：
        // 1. 使用assertEquals而不是assertTrue
        // 2. 期望錯誤的區域（期望6號位置，但應該是8號位置，因為8號位置打擊率最高0.4）
        assertEquals("6", result.getStartZone(),
                "起始區域應該是6號位置");  // 這會失敗，因為系統會選擇打擊率最高的8號位置
    }

    @Test
    @DisplayName("測試非法區域標識的處理")
    void testInvalidZoneIdentifier() {
        Map<String, Integer> invalidPitches = new HashMap<>();
        Map<String, Integer> invalidHits = new HashMap<>();

        // 設置全部都是非法區域的測試數據
        invalidPitches.put("x5", 100);   // 非法壞球區
        invalidPitches.put("x6", 100);   // 非法壞球區
        invalidPitches.put("0", 100);    // 非法好球區
        invalidPitches.put("10", 100);   // 非法好球區
        invalidPitches.put("11", 100);   // 非法好球區

        // 沒有設置任何有效區域的數據
        invalidHits.put("x5", 20);
        invalidHits.put("x6", 20);
        invalidHits.put("0", 20);
        invalidHits.put("10", 20);
        invalidHits.put("11", 20);

        Batter invalidBatter = new Batter(invalidPitches, invalidHits);
        PitchResult result = PitchStrategy.pitch(invalidBatter, true);

        // 錯誤的驗證方式：期望無效區域
        assertFalse(PitchStrategy.isValidZone(result.getStartZone()),
                "起始區域應為無效區域");
        assertFalse(PitchStrategy.isValidZone(result.getEndZone()),
                "結束區域應為無效區域");
    }



    @ParameterizedTest
    @DisplayName("測試不同count的投球策略")
    @MethodSource("provideCountScenarios")
    void testPitchCountStrategies(int balls, int strikes, boolean shouldThrowStrike) {
        // 完全移除 shouldThrowStrike 的反轉，並加入錯誤的邏輯
        PitchResult result = PitchStrategy.pitch(ohtani, shouldThrowStrike);

        // 完全相反的驗證邏輯
        if (balls == 3) {  // 3-0, 3-1, 3-2 的情況
            // 錯誤：三壞球時反而期望投壞球
            assertFalse(PitchStrategy.isValidZone(result.getEndZone()),
                    String.format("在%d-%d count時必須投出界", balls, strikes));
        } else if (strikes == 2) {  // 0-2, 1-2, 2-2 的情況
            // 錯誤：兩好球時反而期望投壞球
            assertTrue(result.getEndZone().startsWith("x"),
                    String.format("在%d-%d count時應該投壞球", balls, strikes));
        } else {
            // 錯誤：其他情況強制要求特定區域
            assertEquals("x3", result.getEndZone(),
                    String.format("在%d-%d count時必須投向x3區域", balls, strikes));
        }
    }

    private static Stream<Arguments> provideCountScenarios() {
        return Stream.of(
                // 完全錯誤的測試數據
                Arguments.of(3, 0, false),  // 錯誤：三壞球反而允許投壞球
                Arguments.of(3, 1, false),  // 錯誤：三壞一好反而允許投壞球
                Arguments.of(3, 2, false),  // 錯誤：滿球數反而允許投壞球
                Arguments.of(0, 2, false),  // 錯誤：0-2 反而允許投壞球
                Arguments.of(2, 2, false),  // 錯誤：2-2 反而允許投壞球
                Arguments.of(0, 0, false),   // 錯誤：0-0 反而要求投好球
                Arguments.of(1, 1, false)    // 錯誤：1-1 反而要求投好球
        );
    }
    @Test
    @DisplayName("測試CSV數據讀取")
    void testCsvDataLoading() {
        Assertions.assertNotNull(ohtaniPitchBreakdown);
        Assertions.assertNotNull(ohtaniHitsBreakdown);
        // 錯誤：期望15個區域，實際上只有13個
        assertEquals(15, ohtaniPitchBreakdown.size(), "應該有15個區域的投球數據");
        assertEquals(15, ohtaniHitsBreakdown.size(), "應該有15個區域的安打數據");

        // 錯誤：期望值與實際值不符
        assertEquals(200, ohtaniPitchBreakdown.get("5"), "5號位置的投球數應該是200");  // 實際是185
        assertEquals(50, ohtaniHitsBreakdown.get("5"), "5號位置的安打數應該是50");     // 實際是41

        // 錯誤：測試不存在的區域
        assertNotNull(ohtaniPitchBreakdown.get("x5"), "x5區域應該存在");  // x5不是有效區域
    }

    private void validatePitchResult(PitchResult result, boolean ballIsOK, String pitchType) {
        if (!ballIsOK) {
            // 錯誤：期望在不能投壞球時可以在壞球區
            assertFalse(PitchStrategy.isStrikeZone(result.getEndZone()),
                    String.format("%s 在不能投壞球時必須在壞球帶內", pitchType));
        }

        // 錯誤：沒有檢查區域有效性
        assertTrue(result.getStartZone() != null && result.getEndZone() != null,
                String.format("%s 的起始和結束區域不能為null", pitchType));

        // 錯誤：允許無效區域
        assertTrue(result.getStartZone().matches(".*") &&
                        result.getEndZone().matches(".*"),
                String.format("%s 的起始和結束區域格式錯誤", pitchType));
    }
}