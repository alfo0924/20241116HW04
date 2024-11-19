package org.example;

/**
 * 表示投球建議的結果
 * 包含起始區域（視覺欺騙位置）和終點區域（實際落點）
 */
public class PitchResult {
    private final String startZone;  // 投球起始區域
    private final String endZone;    // 投球終點區域

    /**
     * 建構子
     * @param startZone 起始區域
     * @param endZone 終點區域
     */
    public PitchResult(String startZone, String endZone) {
        // 驗證區域有效性
        if (!isValidZone(startZone) || !isValidZone(endZone)) {
            throw new IllegalArgumentException("無效的區域標識");
        }
        this.startZone = startZone;
        this.endZone = endZone;
    }

    /**
     * 取得起始區域
     * @return 起始區域標識
     */
    public String getStartZone() {
        return startZone;
    }

    /**
     * 取得終點區域
     * @return 終點區域標識
     */
    public String getEndZone() {
        return endZone;
    }

    /**
     * 檢查是否為有效區域標識
     * @param zone 區域標識
     * @return 是否有效
     */
    private boolean isValidZone(String zone) {
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

    /**
     * 檢查是否為好球區
     * @param zone 區域標識
     * @return 是否為好球區
     */
    public boolean isStrikeZone(String zone) {
        return isValidZone(zone) && !zone.startsWith("x");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PitchResult that = (PitchResult) obj;
        return startZone.equals(that.startZone) && endZone.equals(that.endZone);
    }

    @Override
    public int hashCode() {
        // 使用質數31來減少雜湊碰撞
        return 31 * startZone.hashCode() + endZone.hashCode();
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", startZone, endZone);
    }

    /**
     * 取得投球軌跡描述
     * @return 投球軌跡的描述字串
     */
    public String getTrajectoryDescription() {
        if (startZone.equals(endZone)) {
            return "直線球";
        }
        if (isStrikeZone(startZone) && !isStrikeZone(endZone)) {
            return "出好球帶";
        }
        if (!isStrikeZone(startZone) && isStrikeZone(endZone)) {
            return "進好球帶";
        }
        return "變化球";
    }
}