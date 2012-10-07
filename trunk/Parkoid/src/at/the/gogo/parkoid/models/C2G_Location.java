package at.the.gogo.parkoid.models;

// {"location":[
// {"countryCode":"DE",
// "defaultLanguage":"de",
// "locationId":1,
// "locationName":"Ulm",
// "mapSection":{
// "center":{
// "latitude":48.398917,
// "longitude":9.99139
// },"lowerRight":{
// "latitude":48.3446,
// "longitude":10.0459
// },"upperLeft":{
// "latitude":48.4383,
// "longitude":9.9146
// }
// },"timezone":"Europe/Berlin"
// }],
// "returnValue":{
// "code":0,
// "description":"Operation successful."
// }}

public class C2G_Location {

    String   countryCode;
    String   defaultLanguage;
    String   locationName;
    String   loationId;

    Position mapCenter;
    Position lowerRight;
    Position upperLeft;

    String   timezone;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(final String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(final String locationName) {
        this.locationName = locationName;
    }

    public String getLoationId() {
        return loationId;
    }

    public void setLoationId(final String loationId) {
        this.loationId = loationId;
    }

    public Position getMapCenter() {
        return mapCenter;
    }

    public void setMapCenter(final Position mapCenter) {
        this.mapCenter = mapCenter;
    }

    public Position getLowerRight() {
        return lowerRight;
    }

    public void setLowerRight(final Position lowerRight) {
        this.lowerRight = lowerRight;
    }

    public Position getUpperLeft() {
        return upperLeft;
    }

    public void setUpperLeft(final Position upperLeft) {
        this.upperLeft = upperLeft;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

}
