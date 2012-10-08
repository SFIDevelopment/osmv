package org.outlander.utils.geo;

/**
 * @author Jim Fandango
 */
import java.util.BitSet;
import java.util.HashMap;

public class GeoHash {

    private static int                       numbits = 6 * 5;
    final static char[]                      digits  = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm',
            'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    final static HashMap<Character, Integer> lookup  = new HashMap<Character, Integer>();

    static {
        int i = 0;
        for (final char c : GeoHash.digits) {
            GeoHash.lookup.put(c, i++);
        }
    }

    public static double[] decode(final String geohash) {
        final StringBuilder buffer = new StringBuilder();
        for (final char c : geohash.toCharArray()) {

            final int i = GeoHash.lookup.get(c) + 32;
            buffer.append(Integer.toString(i, 2).substring(1));
        }

        final BitSet lonset = new BitSet();
        final BitSet latset = new BitSet();

        // even bits
        int j = 0;
        for (int i = 0; i < (GeoHash.numbits * 2); i += 2) {
            boolean isSet = false;
            if (i < buffer.length()) {
                isSet = buffer.charAt(i) == '1';
            }
            lonset.set(j++, isSet);
        }

        // odd bits
        j = 0;
        for (int i = 1; i < (GeoHash.numbits * 2); i += 2) {
            boolean isSet = false;
            if (i < buffer.length()) {
                isSet = buffer.charAt(i) == '1';
            }
            latset.set(j++, isSet);
        }

        final double lon = decode(lonset, -180, 180);
        final double lat = decode(latset, -90, 90);

        return new double[] { lat, lon };
    }

    private static double decode(final BitSet bs, double floor, double ceiling) {
        double mid = 0;
        for (int i = 0; i < bs.length(); i++) {
            mid = (floor + ceiling) / 2;
            if (bs.get(i)) {
                floor = mid;
            }
            else {
                ceiling = mid;
            }
        }
        return mid;
    }

    public static String encode(final double lat, final double lon) {
        final BitSet latbits = getBits(lat, -90, 90);
        final BitSet lonbits = getBits(lon, -180, 180);
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < GeoHash.numbits; i++) {
            buffer.append((lonbits.get(i)) ? '1' : '0');
            buffer.append((latbits.get(i)) ? '1' : '0');
        }
        return base32(Long.parseLong(buffer.toString(), 2));
    }

    private static BitSet getBits(final double lat, double floor, double ceiling) {
        final BitSet buffer = new BitSet(GeoHash.numbits);
        for (int i = 0; i < GeoHash.numbits; i++) {
            final double mid = (floor + ceiling) / 2;
            if (lat >= mid) {
                buffer.set(i);
                floor = mid;
            }
            else {
                ceiling = mid;
            }
        }
        return buffer;
    }

    public static String base32(long i) {
        final char[] buf = new char[65];
        int charPos = 64;
        final boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }
        while (i <= -32) {
            buf[charPos--] = GeoHash.digits[(int) (-(i % 32))];
            i /= 32;
        }
        buf[charPos] = GeoHash.digits[(int) (-i)];

        if (negative) {
            buf[--charPos] = '-';
        }
        return new String(buf, charPos, (65 - charPos));
    }

    public static void main(final String[] args) {

        final double lat = 48.208889;
        final double lon = 16.3725;

        final String hash = encode(lat, lon);

        final double[] latlon = decode(hash);
        System.out.println(hash + " : " + latlon[0] + " " + latlon[1]);
        System.out.println("was : " + lat + " " + lon);

    }

}
