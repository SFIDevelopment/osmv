package org.outlander.model;

import org.outlander.constants.DBConstants;

public class RouteCategory {

    private final int Id;
    public String     Title;
    public String     Description;
    public boolean    Hidden;
    public int        IconId;
    public int        MinZoom;

    public RouteCategory(final int id, final String title, final String descr, final boolean hidden, final int iconId, final int minZoom) {
        super();
        Id = id;
        Title = title;
        Description = descr;
        Hidden = hidden;
        IconId = iconId;
    }

    public RouteCategory() {
        this(DBConstants.EMPTY_ID, "", "", false, 0, 14);
    }

    public RouteCategory(final String title) {
        this(DBConstants.EMPTY_ID, title, "", false, 0, 14);
    }

    public int getId() {
        return Id;
    }

}
