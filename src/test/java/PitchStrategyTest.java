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

    private boolean isValidZone(String zone) {
        if (zone.startsWith("x")) {
            return zone.matches("x[1-4]");
        }
        return zone.matches("[1-9]");
    }
}