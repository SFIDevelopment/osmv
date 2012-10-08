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

/**
 * a wikipedia article
 * 
 * @author marc@geonames
 */
public class WikipediaArticle {

    private String language;
    private String title;
    private String summary;
    private String wikipediaUrl;
    private String feature;
    private int    population = -1;
    private int    elevation  = -1;
    private double latitude;
    private double longitude;
    private String thumbnailImg;
    private String countryCode;
    private double distance   = -1.0;

    /**
     * @return Returns the elevation.
     */
    public int getElevation() {
        return elevation;
    }

    /**
     * @param elevation
     *            The elevation to set.
     */
    public void setElevation(final int elevation) {
        this.elevation = elevation;
    }

    /**
     * @return Returns the feature.
     */
    public String getFeature() {
        return feature;
    }

    /**
     * @param feature
     *            The feature to set.
     */
    public void setFeature(final String feature) {
        this.feature = feature;
    }

    /**
     * @return Returns the language.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language
     *            The language to set.
     */
    public void setLanguage(final String language) {
        this.language = language;
    }

    /**
     * @return Returns the latitude.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude
     *            The latitude to set.
     */
    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return Returns the longitude.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude
     *            The longitude to set.
     */
    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return Returns the population.
     */
    public int getPopulation() {
        return population;
    }

    /**
     * @param population
     *            The population to set.
     */
    public void setPopulation(final int population) {
        this.population = population;
    }

    /**
     * @return Returns the summary.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary
     *            The summary to set.
     */
    public void setSummary(final String summary) {
        this.summary = summary;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            The title to set.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @return Returns the wikipediaUrl.
     */
    public String getWikipediaUrl() {
        return wikipediaUrl;
    }

    /**
     * @param wikipediaUrl
     *            The wikipediaUrl to set.
     * @param wikipediaURL
     */
    public void setWikipediaUrl(final String wikipediaURL) {
        wikipediaUrl = wikipediaURL;
    }

    /**
     * @return the thumbnailImg
     */
    public String getThumbnailImg() {
        return thumbnailImg;
    }

    /**
     * @param thumbnailImg
     *            the thumbnailImg to set
     */
    public void setThumbnailImg(final String thumbnailImg) {
        this.thumbnailImg = thumbnailImg;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(final double distance) {
        this.distance = distance;
    }
}
