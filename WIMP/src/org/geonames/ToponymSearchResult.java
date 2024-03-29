/*
 * Copyright 2008 Marc Wick, geonames.org Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.geonames;

import java.util.ArrayList;
import java.util.List;

/**
 * a toponym search result as returned by the geonames webservice.
 * 
 * @author marc@geonames
 */
public class ToponymSearchResult {

    List<Toponym> toponyms = new ArrayList<Toponym>();
    private int   totalResultsCount;                  // NO_UCD

    private Style style;                              // NO_UCD

    /**
     * @return Returns the toponyms.
     */
    public List<Toponym> getToponyms() {
        return toponyms;
    }

    /**
     * @param toponyms
     *            The toponyms to set.
     */
    public void setToponyms(final List<Toponym> toponyms) {
        this.toponyms = toponyms;
    }

    /**
     * @return Returns the totalResultsCount.
     */
    public int getTotalResultsCount() {
        return totalResultsCount;
    }

    /**
     * @param totalResultsCount
     *            The totalResultsCount to set.
     */
    public void setTotalResultsCount(final int totalResultsCount) {
        this.totalResultsCount = totalResultsCount;
    }

    /**
     * @return the style
     */
    public Style getStyle() {
        return style;
    }

    /**
     * @param style
     *            the style to set
     */
    public void setStyle(final Style style) {
        this.style = style;
    }
}
