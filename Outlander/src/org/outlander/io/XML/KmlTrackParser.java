package org.outlander.io.XML;

import org.outlander.io.db.DBManager;
import org.outlander.model.Track;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class KmlTrackParser extends DefaultHandler {

    private final StringBuilder builder;
    private final DBManager     mPoiManager;
    private Track               mTrack;
    private String[]            mStrArray;
    private String[]            mStrArray2;
    private boolean             mItIsTrack;

    private static final String Placemark   = "Placemark";
    private static final String LineString  = "LineString";
    private static final String NAME        = "name";
    private static final String coordinates = "coordinates";
    private static final String description = "description";

    public KmlTrackParser(final DBManager poiManager) {
        super();
        builder = new StringBuilder();
        mPoiManager = poiManager;
        mTrack = new Track();
        mItIsTrack = false;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        builder.append(ch, start, length);
        super.characters(ch, start, length);
    }

    @Override
    public void startElement(final String uri, final String localName, final String name, final Attributes attributes) throws SAXException {
        builder.delete(0, builder.length());
        if (localName.equalsIgnoreCase(KmlTrackParser.Placemark)) {
            mTrack = new Track();
            mItIsTrack = false;
        }
        super.startElement(uri, localName, name, attributes);
    }

    @Override
    public void endElement(final String uri, final String localName, final String name) throws SAXException {
        if (localName.equalsIgnoreCase(KmlTrackParser.Placemark)) {
            if (mItIsTrack) {
                if (mTrack.Name.equalsIgnoreCase("")) {
                    mTrack.Name = "Track";
                }
                mPoiManager.updateTrack(mTrack, true);
            }
        }
        else if (localName.equalsIgnoreCase(KmlTrackParser.NAME)) {
            mTrack.Name = builder.toString().trim();
        }
        else if (localName.equalsIgnoreCase(KmlTrackParser.description)) {
            mTrack.Descr = builder.toString().trim();
        }
        else if (localName.equalsIgnoreCase(KmlTrackParser.coordinates)) {
            mStrArray = builder.toString().trim().split("\n");
            if (mStrArray.length < 2) {
                mStrArray = builder.toString().trim().split(" ");
            }
            for (int i = 0; i < mStrArray.length; i++) {
                if (!mStrArray[i].trim().equals("")) {
                    mStrArray2 = mStrArray[i].trim().split(",");
                    mTrack.AddTrackPoint();
                    mTrack.LastTrackPoint.setLatitude(Double.parseDouble(mStrArray2[1]));
                    mTrack.LastTrackPoint.setLongitude(Double.parseDouble(mStrArray2[0]));
                    if (mStrArray2.length > 2) {
                        try {
                            mTrack.LastTrackPoint.alt = Double.parseDouble(mStrArray2[2]);
                        }
                        catch (final NumberFormatException e) {
                            try {
                                mTrack.LastTrackPoint.alt = Integer.parseInt(mStrArray2[2]);
                            }
                            catch (final NumberFormatException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        else if (localName.equalsIgnoreCase(KmlTrackParser.LineString)) {
            mItIsTrack = true;
        }
        super.endElement(uri, localName, name);
    }

}
