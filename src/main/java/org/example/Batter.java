package org.example;

import java.util.HashMap;
import java.util.Map;

public record Batter(Map<String, Integer> pitchBreakdown, Map<String, Integer> baseHitsBreakdown) {
}

