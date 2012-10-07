package at.the.gogo.parkoid.models;

import java.util.Date;

public class Sms {

    private String name;
    private String text;
    private int    id = -1;
    private Date   date;

    public Sms(final String name, final String text) {
        this.name = name;
        this.text = text;
        date = new Date();
    }

    public Sms(final int id, final String name, final String text,
            final Date date) {
        this.id = id;
        this.name = name;
        this.text = text;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }
}
