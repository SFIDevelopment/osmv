// http://datamoil.blogspot.com/2011/05/cloudmade-routing-on-osmdroid.html
package at.the.gogo.parkoid.map;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.overlay.PathOverlay;

import android.content.Context;

// http://developers.cloudmade.com/projects/routing-http-api/examples/js-response

public class RouteOverlay extends PathOverlay {

    public RouteOverlay(final int color, final ResourceProxy pResourceProxy) {
        super(color, pResourceProxy);
        // TODO Auto-generated constructor stub
    }

    public RouteOverlay(final int color, final Context ctx) {
        super(color, ctx);
        // TODO Auto-generated constructor stub
    }
}
