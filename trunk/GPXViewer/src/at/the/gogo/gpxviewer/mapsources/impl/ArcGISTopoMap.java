/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.the.gogo.gpxviewer.mapsources.impl;

import at.the.gogo.gpxviewer.mapsources.adapter.BaseTileProvider;
import at.the.gogo.gpxviewer.mapsources.adapter.GoogleTileCache;

/**
 * http://www.arcgis.com/home/webmap/viewer.html?useExisting=1
 */
public class ArcGISTopoMap extends BaseTileProvider {

    private static final String BASE_URL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/";

    public ArcGISTopoMap(GoogleTileCache tileCache) {
//        super("ArcGISTopoMap", 0, 19, "jpg", TileUpdate.IfModifiedSince);
    	super(0, 19, tileCache);
    }

    @Override
    public String getMapName() {
    	return "ArcGISTopoMap";
    }
    
    @Override
    public String getTileUrlString(int x, int y,int zoom) {
        return BASE_URL + zoom + "/" + y + "/" + x;
    }

    @Override
    public String toString() {
        return "ArcGIS Topo Map";
    }    
}
