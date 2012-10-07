package at.the.gogo.parkoid.models;

/**
 * 
 * ... as used by Car2Go
 * 
 * @author johannes
 * 
 */

// {
// "placemarks":[
// {
// "coordinates":[
// 9.987988,
// 48.358829,
// 0
// ],
// "name":"Shell, Hauptstrasse 12"
// },
// {
// "coordinates":[
// 9.990183,
// 48.404832,
// 0
// ],
// "name":"Shell, Karlstrasse 38"
// }
// ]
// }

public class C2G_Gas {

    String   name;
    Position position;

    public C2G_Gas(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(final Position position) {
        this.position = position;
    }

}
