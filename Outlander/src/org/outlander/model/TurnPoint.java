package org.outlander.model;

public class TurnPoint {

    String description;
    double length;
    int    index;
    String length_caption;
    String earth_direction;
    double azimuth;
    String turnType;
    double turnangle;

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public double getLength() {
        return length;
    }

    public void setLength(final double length) {
        this.length = length;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public String getLength_caption() {
        return length_caption;
    }

    public void setLengthCaption(final String length_caption) {
        this.length_caption = length_caption;
    }

    public String getEarth_direction() {
        return earth_direction;
    }

    public void setEarthDirection(final String earth_direction) {
        this.earth_direction = earth_direction;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(final double azimuth) {
        this.azimuth = azimuth;
    }

    public String getTurnType() {
        return turnType;
    }

    public void setTurnType(final String turnType) {
        this.turnType = turnType;
    }

    public double getTurnangle() {
        return turnangle;
    }

    public void setTurnAngle(final double turnangle) {
        this.turnangle = turnangle;
    }

}
