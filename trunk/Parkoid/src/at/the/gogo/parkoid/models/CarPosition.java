package at.the.gogo.parkoid.models;

import java.util.Date;

public class CarPosition extends Position {

    public final static int NO_CAR = -1;

    private Date            datum;
    private int             id;
    private int             carId;

    public CarPosition(final int carId, final double latitude,
            final double longitude, final Date datum) {
        super(latitude, longitude);
        this.datum = datum;
        this.carId = carId;
    }

    public CarPosition(final int id, final int carId, final double latitude,
            final double longitude, final Date datum) {
        super(latitude, longitude);
        this.datum = datum;
        this.id = id;
        this.carId = carId;
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
