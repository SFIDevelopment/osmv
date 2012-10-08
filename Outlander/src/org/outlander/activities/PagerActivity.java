package org.outlander.activities;

import org.outlander.R;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;
import org.outlander.views.ViewPagerHelper;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class PagerActivity extends SherlockFragmentActivity {

    private int startPage = -1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            startPage = bundle.getInt("PageInFlipper");
        }

        // Override how this activity is animated into view
        // This has to be called before onCreate
        // overridePendingTransition(R.anim.pull_in_from_left, R.anim.hold);
        overridePendingTransition(R.anim.push_left_in, R.anim.hold);

        setContentView(new ViewPagerHelper().getViewPagerView(this));
    }

    @Override
    protected void onPause() {

        final SharedPreferences uiState = getPreferences(0);
        final SharedPreferences.Editor editor = uiState.edit();

        if (CoreInfoHandler.getInstance().getViewPager() != null) {
            editor.putInt("PageInFlipper", CoreInfoHandler.getInstance().getViewPager().getCurrentItem());
        }

        editor.commit();

        // Whenever this activity is paused (i.e. looses focus because another
        // activity is started etc)
        // Override how this activity is animated out of view
        overridePendingTransition(R.anim.hold, R.anim.push_out_to_left);
        super.onPause();
    }

    @Override
    protected void onResume() {

        int page;
        if (startPage == -1) {
            final SharedPreferences uiState = getPreferences(0);
            page = uiState.getInt("PageInFlipper", 0);
        }
        else {
            page = startPage;
        }

        if (CoreInfoHandler.getInstance().getViewPager() != null) {
            CoreInfoHandler.getInstance().getViewPager().setCurrentItem(page); // TODO:
                                                                               // ???
                                                                               // move
        }

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
