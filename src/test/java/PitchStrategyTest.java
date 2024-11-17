package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("投手策略測試")
class PitchStrategyTest {
    private Map<String, Integer> ohtaniPitchBreakdown;
    private Map<String, Integer> ohtaniHitsBreakdown;
    private Batter ohtani;

    @BeforeEach
    void setUp() {
        // 初始化大谷翔平的打擊數據
        ohtaniPitchBreakdown = new HashMap<>();
        ohtaniHitsBreakdown = new HashMap<>();

        // Pitch Breakdown數據
        ohtaniPitchBreakdown.put("1", 131);
        ohtaniPitchBreakdown.put("2", 158);
        ohtaniPitchBreakdown.put("3", 107);
        ohtaniPitchBreakdown.put("4", 189);
        ohtaniPitchBreakdown.put("5", 185);
        ohtaniPitchBreakdown.put("6", 122);
        ohtaniPitchBreakdown.put("7", 176);
        ohtaniPitchBreakdown.put("8", 183);
        ohtaniPitchBreakdown.put("9", 112);
        ohtaniPitchBreakdown.put("x1", 297);
        ohtaniPitchBreakdown.put("x2", 248);
        ohtaniPitchBreakdown.put("x3", 549);
        ohtaniPitchBreakdown.put("x4", 381);

        // Base Hits Breakdown數據
        ohtaniHitsBreakdown.put("1", 9);
        ohtaniHitsBreakdown.put("2", 15);
        ohtaniHitsBreakdown.put("3", 11);
        ohtaniHitsBreakdown.put("4", 20);
        ohtaniHitsBreakdown.put("5", 41);
        ohtaniHitsBreakdown.put("6", 15);
        ohtaniHitsBreakdown.put("7", 20);
        ohtaniHitsBreakdown.put("8", 21);
        ohtaniHitsBreakdown.put("9", 12);
        ohtaniHitsBreakdown.put("x1", 8);
        ohtaniHitsBreakdown.put("x2", 10);
        ohtaniHitsBreakdown.put("x3", 5);
        ohtaniHitsBreakdown.put("x4", 10);

        ohtani = new Batter(ohtaniPitchBreakdown, ohtaniHitsBreakdown);
    }

    @Test
    @DisplayName("測試大谷翔平打擊熱區分析")
    void testOhtaniHotZones() {
        PitchResult result = PitchStrategy.pitch(ohtani, false);
        assertEquals("5", result.getStartZone(), "起始區域應為5號位置(最高打擊率)");
        assertTrue(isStrikeZone(result.getEndZone()), "結束區域應在好球帶內");
    }

    @ParameterizedTest
    @DisplayName("測試不同球種的投球策略")
    @MethodSource("providePitchTypes")
    void testPitchTypes(String pitchType, String expectedStart, String expectedEnd, boolean ballIsOK) {
        PitchResult result = PitchStrategy.pitch(ohtani, ballIsOK);
        assertEquals(expectedStart, result.getStartZone(),
                String.format("%s的起始區域應為%s", pitchType, expectedStart));
        if (!ballIsOK) {
            assertTrue(isStrikeZone(result.getEndZone()),
                    String.format("%s的結束區域應在好球帶內", pitchType));
        }
    }

    private static Stream<Arguments> providePitchTypes() {
        return Stream.of(
                // 測試各種球種的投球策略
                Arguments.of("四縫線快速球", "5", "1", false),
                Arguments.of("滑球", "5", "x3", true),
                Arguments.of("曲球", "5", "x1", true),
                Arguments.of("變速球", "5", "7", false)
        );
    }

    @Test
    @DisplayName("測試好球帶邊界情況")
    void testStrikeZoneBoundary() {
        // 測試好球帶邊界的投球
        Map<String, Integer> boundaryPitches = new HashMap<>();
        Map<String, Integer> boundaryHits = new HashMap<>();

        // 設置邊界數據
        boundaryPitches.put("1", 100);
        boundaryPitches.put("9", 100);
        boundaryHits.put("1", 10);
        boundaryHits.put("9", 40);

        Batter boundaryBatter = new Batter(boundaryPitches, boundaryHits);
        PitchResult result = PitchStrategy.pitch(boundaryBatter, false);
        assertTrue(isStrikeZone(result.getEndZone()), "邊界投球應在好球帶內");
    }

    @Test
    @DisplayName("測試極端打擊率情況")
    void testExtremeBattingAverages() {
        Map<String, Integer> extremePitches = new HashMap<>();
        Map<String, Integer> extremeHits = new HashMap<>();

        // 設置極端情況：某區域100%打擊率
        extremePitches.put("5", 100);
        extremeHits.put("5", 100);
        extremePitches.put("1", 100);
        extremeHits.put("1", 0);

        Batter extremeBatter = new Batter(extremePitches, extremeHits);
        PitchResult result = PitchStrategy.pitch(extremeBatter, false);
        assertEquals("5", result.getStartZone(), "應選擇最高打擊率區域作為起始點");
        assertEquals("1", result.getEndZone(), "應選擇最低打擊率區域作為終點");
    }

    @Test
    @DisplayName("測試空數據處理")
    void testEmptyData() {
        Batter emptyBatter = new Batter(new HashMap<>(), new HashMap<>());
        PitchResult result = PitchStrategy.pitch(emptyBatter, true);
        assertNotNull(result, "空數據應返回預設值");
        assertTrue(isValidZone(result.getStartZone()), "起始區域應有效");
        assertTrue(isValidZone(result.getEndZone()), "結束區域應有效");
    }

    private boolean isStrikeZone(String zone) {
        return !zone.startsWith("x");
    }



    // 在原有的PitchStrategyTest類中添加以下測試方法

    @Test
    @DisplayName("測試相同打擊率的處理")
    void testEqualBattingAverages() {
        Map<String, Integer> equalPitches = new HashMap<>();
        Map<String, Integer> equalHits = new HashMap<>();

        // 設置兩個區域具有相同的打擊率 (0.2)
        equalPitches.put("4", 100);
        equalPitches.put("6", 100);
        equalHits.put("4", 20);
        equalHits.put("6", 20);

        Batter equalBatter = new Batter(equalPitches, equalHits);
        PitchResult result = PitchStrategy.pitch(equalBatter, false);

        // 驗證結果應該是其中之一
        assertTrue(
                result.getStartZone().equals("4") || result.getStartZone().equals("6"),
                "起始區域應該是相同打擊率的區域之一"
        );
    }

    @Test
    @DisplayName("測試非法區域標識的處理")
    void testInvalidZoneIdentifier() {
        Map<String, Integer> invalidPitches = new HashMap<>();
        Map<String, Integer> invalidHits = new HashMap<>();

        // 混合有效和無效區域
        // 無效區域
        invalidPitches.put("x5", 100);   // 非法壞球區
        invalidPitches.put("0", 100);    // 非法好球區
        invalidPitches.put("10", 100);   // 非法好球區

        // 有效區域
        invalidPitches.put("5", 100);    // 有效好球區
        invalidPitches.put("x1", 100);   // 有效壞球區

        // 對應的安打數據
        invalidHits.put("x5", 20);
        invalidHits.put("0", 20);
        invalidHits.put("10", 20);
        invalidHits.put("5", 40);
        invalidHits.put("x1", 10);

        Batter invalidBatter = new Batter(invalidPitches, invalidHits);
        PitchResult result = PitchStrategy.pitch(invalidBatter, true);

        // 驗證結果
        assertTrue(isValidZone(result.getStartZone()),
                "起始區域應為有效區域，實際為: " + result.getStartZone());
        assertTrue(isValidZone(result.getEndZone()),
                "結束區域應為有效區域，實際為: " + result.getEndZone());
    }
    private boolean isValidZone(String zone) {
        if (zone.startsWith("x")) {
            return zone.matches("x[1-4]");
        }
        return zone.matches("[1-9]");
    }

    @ParameterizedTest
    @DisplayName("測試更多球種組合")
    @MethodSource("provideExtendedPitchTypes")
    void testExtendedPitchTypes(String pitchType, String expectedStart, String expectedEnd,
                                boolean ballIsOK, int speed, int spin) {
        PitchResult result = PitchStrategy.pitch(ohtani, ballIsOK);
        assertEquals(expectedStart, result.getStartZone(),
                String.format("%s (速度:%d, 轉速:%d) 的起始區域應為%s",
                        pitchType, speed, spin, expectedStart));
        validatePitchResult(result, ballIsOK, pitchType);
    }

    private static Stream<Arguments> provideExtendedPitchTypes() {
        return Stream.of(
                // 根據圖片中的球種數據
                Arguments.of("四縫線快速球", "5", "1", false, 155, 2350),
                Arguments.of("卡特球", "5", "x3", true, 147, 2450),
                Arguments.of("伸卡球", "5", "x2", true, 142, 2150),
                Arguments.of("二縫線快速球", "5", "3", false, 142, 2250),
                Arguments.of("快指叉球", "5", "x1", true, 137, 1600),
                Arguments.of("指叉球", "5", "x4", true, 127, 850),
                Arguments.of("曲球", "5", "2", false, 120, 2600),
                Arguments.of("滑球", "5", "x2", true, 135, 2550),
                Arguments.of("變速球", "5", "7", false, 130, 1850)
        );
    }

    @ParameterizedTest
    @DisplayName("測試不同count的投球策略")
    @MethodSource("provideCountScenarios")
    void testPitchCountStrategies(int balls, int strikes, boolean shouldThrowStrike) {
        PitchResult result = PitchStrategy.pitch(ohtani, !shouldThrowStrike);

        if (shouldThrowStrike) {
            assertTrue(isStrikeZone(result.getEndZone()),
                    String.format("在%d-%d count時應該投好球", balls, strikes));
        } else {
            // 可以投壞球時，不一定要在好球帶內
            assertTrue(isValidZone(result.getEndZone()),
                    String.format("在%d-%d count時可以嘗試投壞球", balls, strikes));
        }
    }

    private static Stream<Arguments> provideCountScenarios() {
        return Stream.of(
                // [球數, 好球數, 是否必須投好球]
                Arguments.of(3, 0, true),   // 3-0 必須投好球
                Arguments.of(3, 1, true),   // 3-1 必須投好球
                Arguments.of(3, 2, true),   // 3-2 必須投好球
                Arguments.of(0, 0, false),  // 0-0 可以試探
                Arguments.of(1, 1, false),  // 1-1 可以試探
                Arguments.of(0, 2, true),   // 0-2 應該投好球
                Arguments.of(2, 2, true)    // 2-2 應該投好球
        );
    }

    // 輔助方法
    private void validatePitchResult(PitchResult result, boolean ballIsOK, String pitchType) {
        if (!ballIsOK) {
            assertTrue(isStrikeZone(result.getEndZone()),
                    String.format("%s 在不能投壞球時必須在好球帶內", pitchType));
        }
        assertTrue(isValidZone(result.getStartZone()) && isValidZone(result.getEndZone()),
                String.format("%s 的起始和結束區域必須有效", pitchType));
    }

}