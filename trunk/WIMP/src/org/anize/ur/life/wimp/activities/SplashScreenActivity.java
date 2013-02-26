package org.anize.ur.life.wimp.activities;

import org.anize.ur.life.wimp.R;
import org.anize.ur.life.wimp.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreenActivity extends Activity {
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
		final SharedPreferences prefs = getSharedPreferences(
				MainSettingActivity.PREFERENCES, 0);

		String prefVersion = prefs.getString(SPLASH_KEY, "");

		boolean showSplash = !prefVersion.equals(version);

		if (showSplash) {
			Editor editor = prefs.edit();
			editor.putString(SPLASH_KEY, version);
			editor.commit();

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
						startMainApp(); // stop();
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
				MainSettingActivity.class));
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			_active = false;
		}
		return true;
	}
}