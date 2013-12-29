package at.the.gogo.gpxviewer.util.geo;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import at.the.gogo.gpxviewer.model.PoiPoint;
import at.the.gogo.gpxviewer.model.Route;
import at.the.gogo.gpxviewer.model.Track;

public class GpxParser extends DefaultHandler {

    private final StringBuilder builder;
    
    private Track               mTrack;
    private Route               route;
    private PoiPoint            point;

    private GPXContentHandler gpxContent;
    
    
    private static final String TRK   = "trk";
    private static final String LAT   = "lat";
    private static final String LON   = "lon";
    private static final String TIME  = "time";
    private static final String NAME  = "name";
    private static final String CMT   = "cmt";
    private static final String DESC  = "desc";
    private static final String POINT = "trkpt";
    private static final String ELE   = "ele";
    private static final String RTE   = "rte";
    private static final String RTEPT = "rtept";
    private static final String WPT   = "wpt";

//    private final boolean       overwrite;

    public GpxParser( final boolean overwrite) {
        super();
        
        builder = new StringBuilder();
        
    
//        this.overwrite = overwrite;
    }

    public GPXContentHandler getGPXContent()
    {
    	return gpxContent;
    }
    
    
    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        builder.append(ch, start, length);
        super.characters(ch, start, length);
    }

    @Override
    public void startElement(final String uri, final String localName, final String name, final Attributes attributes) throws SAXException {
        builder.delete(0, builder.length());
        if (localName.equalsIgnoreCase(GpxParser.TRK)) {
//            Ut.dd("Start parsing Element Track");
            mTrack = new Track();
            
        }
        else if (localName.equalsIgnoreCase(GpxParser.POINT)) {
//            Ut.dd("Start parsing Element Trackpoint");
            mTrack.AddTrackPoint();
            final GeoPoint gp = GeoPoint.from2DoubleString(attributes.getValue(GpxParser.LAT), attributes.getValue(GpxParser.LON));
            mTrack.LastTrackPoint.setLatitude(gp.getLatitude());
            mTrack.LastTrackPoint.setLongitude(gp.getLongitude());
        }
        else if (localName.equalsIgnoreCase(GpxParser.RTEPT)) {
//            Ut.dd("Start parsing Element RoutePoint");
            route.addRoutePoint();
            route.lastRoutePoint.setGeoPoint(GeoPoint.from2DoubleString(attributes.getValue(GpxParser.LAT), attributes.getValue(GpxParser.LON)));

        }
        else if (localName.equalsIgnoreCase(GpxParser.WPT)) {
//            Ut.dd("Start parsing Element WayPoint");
            point = new PoiPoint();
            point.setGeoPoint(GeoPoint.from2DoubleString(attributes.getValue(GpxParser.LAT), attributes.getValue(GpxParser.LON)));
//            point.setCategoryId(poiCategoryId);
            
        }
        else if (localName.equalsIgnoreCase(GpxParser.RTE)) {
//            Ut.dd("Start parsing Element Route");
            route = new Route();
            
//            route.setCategory(routeCategoryId);
        }

        super.startElement(uri, localName, name, attributes);
    }

    @Override
    public void endElement(final String uri, final String localName, final String name) throws SAXException {

        // route closed
        if (localName.equalsIgnoreCase(GpxParser.RTE)) {
//            Ut.dd("Finish parsing Element Route");

//            if (!overwrite) {
//                final Route existingRoute = mPoiManager.getRouteByName(route.getName());
//                if ((existingRoute != null) && (existingRoute.getId() > -1)) {
//                    route.setId(existingRoute.getId());
//                }
//            }
//            mPoiManager.updateRoute(route, true);

        	
        	gpxContent.getRoutes().add(route);
        	
            route = null;
        }
        else
        // track closed
        if (localName.equalsIgnoreCase(GpxParser.TRK)) {
//            Ut.dd("Finish parsing Element Track");
            if (mTrack.Name.equalsIgnoreCase("")) {
                mTrack.Name = "Track";
            }

            // precalc stat for track
            // mTrack.AvgSpeed = GeoMathUtil.avgSpeed(mTrack.getPoints());
            mTrack.Distance = GeoMathUtil.distance(mTrack.getPoints());  // in meters
            mTrack.Time = mTrack.getLastTrackPoint().date.getTime() - mTrack.getFirstTrackPoint().date.getTime(); // in seconds
            
            mTrack.AvgSpeed = (mTrack.Distance / mTrack.Time); // m/s 

//            if (!overwrite) {
//                // try to get existing
//                final Track existingTrack = mPoiManager.getTrackByName(mTrack.Name);
//                if ((existingTrack != null) && (existingTrack.getId() > -1)) {
//                    mTrack.setId(existingTrack.getId());
//                }
//            }
//            mPoiManager.updateTrack(mTrack, true);

            gpxContent.getTracks().add(mTrack);
            
            mTrack = null;
        }
        // wpt / rtept / trkpt closed
        else if (localName.equalsIgnoreCase(GpxParser.WPT)) {
//            Ut.dd("Finish parsing Element Waypoint");
            // single wpt

//            if (!overwrite) {
//                final PoiPoint existingPoi = mPoiManager.getPoiPointByName(point.getTitle());
//                if ((existingPoi != null) && (existingPoi.getId() > -1)) {
//                    point.setId(existingPoi.getId());
//                }
//            }
//            mPoiManager.updatePoi(point);
        	
        	gpxContent.getPoints().add(point);
        	
            point = null;
        }
        else if (localName.equalsIgnoreCase(GpxParser.NAME)) {
            final String title = builder.toString().trim();

            if (point != null) {
                point.setTitle(title);
            }
            else if (route != null) {
                if (route.lastRoutePoint != null) {
                    route.lastRoutePoint.setTitle(title);
                }
                else {
                    route.setName(title);
                }
            }
            else if (mTrack != null) {
                if (mTrack.LastTrackPoint != null) {
                    // NYI !!!
                }
                else {
                    mTrack.Name = title;
                }
            }
        }
        else if (localName.equalsIgnoreCase(GpxParser.DESC)) {
            final String desc = builder.toString().trim();

            if (point != null) {
                {
                    point.setDescr(desc);
                }
            }
            else if (route != null) {
                if (route.lastRoutePoint != null) {
                    route.lastRoutePoint.setDescr(desc);
                }
                else {
                    route.setDescr(desc);
                }
            }
            else if (mTrack != null) {
                if (mTrack.LastTrackPoint != null) {
                    // NYI !!!
                }
                else {
                    mTrack.Descr = desc;
                }
            }
        }
        else if (localName.equalsIgnoreCase(GpxParser.CMT)) {
            final String cmt = builder.toString().trim();

            if (point != null) {
                if ((point.getDescr() == null) || ((point.getDescr().length() < 1))) {
                    point.setDescr(cmt);
                }
            }
            else if (route != null) {
                if (route.lastRoutePoint != null) {
                    if ((route.lastRoutePoint.getDescr() == null) || ((route.lastRoutePoint.getDescr().length() < 1))) {
                        route.lastRoutePoint.setDescr(cmt);
                    }
                }
                else {
                    if ((route.getDescr() == null) || ((route.getDescr().length() < 1))) {
                        route.setDescr(cmt);
                    }
                }
            }
            else if (mTrack != null) {
                if (mTrack.LastTrackPoint != null) {
                    // NYI !!!
                }
                else {
                    if ((mTrack.Descr == null) || ((mTrack.Descr.length() < 1))) {
                        mTrack.Descr = cmt;
                    }
                }
            }
        }
        else if (localName.equalsIgnoreCase(GpxParser.ELE)) {
            final double alt = Double.parseDouble(builder.toString().trim());
            if (route != null) {
                if (route.lastRoutePoint != null) {
                    route.lastRoutePoint.setAlt(alt);
                }
            }
            else if (point != null) {
                point.setAlt(alt);
            }
            else if (mTrack != null) {
                if (mTrack.LastTrackPoint != null) {
                    mTrack.LastTrackPoint.alt = alt;
                }
            }
        }
        else if (localName.equalsIgnoreCase(GpxParser.TIME)) {
            // NYI
        }

        super.endElement(uri, localName, name);
    }
}
