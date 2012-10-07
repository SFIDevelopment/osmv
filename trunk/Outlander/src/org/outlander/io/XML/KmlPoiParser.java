package org.outlander.io.XML;

import org.andnav.osm.util.GeoPoint;
import org.outlander.io.db.DBManager;
import org.outlander.model.PoiPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class KmlPoiParser extends DefaultHandler {
    private final StringBuilder builder;
    private final DBManager     mPoiManager;
    private PoiPoint            mPoiPoint;
    private final int           mCategoryId;
    private String[]            mStrArray;
    private boolean             mItIsPoint;

    private static final String Placemark   = "Placemark";
    private static final String Point       = "Point";
    private static final String NAME        = "name";
    private static final String coordinates = "coordinates";
    private static final String description = "description";

    public KmlPoiParser(final DBManager poiManager, final int CategoryId) {
        super();
        builder = new StringBuilder();
        mPoiManager = poiManager;
        mCategoryId = CategoryId;
        mPoiPoint = new PoiPoint();
        mItIsPoint = false;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        builder.append(ch, start, length);
        super.characters(ch, start, length);
    }

    @Override
    public void startElement(final String uri, final String localName,
            final String name, final Attributes attributes) throws SAXException {
        builder.delete(0, builder.length());
        if (localName.equalsIgnoreCase(KmlPoiParser.Placemark)) {
            mPoiPoint = new PoiPoint();
            mPoiPoint.setCategoryId(mCategoryId);
            mItIsPoint = false;
        }
        super.startElement(uri, localName, name, attributes);
    }

    @Override
    public void endElement(final String uri, final String localName,
            final String name) throws SAXException {
        if (localName.equalsIgnoreCase(KmlPoiParser.Placemark)) {
            if (mItIsPoint) {
                if (mPoiPoint.getTitle().equalsIgnoreCase("")) {
                    mPoiPoint.setTitle("POI");
                }
                mPoiManager.updatePoi(mPoiPoint);
            }
        } else if (localName.equalsIgnoreCase(KmlPoiParser.NAME)) {
            mPoiPoint.setTitle(builder.toString().trim());
        } else if (localName.equalsIgnoreCase(KmlPoiParser.description)) {
            mPoiPoint.setDescr(builder.toString().trim());
        } else if (localName.equalsIgnoreCase(KmlPoiParser.coordinates)) {
            mStrArray = builder.toString().split(",");
            mPoiPoint.setGeoPoint(GeoPoint.from2DoubleString(mStrArray[1],
                    mStrArray[0]));
        } else if (localName.equalsIgnoreCase(KmlPoiParser.Point)) {
            mItIsPoint = true;
        }
        super.endElement(uri, localName, name);
    }

}
