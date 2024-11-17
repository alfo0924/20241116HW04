package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PitchStrategyTest {

    @Test
    @DisplayName("測試大谷翔平的打擊數據")
    void testOhtaniData() {
        // 根據圖片建立大谷翔平的打擊數據
        Map<String, Integer> pitchBreakdown = new HashMap<>();
        pitchBreakdown.put("1", 131);
        pitchBreakdown.put("2", 158);
        pitchBreakdown.put("3", 107);
        pitchBreakdown.put("4", 189);
        pitchBreakdown.put("5", 185);
        pitchBreakdown.put("6", 122);
        pitchBreakdown.put("7", 176);
        pitchBreakdown.put("8", 183);
        pitchBreakdown.put("9", 112);
        pitchBreakdown.put("x1", 297);
        pitchBreakdown.put("x2", 248);
        pitchBreakdown.put("x3", 549);
        pitchBreakdown.put("x4", 381);

        Map<String, Integer> baseHitsBreakdown = new HashMap<>();
        baseHitsBreakdown.put("1", 9);
        baseHitsBreakdown.put("2", 15);
        baseHitsBreakdown.put("3", 11);
        baseHitsBreakdown.put("4", 20);
        baseHitsBreakdown.put("5", 41);
        baseHitsBreakdown.put("6", 15);
        baseHitsBreakdown.put("7", 20);
        baseHitsBreakdown.put("8", 21);
        baseHitsBreakdown.put("9", 12);
        baseHitsBreakdown.put("x1", 8);
        baseHitsBreakdown.put("x2", 10);
        baseHitsBreakdown.put("x3", 5);
        baseHitsBreakdown.put("x4", 10);

        Batter ohtani = new Batter(pitchBreakdown, baseHitsBreakdown);

        // 測試不允許投壞球的情況
        PitchResult resultNoBall = PitchStrategy.pitch(ohtani, false);
        assertEquals("5", resultNoBall.getStartZone());
        assertTrue(isStrikeZone(resultNoBall.getEndZone()));

        // 測試允許投壞球的情況
        PitchResult resultWithBall = PitchStrategy.pitch(ohtani, true);
        assertEquals("5", resultWithBall.getStartZone());
    }

    @ParameterizedTest
    @DisplayName("測試各種不同打擊區域組合")
    @MethodSource("providePitchScenarios")
    void testVariousPitchScenarios(Map<String, Integer> pitches, Map<String, Integer> hits,
                                   boolean ballIsOK, String expectedStart, String expectedEnd) {
        Batter batter = new Batter(pitches, hits);
        PitchResult result = PitchStrategy.pitch(batter, ballIsOK);

        assertEquals(expectedStart, result.getStartZone());
        assertEquals(expectedEnd, result.getEndZone());
    }

    private static Stream<Arguments> providePitchScenarios() {
        return Stream.of(
                // 測試場景1：最高打擊率在5號位置，最低在1號位置
                Arguments.of(
                        createPitchMap(new String[]{"1", "5"}, new Integer[]{100, 100}),
                        createHitMap(new String[]{"1", "5"}, new Integer[]{10, 40}),
                        false,
                        "5", "1"
                ),
                // 測試場景2：最高打擊率在5號位置，最低在x1位置
                Arguments.of(
                        createPitchMap(new String[]{"1", "5", "x1"}, new Integer[]{100, 100, 50}),
                        createHitMap(new String[]{"1", "5", "x1"}, new Integer[]{20, 40, 5}),
                        true,
                        "5", "x1"
                )
        );
    }

    @Test
    @DisplayName("測試空數據處理")
    void testEmptyData() {
        Batter emptyBatter = new Batter(new HashMap<>(), new HashMap<>());
        PitchResult result = PitchStrategy.pitch(emptyBatter, true);
        assertNotNull(result);
    }

    private static Map<String, Integer> createPitchMap(String[] zones, Integer[] counts) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < zones.length; i++) {
            map.put(zones[i], counts[i]);
        }
        return map;
    }

    private static Map<String, Integer> createHitMap(String[] zones, Integer[] hits) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < zones.length; i++) {
            map.put(zones[i], hits[i]);
        }
        return map;
    }

    private boolean isStrikeZone(String zone) {
        return !zone.startsWith("x");
    }
}