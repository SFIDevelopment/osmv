package org.outlander.io.JSON;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.andnav.osm.util.GeoPoint;
import org.json.JSONArray;
import org.json.JSONObject;
import org.outlander.model.ViennaKurzParkZone;
import org.outlander.utils.Ut;

public class ParseWKPZ {

    // http://data.wien.gv.at/katalog/kurzparkzonen.html

    public static List<ViennaKurzParkZone> parseJSONData(final String filename) {

        final StringBuilder jSonText = new StringBuilder();
        final List<ViennaKurzParkZone> kurzparkzonen = new ArrayList<ViennaKurzParkZone>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(filename));
            while (scanner.hasNextLine()) {
                jSonText.append(scanner.nextLine());
            }

            final JSONObject json = new JSONObject(jSonText.toString());
            if (json != null) {
                final JSONArray jsonArray = json.getJSONArray("features");
                final int nrOfEntries = jsonArray.length();

                for (int i = 0; i < nrOfEntries; i++) {

                    final ViennaKurzParkZone kurzparkzone = new ViennaKurzParkZone();
                    final JSONObject jsonObject = jsonArray.getJSONObject(i);

                    kurzparkzone.setId(jsonObject.getString("id"));

                    final JSONObject joProperties = jsonObject.getJSONObject("properties");

                    @SuppressWarnings("rawtypes")
                    final Iterator iterator = joProperties.keys();

                    while (iterator.hasNext()) {
                        final String key = (String) iterator.next();
                        kurzparkzone.getProperties().setProperty(key, joProperties.getString(key));
                    }
                    final JSONObject joGeometry = jsonObject.getJSONObject("geometry");
                    final JSONArray jaPolygon = joGeometry.getJSONArray("coordinates").getJSONArray(0).getJSONArray(0);

                    final int numCoords = jaPolygon.length();

                    for (int j = 0; j < numCoords; j++) {
                        final JSONArray joCoords = jaPolygon.getJSONArray(j);

                        final GeoPoint point = new GeoPoint((int) (joCoords.getDouble(1) * 1E6), (int) (joCoords.getDouble(0) * 1E6));

                        kurzparkzone.addParkRaumCoord(point);
                    }
                    kurzparkzonen.add(kurzparkzone);
                }
            }

        }
        catch (final Exception e) {
            e.printStackTrace();
            Ut.d("Vienna Parkraum json parsing error");
        }

        finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return kurzparkzonen;
    }
}
