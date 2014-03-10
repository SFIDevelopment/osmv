package at.the.gogo.gpxviewer.mapsources;


import java.util.ArrayList;
import java.util.Arrays;

import at.the.gogo.gpxviewer.mapsources.adapter.BaseTileProvider;
import at.the.gogo.gpxviewer.mapsources.adapter.GoogleTileCache;
import at.the.gogo.gpxviewer.mapsources.impl.Microsoft.MicrosoftHybrid;
import at.the.gogo.gpxviewer.mapsources.impl.Microsoft.MicrosoftMaps;
import at.the.gogo.gpxviewer.mapsources.impl.Microsoft.MicrosoftVirtualEarth;
import at.the.gogo.gpxviewer.mapsources.impl.MiscMapSources.OviMaps;
import at.the.gogo.gpxviewer.mapsources.impl.MiscMapSources.YahooMaps;
import at.the.gogo.gpxviewer.mapsources.impl.OsmMapSources;
import at.the.gogo.gpxviewer.mapsources.impl.OsmMapSources.CycleMap;
import at.the.gogo.gpxviewer.mapsources.impl.OsmMapSources.MapQuest;
import at.the.gogo.gpxviewer.mapsources.impl.OsmMapSources.Mapnik;
import at.the.gogo.gpxviewer.mapsources.impl.OsmMapSources.Maps4U;
import at.the.gogo.gpxviewer.mapsources.impl.RegionalMapSources.Bergfex;
import at.the.gogo.gpxviewer.mapsources.impl.RegionalMapSources.MyTopo;
import at.the.gogo.gpxviewer.mapsources.impl.RegionalMapSources.OutdooractiveAustria;
import at.the.gogo.gpxviewer.mapsources.impl.RegionalMapSources.OutdooractiveGermany;
import at.the.gogo.gpxviewer.mapsources.impl.RegionalMapSources.OutdooractiveSouthTyrol;
import at.the.gogo.gpxviewer.mapsources.impl.SovietTopoMap;

public class MapSourcesManager {

    private static final BaseTileProvider[] MAP_SOURCES;

    public MapSourcesManager() {
    }
//    private static MapSource LOCALHOST_TEST_MAPSOURCE_STORE_ON = new LocalhostTestSource(
//            "Localhost (stored)", true);
//    private static MapSource LOCALHOST_TEST_MAPSOURCE_STORE_OFF = new LocalhostTestSource(
//            "Localhost (unstored)", false);

    static {
    	
    	GoogleTileCache memoryTileCache = new GoogleTileCache();
    	
        // MapSourcesUpdater.loadMapSourceProperties();
        MAP_SOURCES = new BaseTileProvider[]{ //
                    //
                    
                    new Mapnik(memoryTileCache),
                    new Bergfex(memoryTileCache),
                    new MicrosoftMaps(memoryTileCache),
                    new MicrosoftVirtualEarth(memoryTileCache),
                    new MicrosoftHybrid(memoryTileCache),
                    new OutdooractiveAustria(memoryTileCache),
                    new YahooMaps(memoryTileCache),
                    new OviMaps(memoryTileCache),
                    new CycleMap(memoryTileCache),
//                    new OsmHikingMap(memoryTileCache),
//                    new OsmHikingMapWithBase(),
//                    new OsmHikingMapWithRelief(),
//                    new OsmPublicTransport(),
//                    new OpenPisteMap(),
//                    new GoogleMapMaker(),
//                    new GoogleMapsKorea(),
                    new OutdooractiveGermany(memoryTileCache),
                    new OutdooractiveSouthTyrol(memoryTileCache), 
//                    new MultimapCom(), 
//                    new MultimapOSUkCom(),
//                    new Cykloatlas(), 
//                    new TerraserverUSA(),
                    new MyTopo(memoryTileCache),
//                    new UmpWawPl(), new DoCeluPL(),
//                    new EmapiPl(), new FreemapSlovakia(), new FreemapSlovakiaHiking(),
//                    new FreemapSlovakiaHikingHillShade(), new Turaterkep(), new NearMap(),
//                    new HubermediaBavaria(), new StatkartTopo2(), new StatkartToporaster2(),
//                    new GoogleMapsChina(), new MicrosoftMapsChina(),
//                    new EniroComMap(), new EniroComAerial(), new EniroComNautical(), new MapplusCh(),
//                    new YandexMap(), new YandexSat(),
//                    new ArcGISSatellite(), new ArcGISStreetMap(), new ArcGISTopoMap(), new Topomapper(),
//                    new ICAOMapsGermany(memoryTileCache),
                    new SovietTopoMap(memoryTileCache),
                    new MapQuest(memoryTileCache),
                    new Maps4U(memoryTileCache),
//                    new OpenSeaMapLayer(),
                    new OsmMapSources.ThunderforestCycle(memoryTileCache),
                    new OsmMapSources.ThunderforestLandscape(memoryTileCache),
                    new OsmMapSources.ThunderforestOutdoors(memoryTileCache)
                };
    }

    public static BaseTileProvider getWeatherOverlay(int serviceId) // see impl
    {
    	return new OsmMapSources.WeatherMap(serviceId,new GoogleTileCache());
    }
    
    private static ArrayList<BaseTileProvider> mapSources = new ArrayList<BaseTileProvider>();
    
    public static ArrayList<BaseTileProvider> getAllMapSources() {
    	
    	if (mapSources.isEmpty())
    	{
    		mapSources.addAll(Arrays.asList(MAP_SOURCES));
    	}
    	
//    	ArrayList<BaseTileProvider> mapSources = new ArrayList<BaseTileProvider>();

    	return mapSources;
    }

//    private static boolean isMapSourceEnabled(String mapSourceName) {
//        boolean enabled = false;
//        if (GlobalConfig.getInstance().getSelectedMaps().isEmpty()) {
//            enabled = true;
//        } else {
//            for (Object o : GlobalConfig.getInstance().getSelectedMaps()) {
//                if (((String) o).equals(mapSourceName)) {
//                    enabled = true;
//                    break;
//                }
//            }
//        }
//        return enabled;
//    }
//
//    public static Vector<MapSource> getEnabledMapSources() {
//
//        Vector<MapSource> mapSources = new Vector<MapSource>();
//
//        for (MapSource ms : Arrays.asList(MAP_SOURCES)) {
//
//            if (isMapSourceEnabled(ms.getName())) {
//                mapSources.add(ms);
//            }
//        }
//        return mapSources;
//    }

//    public static String getDefaultMapSourceName() {
//        return DEFAULT.getName();
//    }
//
//    public static MapSource getSourceByName(String name) {
//        for (MapSource ms : MAP_SOURCES) {
//            if (ms.getName().equals(name)) {
//                return ms;
//            }
//        }
//        return null;
//    }
//    private static final Logger LOG = Logger.getLogger(MapSourcesManager.class.getName());
}
