package at.the.gogo.panoramio.panoviewer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import at.the.gogo.panoramio.panoviewer.R;
import at.the.gogo.panoramio.panoviewer.R.id;
import at.the.gogo.panoramio.panoviewer.R.layout;

public class SplashScreenActivity extends Activity {
	protected boolean _active = true;
	protected int _splashTime = 1000;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		final ImageView splashImage = (ImageView) findViewById(R.id.splashImage);

		splashImage.post(new Runnable() {
			@Override
			public void run() {
				splashImage.startAnimation(AnimationUtils
						.loadAnimation(SplashScreenActivity.this,
								android.R.anim.slide_in_left)); // R.anim.splash
			}
		});

		// thread for displaying the SplashScreen
		final Thread splashTread = new Thread() {
			@Override
			public void run() {
				try {
					int waited = 0;
					while (_active && (waited < _splashTime)) {
						sleep(100);
						if (_active) {
							waited += 100;
						}
					}
				} catch (final InterruptedException e) {
					// do nothing
				} finally {
					finish();

					startActivity(new Intent(SplashScreenActivity.this,
							PanoViewerActivity.class));
					// stop();
				}
			}
		};
		splashTread.start();
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			_active = false;
		}
		return true;
	}
}