package at.the.gogo.gpxviewer.mapsources.impl;


/**
 * Utility methods used by several map sources.
 */
public class MapSourceTools {

    // private static final Logger log = Logger.getLogger(MapSourceTools.class);
    protected static final char[] NUM_CHAR = {'0', '1', '2', '3'};


    public MapSourceTools() {
    }

    /**
     * See: http://msdn.microsoft.com/en-us/library/bb259689.aspx
     *
     * @param zoom
     * @param tilex
     * @param tiley
     * @return quadtree encoded tile number
     */
    public static String encodeQuadTree(int zoom, int tilex, int tiley) {
        char[] tileNum = new char[zoom];
        for (int i = zoom - 1; i >= 0; i--) {
            // Binary encoding using ones for tilex and twos for tiley. if a bit
            // is set in tilex and tiley we get a three.
            int num = (tilex % 2) | ((tiley % 2) << 1);
            tileNum[i] = NUM_CHAR[num];
            tilex >>= 1;
            tiley >>= 1;
        }
        return new String(tileNum);
    }


    public static String formatMapUrl(String mapUrl, int zoom, int tilex, int tiley) {
        String tmp = mapUrl;
        tmp = tmp.replace("{$x}", Integer.toString(tilex));
        tmp = tmp.replace("{$y}", Integer.toString(tiley));
        tmp = tmp.replace("{$z}", Integer.toString(zoom));
        return tmp;
    }

    public static String formatMapUrl(String mapUrl, int serverNum, int zoom, int tilex, int tiley) {
        String tmp = mapUrl;
        tmp = tmp.replace("{$servernum}", Integer.toString(serverNum));
        tmp = tmp.replace("{$x}", Integer.toString(tilex));
        tmp = tmp.replace("{$y}", Integer.toString(tiley));
        tmp = tmp.replace("{$z}", Integer.toString(zoom));
        return tmp;
    }
}
