package org.outlander.model;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.outlander.R;
import org.outlander.constants.DBConstants;

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
public class TurnRoute {

    String          totalTime;
    String          totalDistance;
    String          startPoint;
    String          endPoint;

    List<GeoPoint>  geometry;
    // both have same index
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

    public List<GeoPoint> getGeometry() {
        if (geometry == null) {
            geometry = new ArrayList<GeoPoint>();
        }
        return geometry;
    }

    public void setGeometry(final List<GeoPoint> geometry) {
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

    public Route getAsRoute() {
        final Route route = new Route();

        route.setCategory(DBConstants.ROUTE_CATEGORY_DEFAULT_NAVIROUTE);
        route.setDescr("From '" + getStartPoint() + "' to '" + getEndPoint() + "'");
        route.setName("TurnByTurn1");

        int i = 0;

        for (final TurnPoint turnPoint : getTurnpoints()) {
            final PoiPoint poiPoint = new PoiPoint(DBConstants.EMPTY_ID, turnPoint.getDescription(), "Distance: " + turnPoint.length + " turntype: "
                    + turnPoint.getTurnType(), getGeometry().get(turnPoint.getIndex()), 1234, DBConstants.ROUTE_CATEGORY_DEFAULT_NAVIROUTE, 0, 0, 0);

            // map direction icons

            if (turnPoint.getTurnType() != null) {
                if (turnPoint.getTurnType().equals("C")) {
                    poiPoint.setIconId(R.drawable.c);
                }
                else if (turnPoint.getTurnType().equals("TL")) {
                    poiPoint.setIconId(R.drawable.tl);
                }
                else if (turnPoint.getTurnType().equals("TR")) {
                    poiPoint.setIconId(R.drawable.tr);
                }
                else if (turnPoint.getTurnType().equals("TSLL")) {
                    poiPoint.setIconId(R.drawable.tsll);
                }
                else if (turnPoint.getTurnType().equals("TSHL")) {
                    poiPoint.setIconId(R.drawable.tshl);
                }
                else if (turnPoint.getTurnType().equals("TSLR")) {
                    poiPoint.setIconId(R.drawable.tslr);
                }
                else if (turnPoint.getTurnType().equals("TSHR")) {
                    poiPoint.setIconId(R.drawable.tshr);
                }
                else if (turnPoint.getTurnType().equals("TU")) {
                    poiPoint.setIconId(R.drawable.tu);
                }
            }
            else {
                poiPoint.setIconId(R.drawable.c);
            }
            route.addRoutePoint(poiPoint);

            i++;
        }

        return route;
    }
}
