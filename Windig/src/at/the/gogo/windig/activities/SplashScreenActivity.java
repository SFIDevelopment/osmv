package at.the.gogo.windig.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import at.the.gogo.windig.R;
import at.the.gogo.windig.util.Util;

import com.actionbarsherlock.app.SherlockActivity;

public class SplashScreenActivity extends SherlockActivity {
	protected boolean _active = true;
	protected int _splashTime = 1000;

	final static String SPLASH_KEY = "splashOnVersion";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		final ImageView splashImage = (ImageView) findViewById(R.id.splashImage);

		String version = Util.getAppVersion(this);
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		String prefVersion = prefs.getString(SPLASH_KEY, "");

		boolean showSplash = !prefVersion.equals(version);

		if (showSplash) {

			// update prefs
			{
				final SharedPreferences.Editor editor = prefs.edit();
				editor.putString("SPLASH_KEY", version);
				editor.commit();
			}

			splashImage.post(new Runnable() {
				@Override
				public void run() {
					splashImage.startAnimation(AnimationUtils.loadAnimation(
							SplashScreenActivity.this,
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
						startMainApp();
						// stop();
					}
				}
			};
			splashTread.start();
		} else {
			startMainApp();
		}
	}

	private void startMainApp() {
		finish();

		startActivity(new Intent(SplashScreenActivity.this,
				WindigActivity.class));
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			_active = false;
		}
		return true;
	}
}