package at.the.gogo.windig.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import at.the.gogo.windig.dto.WindEntry;

public class ParseWebpage {

    final static String INITIALKICK = "</tr>";
    final static String B4VALUE     = "size=\"1\">";
    final static String AFTERVALUE  = "<";

    public static List<WindEntry> parseWebpage(final String url) {
        final List<WindEntry> entries = new ArrayList<WindEntry>();
        WindEntry windentry = null;
        int ele = 0;
        int initcntr = 0;
        BufferedReader in = null;

        try {
            final URL webpage = new URL(url);
            in = new BufferedReader(new InputStreamReader(webpage.openStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                // System.out.println(inputLine);

                if (initcntr < 3) {
                    if (inputLine.contains(ParseWebpage.INITIALKICK)) {
                        initcntr++;
                    }
                } else {
                    if (inputLine.contains(ParseWebpage.INITIALKICK)) {

                        windentry = null;
                        // System.out.println("----");
                    } else {
                        // System.out.println(inputLine.toString());
                        if (inputLine.contains(ParseWebpage.B4VALUE)) {
                            final int begin = inputLine
                                    .lastIndexOf(ParseWebpage.B4VALUE)
                                    + ParseWebpage.B4VALUE.length();
                            final String remainingLine = inputLine
                                    .substring(begin);
                            final int end = remainingLine
                                    .indexOf(ParseWebpage.AFTERVALUE);

                            if (windentry == null) {
                                windentry = new WindEntry();
                                ele = 0;
                                entries.add(windentry);
                            }
                            final String value = remainingLine
                                    .substring(0, end);
                            // System.out.println(value);
                            windentry.setValue(value, ele);
                            ele++;
                        }
                    }
                }
            }

        } catch (final Exception x) {
            if (Util.DEBUGMODE) {
                x.printStackTrace();
            }
            Util.dd("Webpage parsing failed - maybe page unreachable");

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                if (Util.DEBUGMODE) {
                    e.printStackTrace();
                }
                Util.dd("Webpage parsing failed - Stream closing failed");
            }
        }
        return entries;
    }
}
