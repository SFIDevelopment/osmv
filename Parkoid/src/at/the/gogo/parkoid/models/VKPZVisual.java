package at.the.gogo.parkoid.models;

import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.drawable.Drawable;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.ZoneChecker;

public class VKPZVisual extends OverlayItem {

    private final ViennaKurzParkZone vkpz;

    public VKPZVisual(final ViennaKurzParkZone vkpz) {

        super(vkpz.getId(), getTitle(vkpz), vkpz.getProperties().get("dauer")
                + " " + vkpz.getProperties().get("zeitraum"), vkpz.getPolygon()
                .get(vkpz.getPolygon().size() % 2));
        this.vkpz = vkpz;
    }

    private static String getTitle(final ViennaKurzParkZone vkpz) {
        String title = "";
        if (CoreInfoHolder.getInstance().getVKPZCurrentList() != null) {
            for (final String key : CoreInfoHolder.getInstance()
                    .getVKPZCurrentList().keySet()) {
                final ViennaKurzParkZone vkpz1 = CoreInfoHolder.getInstance()
                        .getVKPZCurrentList().get(key);

                if (vkpz.getId() == vkpz1.getId()) {
                    title = key;
                }
            }
        }
        return title;
    }

    @Override
    public Drawable getMarker(final int stateBitset) {
        final Drawable marker = CoreInfoHolder
                .getInstance()
                .getContext()
                .getResources()
                .getDrawable(
                        ZoneChecker.isActive(vkpz) ? R.drawable.parking_marker_pay
                                : R.drawable.parking_marker);
        setMarker(marker);
        return marker;
    }

    @Override
    public HotspotPlace getMarkerHotspot() {

        return HotspotPlace.BOTTOM_CENTER;
    }

}
