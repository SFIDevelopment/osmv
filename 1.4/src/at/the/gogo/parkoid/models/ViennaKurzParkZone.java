package at.the.gogo.parkoid.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.osmdroid.util.GeoPoint;

public class ViennaKurzParkZone {
    private String         id;
    private Properties     properties;
    private List<GeoPoint> polygon;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Properties getProperties() {

        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    public List<GeoPoint> getPolygon() {

        if (polygon == null) {
            polygon = new ArrayList<GeoPoint>();
        }
        return polygon;
    }

    public void setPolygon(final List<GeoPoint> polygon) {
        this.polygon = polygon;
    }

    public void addParkRaumCoord(final GeoPoint prc) {
        getPolygon().add(prc);
    }

}
