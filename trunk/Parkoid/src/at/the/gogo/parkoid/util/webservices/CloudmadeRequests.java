package at.the.gogo.parkoid.util.webservices;

import at.the.gogo.parkoid.models.Route;
import at.the.gogo.parkoid.util.json.CloudmadeRouteParser;

public class CloudmadeRequests {

    private static String BaseURL = "http://routes.cloudmade.com/";
    private static String ApiKey  = "8ee2a50541944fb9bcedded5165f09d9";
    private static String version = "/api/0.3";

    // http://routes.cloudmade.com/8ee2a50541944fb9bcedded5165f09d9/api/0.3/47.25976,9.58423,47.26117,9.59882/bicycle.js
    public static String getRoutingInfoBrutto(final double sourceLat,
            final double sourceLon, final double destLat, final double destLon,
            final boolean byFeed) {
        final String requestUrl = BaseURL + ApiKey + version + "/" + sourceLat
                + "," + sourceLon + "," + destLat + "," + destLon
                + "/bicycle.js";

        return YahooGeocoding.webGetString(requestUrl);
    }

    public static Route getRoutingInfo(final double sourceLat,
            final double sourceLon, final double destLat, final double destLon,
            final boolean byFeed) {
        Route route = null;

        final String result = getRoutingInfoBrutto(sourceLat, sourceLon,
                destLat, destLon, byFeed);

        if (result != null) {
            route = CloudmadeRouteParser.parseJSONData(result);
        }

        return route;
    }

}
