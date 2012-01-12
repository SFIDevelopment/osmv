package at.the.gogo.parkoid.models;

/**
 * ... as used by Car2Go
 * 
 * @author johannes
 * 
 */

// {
// "placemarks":[
// {
// "coordinates":[
// -97.750983,
// 30.269577,
// 0
// ],
// "name":"West Ave",
// "totalCapacity":4,
// "usedCapacity":0,
// "chargingPole":false
// },
// {
// "coordinates":[
// -97.74225,
// 30.265976,
// 0
// ],
// "name":"100 East 4th Street",
// "totalCapacity":4,
// "usedCapacity":0,
// "chargingPole":true
// }
// }
public class C2G_ParkingSpot {

    String name;
    int totalCapacity;
    int usedCapacity;
    Position position;
    boolean chargingPole;

    public C2G_ParkingSpot(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(final int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public int getUsedCapacity() {
        return usedCapacity;
    }

    public void setUsedCapacity(final int usedCapacity) {
        this.usedCapacity = usedCapacity;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(final Position position) {
        this.position = position;
    }

    public boolean isChargingPole() {
        return chargingPole;
    }

    public void setChargingPole(boolean chargingPole) {
        this.chargingPole = chargingPole;
    }
}
