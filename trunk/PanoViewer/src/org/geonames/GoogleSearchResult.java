package org.geonames;

import java.util.ArrayList;
import java.util.List;

class GoogleSearchResult {

    // TODO from UCDetector: Change visibility of Field
    // "GoogleSearchResult.googleResults" to private
    List<GoogleResult> googleResults = new ArrayList<GoogleResult>(); // NO_UCD

    Style              style;

    /**
     * @return Returns the toponyms.
     */
    public List<GoogleResult> getGoogleResults() {
        return googleResults;
    }

    /**
     * @param toponyms
     *            The toponyms to set.
     */
    public void setGoogleResults(final List<GoogleResult> googleResults) {
        this.googleResults = googleResults;
    }

    /**
     * @return Returns the totalResultsCount.
     */
    public int getTotalResultsCount() {
        return googleResults.size();
    }

}
