package org.outlander.views.util;

public class f {
    private final int    a;
    private final String b;
    private final String c;

    public f(final int paramInt, final String paramString) {
        this(paramInt, null, paramString);
    }

    public f(final int paramInt, final String paramString1,
            final String paramString2) {
        a = paramInt;
        b = paramString1;
        c = paramString2;
    }

    public int a() {
        return a;
    }

    public String b() {
        return c;
    }

    public String c() {
        String str;
        switch (a) {
            default:
                str = "";
                break;
            case 1:
                str = "m";
                break;
            case 2:
                str = "km";
                break;
            case 3:
                str = "km/h";
                break;
            case 4:
                str = "°";
                break;
            case 5:
                str = "m/s²";
                break;
            case 11:
                str = "mi";
                break;
            case 12:
                str = "mph";
                break;
            case 23:
                str = b;
        }
        return str;
    }

    @Override
    public boolean equals(final Object paramObject) {
        int j = 1;
        int i = 0;
        if ((paramObject != null) && ((paramObject instanceof f))) {
            final f localf = (f) paramObject;
            if (((localf.b == null) || (b != null))
                    && ((localf.b != null) || (b == null))) {
                if ((localf.b != null) || (b != null)) {
                    if ((localf.a != a) || (!localf.c.equals(c))
                            || (!localf.b.equals(b))) {
                        j = 0;
                    }
                    i = j;
                } else {
                    if ((localf.a != a) || (localf.c != localf.c)) {
                        j = 0;
                    }
                    i = j;
                }
            }
        }
        return (i > 0);
    }

    @Override
    public String toString() {
        final StringBuilder localStringBuilder = new StringBuilder(b());
        localStringBuilder.append(c());
        return localStringBuilder.toString();
    }
}
