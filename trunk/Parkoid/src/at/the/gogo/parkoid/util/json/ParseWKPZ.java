package at.the.gogo.parkoid.util.json;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import at.the.gogo.parkoid.models.ViennaKurzParkZone;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.Util;

public class ParseWKPZ {

    // http://data.wien.gv.at/katalog/kurzparkzonen.html

    public static List<ViennaKurzParkZone> parseJSONData(final InputStream file) {

        final StringBuilder jSonText = new StringBuilder();
        List<ViennaKurzParkZone> kurzparkzonen = null;

        Scanner scanner = null;
        try {
            scanner = new Scanner((file));
            while (scanner.hasNextLine()) {
                jSonText.append(scanner.nextLine());
            }

            kurzparkzonen = parseJSONData(jSonText);

        } catch (final Exception e) {
            e.printStackTrace();
            Util.d("Vienna Parkraum json parsing error");
        }

        finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return kurzparkzonen;
    }

    public static List<ViennaKurzParkZone> parseJSONData(final String filename) {
        List<ViennaKurzParkZone> kurzparkzonen = null;

        try {
            kurzparkzonen = parseJSONData(new FileInputStream(filename));

        } catch (final Exception e) {
            e.printStackTrace();
            Util.d("Vienna Parkraum json parsing error");
        }

        return kurzparkzonen;
    }

    private static List<ViennaKurzParkZone> parseJSONData(
            final StringBuilder jsonText) {

        final List<ViennaKurzParkZone> kurzparkzonen = new ArrayList<ViennaKurzParkZone>();

        try {
            final JSONObject json = new JSONObject(jsonText.toString());
            if (json != null) {
                final JSONArray jsonArray = json.getJSONArray("features");
                final int nrOfEntries = jsonArray.length();

                for (int i = 0; i < nrOfEntries; i++) {

                    final ViennaKurzParkZone kurzparkzone = new ViennaKurzParkZone();
                    final JSONObject jsonObject = jsonArray.getJSONObject(i);

                    kurzparkzone.setId(jsonObject.getString("id"));

                    final JSONObject joProperties = jsonObject
                            .getJSONObject("properties");

                    @SuppressWarnings("rawtypes")
                    final Iterator iterator = joProperties.keys();

                    while (iterator.hasNext()) {
                        final String key = (String) iterator.next();
                        kurzparkzone.getProperties().setProperty(key,
                                joProperties.getString(key));
                    }
                    final JSONObject joGeometry = jsonObject
                            .getJSONObject("geometry");
                    final JSONArray jaPolygon = joGeometry
                            .getJSONArray("coordinates").getJSONArray(0)
                            .getJSONArray(0);

                    final int numCoords = jaPolygon.length();

                    for (int j = 0; j < numCoords; j++) {
                        final JSONArray joCoords = jaPolygon.getJSONArray(j);

                        final GeoPoint point = new GeoPoint(
                                (int) (joCoords.getDouble(1) * 1E6),
                                (int) (joCoords.getDouble(0) * 1E6));

                        kurzparkzone.addParkRaumCoord(point);
                    }
                    kurzparkzonen.add(kurzparkzone);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            Util.d("Vienna Parkraum json parsing error");
        }

        return kurzparkzonen;
    }

    /**
     * add elements to internal cache return list of new elements
     * 
     * @param rawValues
     * @param parsedeep
     *            ( use if only interrested in adddresses not coords )
     * @return
     */
    public static Map<String, ViennaKurzParkZone> parseWebserviceData(
            final Map<String, Map<String, String>> rawValues,
            final boolean parsedeep) {

        final Map<String, ViennaKurzParkZone> cacheList = CoreInfoHolder
                .getInstance().getVKPZCacheList();

        final Map<String, ViennaKurzParkZone> kpzList = new HashMap<String, ViennaKurzParkZone>();

        try {
            // parse it a little
            // "geometry"
            // 11-01 13:33:06.642: D/Parkoid(16229): subkey :geometry | value :
            // {"type":"Polygon","coordinates":[[[16.361533289185616,48.20266123322078],[16.361534254314531,48.202662345469932],[16.361762217558894,48.20249460263306],[16.362942039557673,48.201892165794966],[16.364024937219284,48.201331664985169],[16.364070462789311,48.201179206076247],[16.36425283710572,48.201076260161969],[16.36454920298738,48.200923739426734],[16.364834163214031,48.200763599906992],[16.365020441155558,48.200632005651315],[16.365758577703954,48.200042517366597],[16.365916144461128,48.199956618259108],[16.365974492272468,48.199907820234976],[16.366032845860367,48.199866827406304],[16.366099970212964,48.19985119857521],[16.366170020301748,48.199849228762297],[16.36623303756738,48.199884028682526],[16.36636538746658,48.199751198972969],[16.367690033804152,48.200389527091353],[16.367860337032099,48.200452717927966],[16.368002236684227,48.200471648103239],[16.368125197279706,48.200458967667679],[16.368399477546603,48.200408301382907],[16.372021759480045,48.199616775532611],[16.372683844929494,48.199559654156751],[16.372806829265496,48.199584910257578],[16.372948695277277,48.199559569645324],[16.373487817410343,48.19950880699308],[16.376647733818373,48.200310837019877],[16.377689066261588,48.201341215575283],[16.378702028793871,48.202333652589289],[16.378967125401317,48.202611792939756],[16.380159874145633,48.203604151562892],[16.38066162058227,48.204046610345998],[16.380926729782573,48.204318422953513],[16.381087719402871,48.204514392657131],[16.381400289238613,48.204963246001661],[16.381750700147453,48.205405761215587],[16.38253687615801,48.206505755798432],[16.383124160111077,48.207327586788416],[16.383692496423283,48.208098834738173],[16.383995609727965,48.208503419133507],[16.384317672765771,48.208933290887224],[16.384459802841196,48.209167204206459],[16.384516806423324,48.209420125291047],[16.384583346848899,48.209755248421992],[16.384897079210337,48.211361312331462],[16.384499759442019,48.211412068805359],[16.384499813038929,48.211468981445087],[16.384481009927963,48.211595461834946],[16.384386499850901,48.211703002833076],[16.384282491986955,48.211772606256872],[16.384131155107543,48.211816935040183],[16.383960863516101,48.211829652268278],[16.383705394192141,48.211810788876193],[16.383336362262693,48.211760351457023],[16.382957908267787,48.211754182192443],[16.382238801020069,48.211691237041109],[16.381481897088953,48.211678890937989],[16.380734486916516,48.211704477642364],[16.379930341551894,48.211768022588316],[16.37909783651552,48.211863190854025],[16.378274840781778,48.212021586289083],[16.377546465021521,48.212192588040224],[16.377026241987373,48.212376159375999],[16.37661008871175,48.212559690858946],[16.376269593371902,48.212705252974793],[16.375437409960874,48.213249370847741],[16.375049788429028,48.213641566279833],[16.374728424481653,48.214078003636942],[16.374425967591407,48.214501786614839],[16.373792712452904,48.215425242273106],[16.373480793834499,48.21586799603611],[16.373282242959473,48.216076738760215],[16.373055291582848,48.216266519986611],[16.372582465552533,48.216652411100384],[16.371438162567514,48.217538071994944],[16.370378924572545,48.218341490075801],[16.370151937044302,48.218505971220878],[16.369432432353367,48.217949704873753],[16.368807732696443,48.217658998902898],[16.360431802231151,48.213816322264975],[16.360006058286682,48.213873324099531],[16.360006064166569,48.213885972150351],[16.359807380057077,48.213904983516358],[16.359788442420175,48.213873369083196],[16.359665450548604,48.213892365503554],[16.359618155120874,48.213917669480296],[16.356514923850714,48.214341943385421],[16.355358749875641,48.209397080728685],[16.355358652824869,48.209137813456081],[16.355169248472379,48.208625631696336],[16.355424386449034,48.207828814161097],[16.355481058433881,48.2075885066781],[16.355537728543787,48.207348200056764],[16.355670084434109,48.207107880559782],[16.355859214835032,48.20691181671048],[16.356313182731238,48.206589232873469],[16.356341549042984,48.206551286383991],[16.357221074114339,48.205880825722289],[16.361533289185616,4

            if ((rawValues != null) && (rawValues.size() > 0)) {
                final Iterator<String> iterator = rawValues.keySet().iterator();

                while (iterator.hasNext()) {

                    final String key = iterator.next();

                    // if (!cacheList.containsKey(key)) {

                    final ViennaKurzParkZone kpz = new ViennaKurzParkZone();
                    kpz.setId(key);

                    Util.d("key :" + key);

                    final Map<String, String> map = rawValues.get(key);
                    final Iterator<String> iterator2 = map.keySet().iterator();
                    while (iterator2.hasNext()) {
                        final String key2 = iterator2.next();

                        if (!key2.equalsIgnoreCase("geometry")) {
                            kpz.getProperties()
                                    .setProperty(key2, map.get(key2));
                        } else if (parsedeep) {
                            final JSONObject json = new JSONObject(
                                    map.get(key2));

                            final JSONArray jaPolygon = json.getJSONArray(
                                    "coordinates").getJSONArray(0);

                            final int numCoords = jaPolygon.length();

                            for (int j = 0; j < numCoords; j++) {
                                final JSONArray joCoords = jaPolygon
                                        .getJSONArray(j);

                                final GeoPoint point = new GeoPoint(
                                        (int) (joCoords.getDouble(1) * 1E6),
                                        (int) (joCoords.getDouble(0) * 1E6));

                                kpz.addParkRaumCoord(point);
                            }
                        }
                        Util.d("subkey :" + key2 + " | value : "
                                + map.get(key2));

                    }
                    kpzList.put(key, kpz);
                        
                    if (!cacheList.containsKey(key)) {
                        cacheList.put(key, kpz);
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            Util.d("Vienna Parkraum json parsing error");
        }

        return kpzList;
    }

}
