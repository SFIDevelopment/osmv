package at.the.gogo.parkoid.activities;

import android.os.Bundle;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.fragments.MapFragment;
import at.the.gogo.parkoid.util.CoreInfoHolder;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MapActivity extends SherlockFragmentActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (CoreInfoHolder.getInstance().getContext() == null) {
            CoreInfoHolder.getInstance().setContext(this);
        }
        super.onCreate(savedInstanceState);
        // Override how this activity is animated into view
        // This has to be called before onCreate
        // overridePendingTransition(R.anim.pull_in_from_left, R.anim.hold);
        overridePendingTransition(R.anim.rotate_in, R.anim.hold);

        // setContentView(R.layout.activity_sliding_drawer);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new MapFragment()).commit();
        }

    }

    @Override
    protected void onPause() {
        // Whenever this activity is paused (i.e. looses focus because another
        // activity is started etc)
        // Override how this activity is animated out of view
        overridePendingTransition(R.anim.hold, R.anim.rotate_out);
        super.onPause();
    }

    // @Override
    // public void finish() {
    // // we need to override this to performe the animtationOut on each
    // // finish.
    // ActivitySwitcher.animationOut(findViewById(R.id.container),
    // getWindowManager(), new ActivitySwitcher.AnimationFinishedListener() {
    // @Override
    // public void onAnimationFinished() {
    // MapActivity.super.finish();
    // // disable default animation
    // overridePendingTransition(0, 0);
    // }
    // });
    // }
}
