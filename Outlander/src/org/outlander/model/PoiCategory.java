package org.outlander.model;

import org.outlander.R;
import org.outlander.constants.DBConstants;

public class PoiCategory {

    private final int Id;
    public String     Title;
    public String     Descr;
    public boolean    Hidden;
    public int        IconId;
    public int        MinZoom;

    public PoiCategory(final int id, final String title, final boolean hidden, final int iconid, final int minzoom, final String descr) {
        super();
        Id = id;
        Title = title;
        Hidden = hidden;
        IconId = iconid;
        MinZoom = minzoom;
        Descr = descr;
    }

    public PoiCategory() {
        this(DBConstants.EMPTY_ID, "", false, R.drawable.poi, 14, "");
    }

    public PoiCategory(final String title) {
        this(DBConstants.EMPTY_ID, title, false, R.drawable.poi, 14, "");
    }

    public int getId() {
        return Id;
    }

}
