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
 * a postal code
 * 
 * @author marc@geonames
 */
class PostalCode {

    private String postalCode;
    private String placeName;
    private String countryCode;
    private double latitude;
    private double longitude;
    private String adminName1;
    private String adminCode1;
    private String adminName2;
    private String adminCode2;
    private double distance;

    /**
     * @return Returns the distance.
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @param distance
     *            The distance to set.
     */
    public void setDistance(final double distance) {
        this.distance = distance;
    }

    /**
     * @return Returns the adminCode1.
     */
    public String getAdminCode1() {
        return adminCode1;
    }

    /**
     * @param adminCode1
     *            The adminCode1 to set.
     */
    public void setAdminCode1(final String adminCode1) {
        this.adminCode1 = adminCode1;
    }

    /**
     * @return Returns the adminCode2.
     */
    public String getAdminCode2() {
        return adminCode2;
    }

    /**
     * @param adminCode2
     *            The adminCode2 to set.
     */
    public void setAdminCode2(final String adminCode2) {
        this.adminCode2 = adminCode2;
    }

    /**
     * @return Returns the adminName1.
     */
    public String getAdminName1() {
        return adminName1;
    }

    /**
     * @param adminName1
     *            The adminName1 to set.
     */
    public void setAdminName1(final String adminName1) {
        this.adminName1 = adminName1;
    }

    /**
     * @return Returns the adminName2.
     */
    public String getAdminName2() {
        return adminName2;
    }

    /**
     * @param adminName2
     *            The adminName2 to set.
     */
    public void setAdminName2(final String adminName2) {
        this.adminName2 = adminName2;
    }

    /**
     * @return Returns the ISO 3166-1-alpha-2 countryCode.
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * @param countryCode
     *            The ISO 3166-1-alpha-2 countryCode to set.
     */
    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * latitude in WGS84
     * 
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
     * longitude in WGS84
     * 
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
     * @return Returns the placeName.
     */
    public String getPlaceName() {
        return placeName;
    }

    /**
     * @param placeName
     *            The placeName to set.
     */
    public void setPlaceName(final String placeName) {
        this.placeName = placeName;
    }

    /**
     * @return Returns the postalCode.
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * @param postalCode
     *            The postalCode to set.
     */
    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }
}
