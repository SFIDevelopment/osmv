package at.the.gogo.parkoid.models;

/**
 * ... as used by car2go
 * 
 * @author johannes
 * 
 */

// {
// "placemarks":[
// {
// "address":"Reuttier Strasse 133, 89231 Neu-Ulm",
// "coordinates":[
// 10.023925,
// 48.383125,
// 0
// ],
// "exterior":"GOOD",
// "fuel":98,
// "interior":"GOOD",
// "name":"UL-C5704",
// "vin":"WME4513001K155530",
// "engineType":"CE"
// }
// ]
// }

public class C2G_Vehicle extends Car {

    String address;
    Position position;

    String exterior;
    String interior;
    String fuel;
    String engineType;
    String vin;

    public C2G_Vehicle(final int id, final String name, final String licence) {
        super(id, name, licence);

    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(final Position position) {
        this.position = position;
    }

    public String getExterior() {
        return exterior;
    }

    public void setExterior(final String exterior) {
        this.exterior = exterior;
    }

    public String getInterior() {
        return interior;
    }

    public void setInterior(final String interior) {
        this.interior = interior;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(final String fuel) {
        this.fuel = fuel;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(final String engineType) {
        this.engineType = engineType;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(final String vin) {
        this.vin = vin;
    }

}
