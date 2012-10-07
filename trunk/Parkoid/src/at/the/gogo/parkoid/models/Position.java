package at.the.gogo.parkoid.models;

public class Position {

    protected double latitude;
    protected double longitude;

    public Position(final double latitude, final double longitude) {

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

}