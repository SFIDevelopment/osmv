package at.the.gogo.parkoid.map;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.C2G_Vehicle;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.webservices.C2G_Services;

public class C2G_CarOverlay extends ParkingCarOverlay {

    boolean                  enabled;
    long                     lastrefresh;

    final public static long MINAGE = 60000;

    public static C2G_CarOverlay overlayFactory(final Context context) {
        return new C2G_CarOverlay(context, new ArrayList<ParkingCarItem>());
    }

    protected C2G_CarOverlay(final Context pContext,
            final List<ParkingCarItem> pList) {
        super(pContext, pList);

        CoreInfoHolder.getInstance().setParkingCarOverlay(this);
        // setFocusItemsOnTap(true);
        // setFocusedItem(0);
    }

    @Override
    public void refresh() {

        if ((lastrefresh + MINAGE) > System.currentTimeMillis()) {
            return;
        } else {
            lastrefresh = System.currentTimeMillis();
        }
        if (marker == null) {
            marker = CoreInfoHolder.getInstance().getContext().getResources()
                    .getDrawable(R.drawable.car_avail);
        }

        final List<C2G_Vehicle> vehicles = C2G_Services.getFreeVehicles("wien");
        removeAllItems();

        if ((vehicles != null) && (vehicles.size() > 0)) {

            for (final C2G_Vehicle vehicle : vehicles) {
                final ParkingCarItem parking = new ParkingCarItem(
                        vehicle.getName(), vehicle.getAddress(), new GeoPoint(
                                vehicle.getPosition().getLatitude(), vehicle
                                        .getPosition().getLongitude()), marker);
                addItem(parking);
            }
        }
    }

    public void enable(final boolean enable) {
        enabled = enable;
    }

}
