package org.example;

/**
 * 棒球球種類別
 * 用於記錄不同球種的特性，包含速度和轉速範圍
 */
public class PitchType {
    private final String name;        // 球種名稱
    private final int minSpeed;       // 最低速度 (KPH)
    private final int maxSpeed;       // 最高速度 (KPH)
    private final int minSpin;        // 最低轉速 (RPM)
    private final int maxSpin;        // 最高轉速 (RPM)

    /**
     * 建構子
     * @param name 球種名稱
     * @param minSpeed 最低速度
     * @param maxSpeed 最高速度
     * @param minSpin 最低轉速
     * @param maxSpin 最高轉速
     */
    public PitchType(String name, int minSpeed, int maxSpeed, int minSpin, int maxSpin) {
        validateParameters(name, minSpeed, maxSpeed, minSpin, maxSpin);
        this.name = name;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.minSpin = minSpin;
        this.maxSpin = maxSpin;
    }

    /**
     * 驗證參數有效性
     */
    private void validateParameters(String name, int minSpeed, int maxSpeed, int minSpin, int maxSpin) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("球種名稱不能為空");
        }
        if (minSpeed > maxSpeed) {
            throw new IllegalArgumentException("最低速度不能大於最高速度");
        }
        if (minSpin > maxSpin) {
            throw new IllegalArgumentException("最低轉速不能大於最高轉速");
        }
        if (minSpeed < 0 || maxSpeed < 0 || minSpin < 0 || maxSpin < 0) {
            throw new IllegalArgumentException("速度和轉速不能為負數");
        }
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getMinSpeed() {
        return minSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public int getMinSpin() {
        return minSpin;
    }

    public int getMaxSpin() {
        return maxSpin;
    }

    /**
     * 檢查指定速度是否在此球種的速度範圍內
     */
    public boolean isSpeedInRange(int speed) {
        return speed >= minSpeed && speed <= maxSpeed;
    }

    /**
     * 檢查指定轉速是否在此球種的轉速範圍內
     */
    public boolean isSpinInRange(int spin) {
        return spin >= minSpin && spin <= maxSpin;
    }

    /**
     * 取得速度範圍的中間值
     */
    public int getAverageSpeed() {
        return (minSpeed + maxSpeed) / 2;
    }

    /**
     * 取得轉速範圍的中間值
     */
    public int getAverageSpin() {
        return (minSpin + maxSpin) / 2;
    }

    @Override
    public String toString() {
        return String.format("%s (速度: %d-%d KPH, 轉速: %d-%d RPM)",
                name, minSpeed, maxSpeed, minSpin, maxSpin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PitchType other = (PitchType) obj;
        return name.equals(other.name) &&
                minSpeed == other.minSpeed &&
                maxSpeed == other.maxSpeed &&
                minSpin == other.minSpin &&
                maxSpin == other.maxSpin;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + minSpeed;
        result = 31 * result + maxSpeed;
        result = 31 * result + minSpin;
        result = 31 * result + maxSpin;
        return result;
    }

    /**
     * 建立預設的球種物件
     */
    public static PitchType createDefault() {
        return new PitchType("四縫線快速球", 140, 170, 2100, 2600);
    }
}