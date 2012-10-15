package at.the.gogo.windig.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;
import at.the.gogo.windig.R;
import at.the.gogo.windig.widget.UpdateWidgetService;
import at.the.gogo.windig.widget.WindigWidgetModel;

public class WidgetConfigActivity extends Activity {
	// private static String tag = "ConfigureWindigWidgetActivity";
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_widget);
		setupButton();

		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

	}

	private void setupButton() {
		final Button b = (Button) findViewById(R.id.button_update);
		b.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(final View v) {
				parentButtonClicked(v);
			}
		});

	}

	// ?????
	private void parentButtonClicked(final View v) {
		final String name = getSite();
		final String date = getSpeed();

		updateAppWidgetLocal(name, date);
		final Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(Activity.RESULT_OK, resultValue);
		finish();
	}

	private String getSite() {

		final Spinner spinner = (Spinner) findViewById(R.id.spinnerTitles);
		final String site = "" + spinner.getSelectedItemId();
		return site;
	}

	private String getSpeed() {
		final Spinner spinner = (Spinner) findViewById(R.id.spinnerSpeeds);
		final String speed = "" + spinner.getSelectedItemId();
		return speed;
	}

	private void updateAppWidgetLocal(final String site, final String speed) {
		final WindigWidgetModel m = new WindigWidgetModel(mAppWidgetId, site,
				speed);
		m.savePreferences(this);
		updateAppWidget(this, AppWidgetManager.getInstance(this), m);

	}

	public static void updateAppWidget(final Context context,
			final AppWidgetManager appWidgetManager,
			final WindigWidgetModel widgetModel) {

		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget);

		final Intent intent = new Intent(context.getApplicationContext(),
				UpdateWidgetService.class);

		// intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
		// Integer.parseInt(widgetModel.getSite()));
		final int ids[] = { widgetModel.iid };
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		final PendingIntent pendingIntent = PendingIntent.getActivity(context,
				0 /*
				 * no requestCode
				 */, intent, 0 /* no flags */);
		views.setOnClickPendingIntent(R.id.refreshButton, pendingIntent);

		// Update the widgets via the service
		context.startService(intent);

		// Tell the widget manager
		appWidgetManager.updateAppWidget(widgetModel.iid, views);
	}
}