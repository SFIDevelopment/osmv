package at.the.gogo.parkoid.models;

import java.util.Date;

public class Position {

    public final static int NO_CAR = -1;

    private double          latitude;
    private double          longitude;
    private Date            datum;
    private int             id;
    private int             carId;

    public Position(final int carId, final double latitude,
            final double longitude, final Date datum) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.datum = datum;
        this.carId = carId;
    }

    public Position(final int id, final int carId, final double latitude,
            final double longitude, final Date datum) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.datum = datum;
        this.id = id;
        this.carId = carId;
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

    public Date getDatum() {
        return datum;
    }

    public void setDatum(final Date datum) {
        this.datum = datum;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(final int carId) {
        this.carId = carId;
    }

}
