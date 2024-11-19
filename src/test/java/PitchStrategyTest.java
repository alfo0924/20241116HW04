package org.example;

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
class PitchStrategyTest {
    private Batter ohtani;
    private Map<String, Integer> ohtaniPitchBreakdown;
    private Map<String, Integer> ohtaniHitsBreakdown;

    @BeforeEach
    void setUp() {
        // 初始化大谷翔平的打擊數據
        ohtaniPitchBreakdown = new HashMap<>();
        ohtaniHitsBreakdown = new HashMap<>();

        // 從圖片讀取的Pitch Breakdown數據
        initializePitchBreakdown();
        // 從圖片讀取的Base Hits Breakdown數據
        initializeHitsBreakdown();

        ohtani = new Batter(ohtaniPitchBreakdown, ohtaniHitsBreakdown);
    }

    private void initializePitchBreakdown() {
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
    }

    private void initializeHitsBreakdown() {
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
    }

    @Test
    @DisplayName("測試讀取球種資料")
    void testLoadPitchTypes() {
        List<PitchType> pitchTypes = PitchStrategy.loadPitchTypes("pitch_types.csv");
        assertNotNull(pitchTypes);
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