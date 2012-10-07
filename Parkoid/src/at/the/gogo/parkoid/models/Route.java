package at.the.gogo.parkoid.models;

import java.util.ArrayList;
import java.util.List;

/*
 * { "status":0, "route_instructions":[
 * ["Head south on Perckhoevelaan",111,0,13,"0.1 km","S",160.6],
 * ["Turn left at Laarstraat",112,3,13,"0.1 km","NE",58.1,"TL",269.0],
 * ["Turn right at Goudenregenlaan",70,5,8,"70 m","SE",143.4,"TR",89.8] ],
 * "route_summary"
 * :{"total_time":34,"total_distance":293,"end_point":"Goudenregenlaan"
 * ,"start_point":"Perckhoevelaan"},
 * "route_geometry":[[51.17702,4.3963],[51.17656
 * ,4.39655],[51.17639,4.3967],[51.17612
 * ,4.39696],[51.1764,4.39767],[51.17668,4.39828
 * ],[51.17628,4.39874],[51.17618,4.39888]], "version":"0.3" }
 */
public class Route {

    String          totalTime;
    String          totalDistance;
    String          startPoint;
    String          endPoint;

    List<Position>  geometry;

    List<TurnPoint> turnPoints;

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(final String totalTime) {
        this.totalTime = totalTime;
    }

    public String getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(final String totalDistance) {
        this.totalDistance = totalDistance;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(final String startpoint) {
        this.startPoint = startpoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(final String endPoint) {
        this.endPoint = endPoint;
    }

    public List<Position> getGeometry() {
        if (geometry == null) {
            geometry = new ArrayList<Position>();
        }
        return geometry;
    }

    public void setGeometry(final List<Position> geometry) {
        this.geometry = geometry;
    }

    public List<TurnPoint> getTurnpoints() {
        if (turnPoints == null) {
            turnPoints = new ArrayList<TurnPoint>();
        }
        return turnPoints;
    }

    public void setTurnpoints(final List<TurnPoint> turnpoints) {
        this.turnPoints = turnpoints;
    }

}
