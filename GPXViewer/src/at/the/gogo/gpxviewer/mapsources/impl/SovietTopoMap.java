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
public class SovietTopoMap extends BaseTileProvider {

	private static final String BASE_URL = "http://maps2.atlogis.com/cgi-bin/tilecache-2.11/tilecache.py/1.0.0/topomapper_gmerc/";

	public SovietTopoMap(GoogleTileCache tileCache) {
		super(5, 13, tileCache);
	}

	@Override
	public String getTileUrlString(int tilex, int tiley, int zoom) {

		return BASE_URL + zoom + "/" + tilex + "/" + tiley;
	}

	@Override
	public String getMapName() {
		return "Soviet Military Topo Maps";
	}

}
