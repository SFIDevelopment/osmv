package org.outlander.io.JSON;

import org.andnav.osm.util.GeoPoint;
import org.json.JSONArray;
import org.json.JSONObject;
import org.outlander.model.TurnPoint;
import org.outlander.model.TurnRoute;
import org.outlander.utils.Ut;

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

public class CloudmadeRouteParser {

    public static TurnRoute parseJSONData(final String jsonText) {

        final TurnRoute route = new TurnRoute();

        try {
            final JSONObject json = new JSONObject(jsonText.toString());
            if (json != null) {

                final JSONArray jsonArray = json.getJSONArray("route_instructions");

                int nrOfEntries = jsonArray.length();

                for (int i = 0; i < nrOfEntries; i++) {
                    final JSONArray jsonRouteInstructions = jsonArray.getJSONArray(i);
                    final TurnPoint turnpoint = new TurnPoint();

                    turnpoint.setDescription(jsonRouteInstructions.getString(0));
                    turnpoint.setLength(jsonRouteInstructions.getDouble(1));
                    turnpoint.setIndex(jsonRouteInstructions.getInt(2));
                    turnpoint.setLengthCaption(jsonRouteInstructions.getString(4));
                    turnpoint.setEarthDirection(jsonRouteInstructions.getString(5));

                    if (i > 0) {
                        turnpoint.setAzimuth(jsonRouteInstructions.getDouble(6));
                        turnpoint.setTurnType(jsonRouteInstructions.getString(7)); // only
                                                                                   // >
                                                                                   // 1
                    }
                    turnpoint.setTurnAngle(jsonRouteInstructions.getDouble(i == 0 ? 6 : 8));

                    route.getTurnpoints().add(turnpoint);
                }

                final JSONObject jsonObject = json.getJSONObject("route_summary");

                route.setEndPoint(jsonObject.getString("end_point"));
                route.setStartPoint(jsonObject.getString("start_point"));
                route.setTotalDistance(jsonObject.getString("total_distance"));
                route.setTotalTime(jsonObject.getString("total_time"));

                final JSONArray jsonGeometry = json.getJSONArray("route_geometry");
                nrOfEntries = jsonGeometry.length();
                for (int i = 0; i < nrOfEntries; i++) {
                    final JSONArray jsonPosition = jsonGeometry.getJSONArray(i);
                    final GeoPoint position = new GeoPoint(jsonPosition.getDouble(0), jsonPosition.getDouble(1));
                    route.getGeometry().add(position);
                }

            }
        }
        catch (final Exception x) {
            x.printStackTrace();
            Ut.d("Cloudmade route json parsing error");
        }
        return route;

    }

}
