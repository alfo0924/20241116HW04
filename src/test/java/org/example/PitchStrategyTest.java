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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("投手策略測試")
class PitchStrategyTest {
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
        assertEquals(9, pitchTypes.size(), "應該有9種球種");

        // 測試四縫線快速球的資料
        PitchType fastball = pitchTypes.get(0);
        assertEquals("四縫線快速球", fastball.getName());
        assertEquals(140, fastball.getMinSpeed());
        assertEquals(170, fastball.getMaxSpeed());
        assertEquals(2100, fastball.getMinSpin());
        assertEquals(2600, fastball.getMaxSpin());
    }

    @Test
    @DisplayName("測試大谷翔平打擊熱區分析")
    void testOhtaniHotZones() {
        PitchResult result = PitchStrategy.pitch(ohtani, false);
        assertEquals("5", result.getStartZone(), "起始區域應為5號位置(最高打擊率)");
        assertTrue(PitchStrategy.isStrikeZone(result.getEndZone()), "結束區域應在好球帶內");
    }

    @ParameterizedTest
    @DisplayName("測試不同球種的投球策略")
    @MethodSource("providePitchTypes")
    void testPitchTypes(String pitchType, String expectedStart, String expectedEnd, boolean ballIsOK) {
        PitchResult result = PitchStrategy.getPitchByType(pitchType, ballIsOK);
        assertEquals(expectedStart, result.getStartZone(),
                String.format("%s的起始區域應為%s", pitchType, expectedStart));
        if (!ballIsOK) {
            assertTrue(PitchStrategy.isStrikeZone(result.getEndZone()),
                    String.format("%s的結束區域應在好球帶內", pitchType));
        }
    }

    private static Stream<Arguments> providePitchTypes() {
        return Stream.of(
                Arguments.of("四縫線快速球", "5", "1", false),
                Arguments.of("卡特球", "5", "x3", true),
                Arguments.of("伸卡球", "5", "x2", true),
                Arguments.of("二縫線快速球", "5", "3", false),
                Arguments.of("快指叉球", "5", "x1", true),
                Arguments.of("指叉球", "5", "x4", true),
                Arguments.of("曲球", "5", "2", false),
                Arguments.of("滑球", "5", "x2", true),
                Arguments.of("變速球", "5", "7", false)
        );
    }

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

        // 設置測試數據
        invalidPitches.put("x5", 100);
        invalidPitches.put("0", 100);
        invalidPitches.put("10", 100);
        invalidPitches.put("5", 100);
        invalidPitches.put("x1", 100);

        invalidHits.put("x5", 20);
        invalidHits.put("0", 20);
        invalidHits.put("10", 20);
        invalidHits.put("5", 40);
        invalidHits.put("x1", 10);

        Batter invalidBatter = new Batter(invalidPitches, invalidHits);
        PitchResult result = PitchStrategy.pitch(invalidBatter, true);

        assertTrue(PitchStrategy.isValidZone(result.getStartZone()),
                "起始區域應為有效區域，實際為: " + result.getStartZone());
        assertTrue(PitchStrategy.isValidZone(result.getEndZone()),
                "結束區域應為有效區域，實際為: " + result.getEndZone());
    }

    @ParameterizedTest
    @DisplayName("測試不同count的投球策略")
    @MethodSource("provideCountScenarios")
    void testPitchCountStrategies(int balls, int strikes, boolean shouldThrowStrike) {
        PitchResult result = PitchStrategy.pitch(ohtani, !shouldThrowStrike);

        if (shouldThrowStrike) {
            assertTrue(PitchStrategy.isStrikeZone(result.getEndZone()),
                    String.format("在%d-%d count時應該投好球", balls, strikes));
        } else {
            assertTrue(PitchStrategy.isValidZone(result.getEndZone()),
                    String.format("在%d-%d count時可以嘗試投壞球", balls, strikes));
        }
    }

    private static Stream<Arguments> provideCountScenarios() {
        return Stream.of(
                Arguments.of(3, 0, true),   // 3-0 必須投好球
                Arguments.of(3, 1, true),   // 3-1 必須投好球
                Arguments.of(3, 2, true),   // 3-2 必須投好球
                Arguments.of(0, 0, false),  // 0-0 可以試探
                Arguments.of(1, 1, false),  // 1-1 可以試探
                Arguments.of(0, 2, true),   // 0-2 應該投好球
                Arguments.of(2, 2, true)    // 2-2 應該投好球
        );
    }

    @Test
    @DisplayName("測試CSV數據讀取")
    void testCsvDataLoading() {
        Assertions.assertNotNull(ohtaniPitchBreakdown);
        Assertions.assertNotNull(ohtaniHitsBreakdown);
        assertEquals(13, ohtaniPitchBreakdown.size(), "應該有13個區域的投球數據");
        assertEquals(13, ohtaniHitsBreakdown.size(), "應該有13個區域的安打數據");
        assertEquals(185, ohtaniPitchBreakdown.get("5"), "5號位置的投球數應該是185");
        assertEquals(41, ohtaniHitsBreakdown.get("5"), "5號位置的安打數應該是41");
    }

    private void validatePitchResult(PitchResult result, boolean ballIsOK, String pitchType) {
        if (!ballIsOK) {
            assertTrue(PitchStrategy.isStrikeZone(result.getEndZone()),
                    String.format("%s 在不能投壞球時必須在好球帶內", pitchType));
        }
        assertTrue(PitchStrategy.isValidZone(result.getStartZone()) &&
                        PitchStrategy.isValidZone(result.getEndZone()),
                String.format("%s 的起始和結束區域必須有效", pitchType));
    }
}