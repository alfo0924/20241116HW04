package org.example;

public class PitchResult {
    private final String startZone;
    private final String endZone;

    public PitchResult(String startZone, String endZone) {
        this.startZone = startZone;
        this.endZone = endZone;
    }

    public String getStartZone() {
        return startZone;
    }

    public String getEndZone() {
        return endZone;
    }

    @Override
    public String toString() {
        return "(" + startZone + ", " + endZone + ")";
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
        return 31 * startZone.hashCode() + endZone.hashCode();
    }
}
