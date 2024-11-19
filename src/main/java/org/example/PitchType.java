package org.example;

/**
 * @param name     球種名稱
 * @param minSpeed 最低速度
 * @param maxSpeed 最高速度
 * @param minSpin  最低轉速
 * @param maxSpin  最高轉速
 */
public record PitchType(String name, int minSpeed, int maxSpeed, int minSpin, int maxSpin) {

    @Override
    public String toString() {
        return String.format("%s (速度: %d-%d KPH, 轉速: %d-%d RPM)",
                name, minSpeed, maxSpeed, minSpin, maxSpin);
    }
}