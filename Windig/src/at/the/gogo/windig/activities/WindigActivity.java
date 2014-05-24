/*
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package at.the.gogo.windig.activities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Window;
import at.the.gogo.windig.R;
import at.the.gogo.windig.fragments.GraphFragment;
import at.the.gogo.windig.fragments.OverviewFragment;
import at.the.gogo.windig.fragments.WerteListFragment;
import at.the.gogo.windig.util.CoreInfoHolder;
import at.the.gogo.windig.util.CrashReportHandler;
import at.the.gogo.windig.util.Util;
import at.the.gogo.windig.util.speech.SpeakItOut;
import at.the.gogo.windig.widget.WindigWidgetProvider;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.TitleProvider;

public class WindigActivity extends SherlockFragmentActivity implements
		TextToSpeech.OnInitListener {

	static final int NUM_ITEMS = 3;
	private final static int PREF_ID = 123;
	final static String POSKEY = "index";
	private static final int MY_TTS_CHECK_CODE = 1234;

	Fragment pages[] = new Fragment[WindigActivity.NUM_ITEMS];
	int currentPage = 0;
	int wertelistId = 0;

	private MyPagerAdapter mPagerAdapter;
	private ViewPager mViewPager;

	private LinePageIndicator mIndicator;
	boolean wantToUseTTS = false;

	@Override
	public void onAttachedToWindow() {

		super.onAttachedToWindow();
		final Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final int applicationFlags = getApplicationInfo().flags;
		if ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
			CrashReportHandler.attach(this);
		}
		// now switch debug mode off if
		Util.DEBUGMODE = ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);

		final SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		wantToUseTTS = sharedPreferences.getBoolean("pref_tts_speech", true);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(WindigActivity.POSKEY)) {
				currentPage = savedInstanceState.getInt(WindigActivity.POSKEY);
			}
		}

		setContentView(R.layout.windigmain);
		getSupportActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		setTitle(R.string.wetterwertetitel);
		getSupportActionBar().setDisplayUseLogoEnabled(false);

		mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.viewflipper);
		mViewPager.setAdapter(mPagerAdapter);

		mIndicator = (LinePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);
//		mIndicator.setFooterIndicatorStyle(IndicatorStyle.Underline);

		int lastpageViewed = sharedPreferences.getInt("PageInFlipper", 0);

		if (savedInstanceState != null) {
			lastpageViewed = savedInstanceState.getInt("index", lastpageViewed);
		}
		mViewPager.setCurrentItem(lastpageViewed);

		// Set the indicator as the pageChangeListener
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrolled(final int position,
					final float positionOffset, final int positionOffsetPixels) {
				mIndicator.onPageScrolled(position, positionOffset,
						positionOffsetPixels);
			}

			@Override
			public void onPageSelected(final int position) {
				mIndicator.onPageSelected(position);
			}

			@Override
			public void onPageScrollStateChanged(final int state) {
				mIndicator.onPageScrollStateChanged(state);
			}
		});

		restoreUIState();
		checkTTS();
	}

	private void checkTTS() {
		// Fire off an intent to check if a TTS engine is installed
		// final Intent checkIntent = new Intent();
		// checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		// startActivityForResult(checkIntent,
		// WindigActivity.MY_TTS_CHECK_CODE);

		// directly instantiate TTS
		if (wantToUseTTS) {
			CoreInfoHolder.getInstance().setTts(new TextToSpeech(this, this));
		}

	}

	private void restoreUIState() {
		final SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);

		if (settings.getString("error", "").length() > 0) {
			showDialog(R.id.error);
		}

		if (!settings.getString("app_version", "").equalsIgnoreCase(
				Util.getAppVersion(this))) {
			showDialog(R.id.whatsnew);
			final SharedPreferences.Editor editor = settings.edit();
			editor.putString("app_version", Util.getAppVersion(this));
			editor.commit();
		}
	}

	@Override
	protected void onPause() {

		final SharedPreferences uiState = PreferenceManager
				.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = uiState.edit();
		editor.putInt("PageInFlipper", mViewPager.getCurrentItem());

		super.onPause();
	}

	@Override
	protected void onStop() {
		if (CoreInfoHolder.getInstance().isSpeakit()) {
			SpeakItOut.speak(getText(R.string.tts_bye).toString());
		}
		if (CoreInfoHolder.getInstance().getTts() != null) {
			CoreInfoHolder.getInstance().getTts().stop();
			CoreInfoHolder.getInstance().getTts().shutdown();
		}
		super.onStop();
	}

	// @Override
	// protected void onSaveInstanceState(final Bundle outState) {
	// super.onSaveInstanceState(outState);
	// outState.putInt("index", mViewPager.getCurrentItem());
	// }

	public class MyPagerAdapter extends FragmentPagerAdapter implements
			TitleProvider {

		public MyPagerAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return WindigActivity.NUM_ITEMS;
		}

		@Override
		public Fragment getItem(final int position) {
			if (pages[position] == null) {
				switch (position) {
				case 0:
					pages[position] = OverviewFragment.newInstance(0);
					break;
				case 1:
					pages[position] = WerteListFragment.newInstance(0);
					wertelistId = pages[position].getId();
					break;
				case 2:
					pages[position] = GraphFragment.newInstance(0,
							GraphFragment.PAGE_TEMPSPEED);
					break;
				case 3:
					pages[position] = GraphFragment.newInstance(0,
							GraphFragment.PAGE_DIRECTION);
					break;
				}
			}
			return pages[position];

		}

		@Override
		public String getTitle(final int pos) {
			return getResources().getStringArray(R.array.page_titles)[pos];

		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		super.onOptionsItemSelected(item);

		boolean result = false;

		switch (item.getItemId()) {
		case android.R.id.home: {
			mViewPager.setCurrentItem(0);
			result = true;
			break;
		}
		case R.id.refresh: {

			// Fragment fragment = getSupportFragmentManager()
			// .findFragmentById(wertelistId);
			if (pages[1] != null) {
				((WerteListFragment) pages[1]).refreshData(true,
						((GraphFragment) pages[2]));
			}
			// if (pages[1] != null) {
			// ((GraphFragment) pages[1]).refreshData(true);
			// }
			// broadcast event
			sendBroadcast(new Intent(WindigWidgetProvider.APPWIDGET_UPDATE));

			// } else {
			// Util.dd("refresh failed: fragment not found...");
			// }
			result = true;
			break;
		}
		case R.id.settings: {
			startActivityForResult(new Intent(this,
					MainPreferenceActivity.class), WindigActivity.PREF_ID);
			result = true;
			break;
		}
		case R.id.about: {
			if (CoreInfoHolder.getInstance().isSpeakit()) {
				SpeakItOut.speak(getText(R.string.tts_welcome).toString());
			}
			showDialog(R.id.about);
			result = true;
			break;
		}

		}
		return result;
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		if (requestCode == WindigActivity.MY_TTS_CHECK_CODE) {
			// if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
			// // success, create the TTS instance
			// CoreInfoHolder.getInstance().setTts(
			// new TextToSpeech(this, this));
			// } else {
			// // missing data, install it
			//
			// Util.e("Speech data missing:" + resultCode);
			//
			// final Intent installIntent = new Intent();
			// installIntent
			// .setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
			// startActivity(installIntent);
			// }
		} else { // we come from preferences
			finish();
			startActivity(new Intent(this, this.getClass()));
		}

		// }
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {

		case R.id.whatsnew:
			return new AlertDialog.Builder(this)
					// .setIcon( R.drawable.alert_dialog_icon)
					.setTitle(R.string.about_dialog_whats_new)
					.setMessage(R.string.whats_new_dialog_text)
					.setNegativeButton(R.string.about_dialog_close,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int whichButton) {

									if ((CoreInfoHolder.getInstance()
											.isSpeakit())) {
										SpeakItOut.speak(getText(
												R.string.tts_bye).toString());
									}

								}
							}).create();
		case R.id.about:
			if ((CoreInfoHolder.getInstance().isSpeakit())) {
				SpeakItOut.speak(getText(R.string.tts_about).toString());
			}
			return new AlertDialog.Builder(this)
					// .setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.menu_about)
					.setMessage(
							getText(R.string.app_name) + " v."
									+ Util.getAppVersion(this) + "\n\n"
									+ getText(R.string.about_dialog_text))
					.setPositiveButton(R.string.about_dialog_whats_new,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int whichButton) {

									showDialog(R.id.whatsnew);
								}
							})
					.setNegativeButton(R.string.about_dialog_close,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int whichButton) {

									/* User clicked Cancel so do some stuff */
								}
							}).create();
		case R.id.error:
			return new AlertDialog.Builder(this)
					// .setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.error_title)
					.setMessage(getText(R.string.error_text))
					.setPositiveButton(R.string.error_send,
							new DialogInterface.OnClickListener() {
								@Override
								@SuppressWarnings("static-access")
								public void onClick(
										final DialogInterface dialog,
										final int whichButton) {

									final SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
									String text = settings.getString("error",
											"");
									String subj = getText(R.string.app_name)
											+ " runtime error: ";
									try {
										final String[] lines = text.split("\n",
												2);
										final Pattern p = Pattern
												.compile("[.][\\w]+[:| |\\t|\\n]");
										final Matcher m = p.matcher(lines[0]
												+ "\n");
										if (m.find()) {
											subj += m.group().replace(".", "")
													.replace(":", "")
													.replace("\n", "")
													+ " at ";
										}
										final Pattern p2 = Pattern
												.compile("[.][\\w]+[(][\\w| |\\t]*[)]");
										final Matcher m2 = p2.matcher(lines[1]);
										if (m2.find()) {
											subj += m2.group().substring(2);
										}
									} catch (final Exception e) {
									}

									final Build b = new Build();
									final Build.VERSION v = new Build.VERSION();
									text = "Your message:"
											+ "\n\n"
											+ getText(R.string.app_name)
											+ ": "
											+ Util.getAppVersion(WindigActivity.this)
											+ "\nAndroid: " + v.RELEASE
											+ "\nDevice: " + b.BOARD + " "
											+ b.BRAND + " " + b.DEVICE + " "
											+ b.MANUFACTURER + " " + b.MODEL
											+ " " + b.PRODUCT + "\n\n" + text;

									startActivity(Util.sendErrorReportMail(
											subj, text));
									Util.e(text);
									final SharedPreferences uiState = getPreferences(0);
									final SharedPreferences.Editor editor = uiState
											.edit();
									editor.putString("error", "");
									editor.commit();

								}
							})
					.setNegativeButton(R.string.about_dialog_close,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int whichButton) {

									final SharedPreferences uiState = getPreferences(0);
									final SharedPreferences.Editor editor = uiState
											.edit();
									editor.putString("error", "");
									editor.commit();
								}
							}).create();

		}
		return null;
	}

	// for TTS
	@Override
	public void onInit(final int status) {
		if (status == TextToSpeech.SUCCESS) {
			CoreInfoHolder.getInstance().setSpeakit(wantToUseTTS); // wanted &
																	// installed
		}
		// if (CoreInfoHolder.getInstance().isSpeakit()) {
		// SpeakItOut.speak(getText(R.string.tts_welcome).toString());
		// }

	}

}
