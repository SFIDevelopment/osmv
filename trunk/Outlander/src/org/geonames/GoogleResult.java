package org.geonames;

import org.json.JSONException;
import org.json.JSONObject;
import org.outlander.utils.Ut;

// [{"region":"Lower Austria",
// "streetAddress":"2486 Pottendorf",
// "titleNoFormatting":"2486 Pottendorf",
// "staticMapUrl":"http:\/\/maps.google.com\/maps\/api\/staticmap?maptype=roadmap&format=gif&sensor=false&size=150x100&zoom=13&markers=47.91149,16.38802",
// "listingType":"local",
// "addressLines":["2486 Pottendorf","Austria"],
// "lng":"16.38802",
// "url":"http:\/\/www.google.com\/maps?source=uds&q=Pottendorf",
// "country":"AT",
// "city":"Pottendorf",
// "GsearchResultClass":"GlocalSearch",
// "maxAge":604800,
// "addressLookupResult":"\/maps",
// "title":"2486 Pottendorf",
// "postalCode":"",
// "ddUrlToHere":"http:\/\/www.google.com\/maps?source=uds&daddr=2486+Pottendorf,+Pottendorf,+Lower+Austria+(2486+Pottendorf)+@47.91149,16.38802&iwstate1=dir:to",
// "ddUrl":"http:\/\/www.google.com\/maps?source=uds&daddr=2486+Pottendorf,+Pottendorf,+Lower+Austria+(2486+Pottendorf)+@47.91149,16.38802&saddr=48.208174,16.373819",
// "ddUrlFromHere":"http:\/\/www.google.com\/maps?source=uds&saddr=2486+Pottendorf,+Pottendorf,+Lower+Austria+(2486+Pottendorf)+@47.91149,16.38802&iwstate1=dir:from",
// "accuracy":"4",
// "lat":"47.91149"
// }]
//

class GoogleResult {
    private String streetAddress;
    private String titleNoFormatting;
    private String staticMapUrl;
    private String listingType;
    private String addressLines;
    private String url;
    private String country;
    private String city;
    private String GsearchResultClass;
    private String addressLookupResult;
    private String title;
    private String postalCode;
    private String ddUrlToHere;
    private String ddUrl;
    private String ddUrlFromHere;
    private int    accuracy;
    private double lat;
    private double lng;

    private GoogleResult() {
    }

    static GoogleResult fromJSonElement(final JSONObject element) {
        final GoogleResult result = new GoogleResult();

        try {
            result.setAccuracy(element.getInt("accuracy"));
            result.setAddressLines(element.getString("addressLines"));
            result.setAddressLookupResult(element
                    .getString("addressLookupResult"));
            result.setCity(element.getString("city"));
            result.setDdUrl(element.getString("ddUrl"));
            result.setDdUrlFromHere(element.getString("ddUrlFromHere"));
            result.setDdUrlToHere(element.getString("ddUrlToHere"));
            result.setGsearchResultClass(element
                    .getString("gsearchResultClass"));
            result.setLat(element.getDouble("lat"));
            result.setListingType(element.getString("listingType"));
            result.setLng(element.getDouble("lng"));
            result.setPostalCode(element.getString("postalCode"));
            result.setStaticMapUrl(element.getString("staticMapUrl"));
            result.setStreetAddress(element.getString("streetAddress"));
            result.setTitle(element.getString("title"));
            result.setTitleNoFormatting(element.getString("titleNoFormatting"));
            result.setUrl(element.getString("url"));
        } catch (final JSONException e) {
            //
            // e.printStackTrace();
            Ut.dd(e.toString());
        }

        return result;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(final String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getTitleNoFormatting() {
        return titleNoFormatting;
    }

    public void setTitleNoFormatting(final String titleNoFormatting) {
        this.titleNoFormatting = titleNoFormatting;
    }

    public String getStaticMapUrl() {
        return staticMapUrl;
    }

    public void setStaticMapUrl(final String staticMapUrl) {
        this.staticMapUrl = staticMapUrl;
    }

    public String getListingType() {
        return listingType;
    }

    public void setListingType(final String listingType) {
        this.listingType = listingType;
    }

    public String getAddressLines() {
        return addressLines;
    }

    public void setAddressLines(final String addressLines) {
        this.addressLines = addressLines;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getGsearchResultClass() {
        return GsearchResultClass;
    }

    public void setGsearchResultClass(final String gsearchResultClass) {
        GsearchResultClass = gsearchResultClass;
    }

    public String getAddressLookupResult() {
        return addressLookupResult;
    }

    public void setAddressLookupResult(final String addressLookupResult) {
        this.addressLookupResult = addressLookupResult;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDdUrlToHere() {
        return ddUrlToHere;
    }

    public void setDdUrlToHere(final String ddUrlToHere) {
        this.ddUrlToHere = ddUrlToHere;
    }

    public String getDdUrl() {
        return ddUrl;
    }

    public void setDdUrl(final String ddUrl) {
        this.ddUrl = ddUrl;
    }

    public String getDdUrlFromHere() {
        return ddUrlFromHere;
    }

    public void setDdUrlFromHere(final String ddUrlFromHere) {
        this.ddUrlFromHere = ddUrlFromHere;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(final int accuracy) {
        this.accuracy = accuracy;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(final double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(final double lng) {
        this.lng = lng;
    }

}
