package at.the.gogo.parkoid.util.webservices;

import java.util.Map;

import org.xmlrpc.android.XMLRPCClient;

import at.the.gogo.parkoid.util.Util;

public class VKPZQuery {

    public final static String VKPZ_URL = "http://spaceinfo.v10.at:8080/spaceinfo/xmlrpc";

    public static boolean checkPointInVKPZ(final double latitude,
            final double longitude) {
        boolean isInArea = false;
        try {

            final XMLRPCClient client = new XMLRPCClient(VKPZQuery.VKPZ_URL);

            isInArea = (Boolean) client
                    .callEx("Spaceinfo.getInZone", new Object[] {
                            new Double(latitude), new Double(longitude) });

        } catch (final Exception x) {
            Util.dd("KLUMP" + x.toString());
        }
        return isInArea;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, String>> getInZone(
            final double longitude, final double latitude, final double accuracy) {
        Map<String, Map<String, String>> result = null;

        try {

            final XMLRPCClient client = new XMLRPCClient(VKPZQuery.VKPZ_URL);

            result = (Map<String, Map<String, String>>) client.callEx(
                    "Spaceinfo.getInZone", new Object[] { new Double(latitude),
                            new Double(longitude), new Double(accuracy) });

        } catch (final Exception x) {
            Util.dd("KLUMP" + x.toString());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, String>> getZones(
            final double longitude, final double latitude,
            final double distance, final double accuracy) {
        Map<String, Map<String, String>> result = null;

        try {

            final XMLRPCClient client = new XMLRPCClient(VKPZQuery.VKPZ_URL);

            result = (Map<String, Map<String, String>>) client.callEx(
                    "Spaceinfo.getZones", new Object[] { new Double(latitude),
                            new Double(longitude), new Double(distance),
                            new Double(accuracy) });

        } catch (final Exception x) {
            Util.dd("KLUMP" + x.toString());
        }

        return result;

    }

}
