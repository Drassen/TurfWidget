package com.turfgame.widget.model;

/**
 * Immutable snapshot of a Turf user's stats, as returned by the Turf API.
 */
public final class CharStats {

    private final int points;
    private final int pointsPerHour;
    private final int place;
    private final int zones;

    public CharStats(int points, int pointsPerHour, int place, int zones) {
        this.points = points;
        this.pointsPerHour = pointsPerHour;
        this.place = place;
        this.zones = zones;
    }

    public int points() {
        return points;
    }

    public int pointsPerHour() {
        return pointsPerHour;
    }

    public int place() {
        return place;
    }

    public int zones() {
        return zones;
    }
}
