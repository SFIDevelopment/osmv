package at.the.gogo.parkoid.models;

public class Car {

    private String name;
    private String licence;
    private int    id = -1;

    public Car(final String name, final String licence) {
        this.name = name;
        this.licence = licence;
    }

    public Car(final int id, final String name, final String licence) {
        this.id = id;
        this.name = name;
        this.licence = licence;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLicence() {
        return licence;
    }

    public void setLicence(final String licence) {
        this.licence = licence;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

}
