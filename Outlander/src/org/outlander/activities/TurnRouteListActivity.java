package org.outlander.activities;

import org.andnav.osm.util.GeoPoint;
import org.outlander.R;
import org.outlander.fragments.TurnRouteListFragment;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TurnRouteListActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        GeoPoint target = null;
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            final double lat = bundle.getDouble("lat");
            final double lon = bundle.getDouble("lon");

            target = new GeoPoint(lat, lon);
        }
        else {
            target = CoreInfoHandler.getInstance().getCurrentTarget();
        }

        // Override how this activity is animated into view
        // This has to be called before onCreate
        // overridePendingTransition(R.anim.pull_in_from_left, R.anim.hold);
        overridePendingTransition(R.anim.push_left_in, R.anim.hold);

        final TurnRouteListFragment fragment = new TurnRouteListFragment(target);
        // fragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
    }

    @Override
    protected void onPause() {

        overridePendingTransition(R.anim.hold, R.anim.push_out_to_left);
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();

        setupActionBar();
    }

    private void setupActionBar() {

        if (Ut.isMultiPane(this)) {
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

            getSupportActionBar().setDisplayUseLogoEnabled(false);

        }
        else {
            getSupportActionBar().hide();
        }
    }

}
