package at.the.gogo.windig.fragments;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import at.the.gogo.windig.R;
import at.the.gogo.windig.dto.WindEntry;
import at.the.gogo.windig.dto.WindEntryHolder;
import at.the.gogo.windig.notifications.DataImportReady;
import at.the.gogo.windig.notifications.NotificationManager;
import at.the.gogo.windig.util.CoreInfoHolder;
import at.the.gogo.windig.util.WindInfoHandler;
import at.the.gogo.windig.util.speech.SpeakItOut;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieGraph.OnSliceClickedListener;
import com.echo.holographlibrary.PieSlice;

public class OverviewFragment extends SherlockFragment {

	/** View to bind. */
	View view;

	Context activity;

	static Typeface mFont;

	private int activeSite = 0;
	private boolean refresh = false;
	private List<WindEntry> entries = null;

	PieGraph pieGraphView;
	View backgroundView;
	TextView directionView;
	TextView maxSpeedTextView;
	TextView maxSpeedIconView;
	TextView maxTempTextView;
	TextView maxTempIconView;
	TextView speedView;
	TextView conditionsView;

	public final static String WEATHER_FONT = "fonts/Climacons.ttf";

	public final static int FONT_CHAR_INFO_HUM = 0x0066;
	public final static int FONT_CHAR_INFO_TEMP = 0x005C;
	public final static int FONT_CHAR_INFO_WIND = 0x0042;
	
	
	public static Typeface getTypeface(Context context, String typeface) {
		if (mFont == null) {
			mFont = Typeface.createFromAsset(context.getAssets(), typeface);
		}
		return mFont;
	}

	public static OverviewFragment newInstance(final int activeSiteIx) {
		final OverviewFragment f = new OverviewFragment();

		f.activeSite = activeSiteIx;

		// Supply num input as an argument.
		// Bundle args = new Bundle();
		// args.putInt("num", num);
		// f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		WerteListFragment.site_title = getResources().getStringArray(
				R.array.site_url_title);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		final SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		//
		activeSite = Integer.parseInt(pref.getString("pref_site", "0"));

		final View view = inflater.inflate(R.layout.overview, container, false);

		backgroundView = view.findViewById(R.id.windig_overview);

		pieGraphView = (PieGraph) view.findViewById(R.id.piegraph);

		pieGraphView.setOnSliceClickedListener(new OnSliceClickedListener() {

			@Override
			public void onClick(int index) {
				// NOOP
			}

		});

		directionView = (TextView) view.findViewById(R.id.direction);

		View includeView = view.findViewById(R.id.conditioncell1);

		maxSpeedIconView = (TextView) includeView
				.findViewById(R.id.conditionIcon);
		maxSpeedIconView.setTypeface(getTypeface(getActivity(), WEATHER_FONT));
		maxSpeedIconView.setText(""+((char)FONT_CHAR_INFO_WIND));

		maxSpeedTextView = (TextView) includeView
				.findViewById(R.id.conditionText);

		includeView = view.findViewById(R.id.conditioncell2);

		maxTempIconView = (TextView) includeView
				.findViewById(R.id.conditionIcon);
		maxTempIconView.setTypeface(getTypeface(getActivity(), WEATHER_FONT));
		maxTempIconView.setText(""+((char)FONT_CHAR_INFO_TEMP));
		
		maxTempTextView = (TextView) includeView
				.findViewById(R.id.conditionText);

		speedView = (TextView) view.findViewById(R.id.speed);
		
		conditionsView = (TextView) view.findViewById(R.id.conditions);
		
		return view;
	}

	private void speakit() {
		entries = WindEntryHolder.getInstance().getEntries(activeSite, false);

		if ((entries != null) && (entries.size() > 0)) {
			if ((refresh) && (CoreInfoHolder.getInstance().isSpeakit())) {
				SpeakItOut.speak(WerteListFragment.makeText(getActivity(),
						entries, activeSite, entries.size() - 1, true));
			}
		} else {
			Toast.makeText(getActivity(), R.string.no_data, Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		super.onOptionsItemSelected(item);

		boolean result = false;

		switch (item.getItemId()) {
		case R.id.speakIt: {
			refresh = true;
			speakit();
			result = true;
			break;
		}
		}
		return result;
	}

	@Override
	public void onResume() {
		fillData(null);
		super.onResume();
	}

	public void fillData(GraphFragment graphPage) {

		final GetData asyncTask = new GetData();

		asyncTask.execute(activeSite);

	}

	public class GetData extends AsyncTask<Integer, Void, List<WindEntry>> {

		@Override
		protected List<WindEntry> doInBackground(final Integer... params) {

			entries = WindEntryHolder.getInstance().getEntries(params[0],
					refresh);

			refresh = false;
			return entries;
		}

		@Override
		protected void onPostExecute(final List<WindEntry> entries) {

			onPostExecuteGetData(entries);

			if (entries != null) {
				NotificationManager.post(new DataImportReady(entries.size()));
			}
		}
	}

	void onPostExecuteGetData(final List<WindEntry> entries) {

		if ((entries == null) || (entries.size() < 1)) {
			return;
		}

		final SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		//
		final int conversionIx = Integer.parseInt(pref.getString("pref_unit",
				"0"));

		String speedText[] = getResources().getStringArray(
				R.array.speed_unit_title);

		String conditions[] = getResources().getStringArray(
				R.array.tts_conditions);
		
		// fill views

		// icon max
		// icon temp
		// speed -
		// dir

		WindEntry windEntry = entries.get(entries.size()-1);  // last is newest...

		int direction = WindInfoHandler.getArrowIndex(Integer
				.parseInt(windEntry.getValue(3)));

		backgroundView.setBackgroundColor(getResources().getColor(
				R.color.orange_dark));

		pieGraphView.setStartingAngle(247);
		pieGraphView.setAnimate(true);
		pieGraphView.removeSlices();

		for (int i = 0; i < 8; i++) {
			PieSlice slice = new PieSlice();
			slice.setColor(getResources().getColor(R.color.white));
			slice.setValue(1);

			slice.setObaque(i == direction );

			pieGraphView.addSlice(slice);
		}

		int speed = WindInfoHandler.convertSpeed(
				Double.parseDouble(windEntry.getValue(2).replace(",", ".")),
				conversionIx);

		int maxSpeed = WindInfoHandler.convertSpeed(
				Double.parseDouble(windEntry.getValue(4).replace(",", ".")),
				conversionIx);

		// maxSpeedIconView;
		maxSpeedTextView.setText(WerteListFragment.decimalFormat.format(maxSpeed)
				.replace(".", ",") + " " + speedText[conversionIx]);

		speedView.setText(WerteListFragment.decimalFormat.format(speed)
				.replace(".", ","));
		
		int temp = (int) Math.rint(Double.parseDouble(windEntry.getValue(5)
				.replace(",", ".")));
		
		maxTempTextView.setText(temp + " " + WindInfoHandler.postfixs[5]);

		directionView.setText(getResources().getStringArray(
				R.array.wind_directions)[direction]);

		// maxTempIconView

		int directionColorIx = WindInfoHandler.getColorIndexOnSpeedAndDegree(
				Double.parseDouble(windEntry.getValue(4).replace(",", ".")),
				Integer.parseInt(windEntry.getValue(3).replace(",", ".")),
				WindInfoHandler.minDegrees[activeSite],
				WindInfoHandler.maxDegrees[activeSite]);

		int color;

		switch (directionColorIx) {
		case 0:
			color = getResources().getColor(R.color.red1);
			break;
		case 1:
			color = getResources().getColor(R.color.green);
			break;
		default:
		case 2:
			color = getResources().getColor(R.color.yellow);
			break;
		}

		backgroundView.setBackgroundColor(color);

		conditionsView.setText(conditions[directionColorIx]);
	}
}
