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
 * search criteria for web services returning toponyms. The string parameters do
 * not have to be utf8 encoded. The encoding is done transparently in the call
 * to the web service. The main parameter for the search over all fields is the
 * 'q' parameter.
 * 
 * @see WebService#search
 * @see <a href="http://www.geonames.org/export/geonames-search.html">search
 *      webservice documentation< /a>
 * @author marc@geonames
 */
class ToponymSearchCriteria {

    private String       q;
    private String       countryCode;
    private String       continentCode;
    private String       name;
    private String       nameEquals;
    private String       nameStartsWith;
    private String       tag;
    private String       language;
    private Style        style;
    private FeatureClass featureClass;
    private String[]     featureCodes;
    private String       adminCode1;
    private String       adminCode2;
    private String       adminCode3;
    private String       adminCode4;
    private int          maxRows;
    private int          startRow;

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
    public void setCountryCode(final String countryCode) throws InvalidParameterException {
        if ((countryCode != null) && (countryCode.length() != 2)) {
            throw new InvalidParameterException("invalid country code " + countryCode);
        }
        this.countryCode = countryCode;
    }

    /**
     * @return the continentCode
     */
    public String getContinentCode() {
        return continentCode;
    }

    /**
     * @param continentCode
     *            the continentCode to set
     */
    public void setContinentCode(final String continentCode) {
        this.continentCode = continentCode;
    }

    /**
     * @return Returns the nameEquals.
     */
    public String getNameEquals() {
        return nameEquals;
    }

    /**
     * @param nameEquals
     *            The nameEquals to set.
     * @param exactName
     */
    public void setNameEquals(final String exactName) {
        nameEquals = exactName;
    }

    /**
     * @return Returns the featureCodes.
     */
    public String[] getFeatureCodes() {
        return featureCodes;
    }

    /**
     * @param featureCodes
     *            The featureCodes to set.
     */
    public void setFeatureCodes(final String[] featureCodes) {
        this.featureCodes = featureCodes;
    }

    public void setFeatureCode(final String featureCode) {
        featureCodes = new String[] { featureCode };
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
     * @return Returns the maxRows.
     */
    public int getMaxRows() {
        return maxRows;
    }

    /**
     * @param maxRows
     *            The maxRows to set.
     */
    public void setMaxRows(final int maxRows) {
        this.maxRows = maxRows;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * search over the name field only.
     * 
     * @param name
     *            The name to set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return Returns the q.
     */
    public String getQ() {
        return q;
    }

    /**
     * The main search term. The search is executed over all fields (place name,
     * country name, admin names, etc)
     * 
     * @param q
     *            The q to set.
     */
    public void setQ(final String q) {
        this.q = q;
    }

    /**
     * @return Returns the startRow.
     */
    public int getStartRow() {
        return startRow;
    }

    /**
     * @param startRow
     *            The startRow to set.
     */
    public void setStartRow(final int startRow) {
        this.startRow = startRow;
    }

    /**
     * @return Returns the style.
     */
    public Style getStyle() {
        return style;
    }

    /**
     * @param style
     *            The style to set.
     */
    public void setStyle(final Style style) {
        this.style = style;
    }

    /**
     * @return Returns the tag.
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param tag
     *            The tag to set.
     */
    public void setTag(final String tag) {
        this.tag = tag;
    }

    /**
     * @return Returns the nameStartsWith.
     */
    public String getNameStartsWith() {
        return nameStartsWith;
    }

    /**
     * @param nameStartsWith
     *            The nameStartsWith to set.
     */
    public void setNameStartsWith(final String nameStartsWith) {
        this.nameStartsWith = nameStartsWith;
    }

    /**
     * @return the featureClass
     */
    public FeatureClass getFeatureClass() {
        return featureClass;
    }

    /**
     * @param featureClass
     *            the featureClass to set
     */
    public void setFeatureClass(final FeatureClass featureClass) {
        this.featureClass = featureClass;
    }

    /**
     * @return the adminCode1
     */
    public String getAdminCode1() {
        return adminCode1;
    }

    /**
     * @param adminCode1
     *            the adminCode1 to set
     */
    public void setAdminCode1(final String adminCode1) {
        this.adminCode1 = adminCode1;
    }

    /**
     * @return the adminCode2
     */
    public String getAdminCode2() {
        return adminCode2;
    }

    /**
     * @param adminCode2
     *            the adminCode2 to set
     */
    public void setAdminCode2(final String adminCode2) {
        this.adminCode2 = adminCode2;
    }

    /**
     * @return the adminCode3
     */
    public String getAdminCode3() {
        return adminCode3;
    }

    /**
     * @param adminCode3
     *            the adminCode3 to set
     */
    public void setAdminCode3(final String adminCode3) {
        this.adminCode3 = adminCode3;
    }

    public String getAdminCode4() {
        return adminCode4;
    }

    public void setAdminCode4(final String adminCode4) {
        this.adminCode4 = adminCode4;
    }
}
