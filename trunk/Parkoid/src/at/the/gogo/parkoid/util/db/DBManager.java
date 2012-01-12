package at.the.gogo.parkoid.util.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import at.the.gogo.parkoid.models.Car;
import at.the.gogo.parkoid.models.CarPosition;
import at.the.gogo.parkoid.models.Position;
import at.the.gogo.parkoid.models.Sms;

public class DBManager {

    protected final Context mCtx;
    private final Database  mDatabase;

    public DBManager(final Context ctx) {
        super();
        mCtx = ctx;
        mDatabase = new Database(ctx);
    }

    public Database getDatabase() {
        return mDatabase;
    }

    public void freeDatabases() {
        mDatabase.freeDatabases();
    }

    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    public void rollbackTransaction() {
        mDatabase.rollbackTransaction();
    }

    public void commitTransaction() {
        mDatabase.commitTransaction();
    }

    // -------- CARS

    public Car getCar(final int carId) {
        Car car = null;
        final Cursor c = mDatabase.getCar(carId);

        if (c != null) {
            if (c.moveToFirst()) {
                car = new Car(c.getInt(0), c.getString(1), c.getString(2));
            }
            c.close();
        }

        return car;
    }

    public void addCar(final String name, final String licence) {
        mDatabase.addCar(name, licence);
    }

    public void updateCar(final Car car) {
        if (car.getId() < 0) {
            mDatabase.addCar(car.getName(), car.getLicence());
        } else {
            mDatabase.updateCar(car.getId(), car.getName(), car.getLicence());
        }
    }

    private List<Car> getCarsFromCursor(final Cursor c) {
        final ArrayList<Car> items = new ArrayList<Car>();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    items.add(new Car(c.getInt(0), c.getString(1), c
                            .getString(2)));
                } while (c.moveToNext());
            }
            c.close();
        }
        return items;
    }

    public List<Car> getCarList() {
        return getCarsFromCursor(mDatabase.getCarListCursor());
    }

    public void deleteCar(final int id) {
        mDatabase.deleteCar(id);
    }

    public void deleteAllCars() {
        mDatabase.deleteAllCars();
    }

    // -------- LOCATIONS

    public Position getLocation(final int locationId) {
        Position location = null;
        final Cursor c = mDatabase.getLocation(locationId);

        if (c != null) {
            if (c.moveToFirst()) {
                location = new CarPosition(c.getInt(0), c.getInt(1),
                        c.getDouble(2), c.getDouble(3), new Date(c.getLong(4)));
            }
            c.close();
        }

        return location;
    }

    public void addLocation(final int carId, final double lat,
            final double lon, final Date date) {
        mDatabase.addLocation(carId, lat, lon, date);
    }

    public void updateLocation(final CarPosition location) {
        if (location.getId() < 0) {
            mDatabase.addLocation(location.getCarId(), location.getLatitude(),
                    location.getLongitude(), location.getDatum());
        } else {
            mDatabase.updateLocation(location.getId(), location.getCarId(),
                    location.getLatitude(), location.getLongitude(),
                    location.getDatum());
        }
    }

    private List<CarPosition> getLocationsFromCursor(final Cursor c) {
        final ArrayList<CarPosition> items = new ArrayList<CarPosition>();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    items.add(new CarPosition(c.getInt(0), c.getInt(1), c
                            .getDouble(2), c.getDouble(3), new Date(c
                            .getLong(4))));
                } while (c.moveToNext());
            }
            c.close();
        }
        return items;
    }

    public List<CarPosition> getLastLocationList(final int carid) {
        return getLocationsFromCursor(mDatabase.getLocationCarListCursor(carid));
    }

    public List<CarPosition> getLastLocationsList() {
        return getLocationsFromCursor(mDatabase.getLocationListCursor());
    }

    public void deleteLocation(final int id) {
        mDatabase.deleteLocation(id);
    }

    public void deleteAllLocations() {
        mDatabase.deleteAllLocations();
    }

    // -------- SMS send

    public Sms getSMS(final int smsId, final String tableName) {
        Sms sms = null;
        final Cursor c = (mDatabase.getSMS(smsId, tableName));

        if (c != null) {
            if (c.moveToFirst()) {
                sms = new Sms(c.getInt(0), c.getString(1), c.getString(2),
                        new Date(c.getLong(3)));
            }
            c.close();
        }

        return sms;
    }

    public void addSMS(final String name, final String text, final Date date,
            final String tableName) {

        mDatabase.addSMS(name, text, date, tableName);

    }

    public void updateSMS(final Sms sms, final String tableName) {
        if (sms.getId() < 0) {

            mDatabase.addSMS(sms.getName(), sms.getText(), sms.getDate(),
                    tableName);
        } else {

            mDatabase.updateSMS(sms.getId(), sms.getName(), sms.getText(),
                    sms.getDate(), tableName);
        }
    }

    private List<Sms> getSMSFromCursor(final Cursor c) {
        final ArrayList<Sms> items = new ArrayList<Sms>();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    items.add(new Sms(c.getInt(0), c.getString(1), c
                            .getString(2), new Date(c.getLong(3))));
                } while (c.moveToNext());
            }
            c.close();
        }
        return items;
    }

    public List<Sms> getSMSList(final String tableName) {
        return getSMSFromCursor(mDatabase.getSMSListCursor(tableName));
    }

    public void deleteSMS(final int id, final String tableName) {
        mDatabase.deleteSMS(id, tableName);
    }

    public void deleteAllSMS(final String tableName) {
        mDatabase.deleteAllSMS(tableName);
    }

}
