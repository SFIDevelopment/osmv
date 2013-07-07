package at.the.gogo.windig.fragments;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.the.gogo.windig.R;
import at.the.gogo.windig.dto.WindEntry;
import at.the.gogo.windig.dto.WindEntryHolder;
import at.the.gogo.windig.util.CoreInfoHolder;
import at.the.gogo.windig.util.WindInfoHandler;
import at.the.gogo.windig.util.speech.SpeakItOut;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class WerteListFragment extends SherlockListFragment {

	int mPositionChecked = 0;
	int mPositionShown = -1;
	// private View headerView;
	// private TextView mDescription;
	// private TextView mHeader;

	private boolean isInitialized = false;
	private List<WindEntry> entries = null;
	// private long refreshed = System.currentTimeMillis();
	private boolean refresh = false;
	private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
	private int activeSite = 0;
	public static String[] site_title;

	public static WerteListFragment newInstance(final int activeSiteIx) {
		final WerteListFragment f = new WerteListFragment();

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

		restoreSavedState(savedInstanceState);
		final SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		//
		activeSite = Integer.parseInt(pref.getString("pref_site", "0"));

		final View view = super.onCreateView(inflater, container,
				savedInstanceState);
		// headerView = addHeader(inflater);
		return view;
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.main_option_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
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

	private void restoreSavedState(final Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mPositionChecked = savedInstanceState.getInt("curChoiceList", 0);
			mPositionShown = savedInstanceState.getInt("shownChoiceList", -1);
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		restoreSavedState(savedInstanceState);

		if (!isInitialized) {
			// getListView().addHeaderView(headerView);
			// mQuickAction = new QuickAction(getActivity());
			// setupQuickAction(mQuickAction);
			isInitialized = true;
		}

		// getListView().setCacheColorHint(Color.TRANSPARENT);

		// if (mHasDetailsFrame) {
		// In dual-pane mode, the list view highlights the selected item.
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		// Make sure our UI is in the correct state.
		// showDetails(mPositionChecked);
		// }
	}

	@Override
	public void onResume() {
		fillData();
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("curChoiceList", mPositionChecked);
		outState.putInt("shownChoiceList", mPositionShown);
	}

	public void refreshData(final boolean forceRefresh) {
		refresh = forceRefresh;
		fillData();
	}

	public void fillData() {

		final GetData asyncTask = new GetData();
		asyncTask.execute(activeSite);
//		speakit();
	}

	@Override
	public void onListItemClick(final ListView l, final View v,
			final int position, final long id) {

		final String text = makeText((entries.size() - 1) - position, false);
		// String text = makeText(position, false);

		shareIt(text);
	}

	private void shareIt(final String text) {
		final Intent sharingIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		final String shareBody = text;

		sharingIntent
				.putExtra(
						android.content.Intent.EXTRA_SUBJECT,
						"Wind Information: "
								+ getResources().getStringArray(
										R.array.site_url_title)[activeSite]);
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

		startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}

	private String makeText(final int index, final boolean forSpeach) {

		final WindEntry entry = entries.get(index);

		final SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		final int conversionIx = Integer.parseInt(pref.getString("pref_unit",
				"0"));

		// header
		String text = getText(R.string.tts_weather_station).toString()
				+ " "
				+ getResources().getStringArray(R.array.site_url_title)[activeSite];

		if (!forSpeach) {// time
			text += " [" + entry.getValue(0) + " " + entry.getValue(1) + "]";
		}
		// wind speed + direction
		text += "  "
				+ getText(R.string.tts_windspeed).toString()
				+ " "
				+ entry.getValue(4).replace(",", ".")
				+ " "
				+ getResources().getStringArray(
						forSpeach ? R.array.speed_unit_title_tts
								: R.array.speed_unit_title)[conversionIx]
				+ "  "
				+ getText(R.string.tts_windspeed_max).toString()
				+ " "
				+ entry.getValue(2).replace(",", ".")
				+ " "
				+ getResources().getStringArray(
						forSpeach ? R.array.speed_unit_title_tts
								: R.array.speed_unit_title)[conversionIx]
				+ "  "
				+ getText(R.string.tts_winddir).toString()
				+ " "
				+ entry.getValue(3)
				+ " "
				+ getText(R.string.tts_grad).toString()
				+ "  "
				+ getResources().getStringArray(R.array.wind_directions)[WindInfoHandler
						.getArrowIndex(Integer.parseInt(entry.getValue(3)))];
		// temp
		text += "  " + getText(R.string.tts_temp).toString() + " "
				+ entry.getValue(5).replace(",", ".") + " "
				+ getText(R.string.tts_grad).toString();

		// text += " " +
		// getText(R.string.tts_weather_when).toString()+" "+((FragmentActivity)
		// (getActivity())).getSupportActionBar().getSubtitle().toString();

		// OK to fly or not......

		return text;
	}

	private void speakit() {
		entries = WindEntryHolder.getInstance().getEntries(activeSite, false);

		if ((entries != null) && (entries.size() > 0)) {
			if ((refresh) && (CoreInfoHolder.getInstance().isSpeakit())) {
				SpeakItOut.speak(makeText(entries.size() - 1, true));
			}
		} else {
			Toast.makeText(getActivity(), R.string.no_data, Toast.LENGTH_SHORT)
					.show();
		}
	}

	public static class ViewHolder {
		public TextView[] textView = new TextView[7];

		ImageView icon1;
		ImageView icon2;
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

			final String newHeaderDescr =
			// "Records "
			// + ((entries != null) ? entries.size() : 0)
			// +
			// "last refresh :"
			// +
			DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
					DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE
							| DateUtils.FORMAT_SHOW_TIME
							| DateUtils.FORMAT_24HOUR);

			// mDescription.setText(newHeaderDescr);

			// if (mHeader != null) {
			// mHeader.setText(site_title[activeSite]);
			// }

			((SherlockFragmentActivity) (getActivity())).getSupportActionBar()
					.setTitle(WerteListFragment.site_title[activeSite]);
			((SherlockFragmentActivity) (getActivity())).getSupportActionBar()
					.setSubtitle(newHeaderDescr);

			final SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			//
			final int conversionIx = Integer.parseInt(pref.getString(
					"pref_unit", "0"));

			final ListAdapter adapter = new BaseAdapter() {

				private LayoutInflater inflater = null;
				String speedText[] = getResources().getStringArray(
						R.array.speed_unit_title);

				@Override
				public int getCount() {
					return (entries != null) ? entries.size() : 0;// searchResult.getTotalResultsCount();
				}

				@Override
				public long getItemId(final int position) {
					return (entries.size() - 1) - position;
				}

				private final int[] textFieldIds = { R.id.text1, R.id.text2,
						R.id.text3, R.id.text4, R.id.text5, R.id.text6,
						R.id.text7 };

				@Override
				public View getView(int position, View convertView,
						final ViewGroup parent) {

					position = (entries.size() - 1) - position;

					ViewHolder holder = null;

					if (convertView == null) {
						if (inflater == null) {
							inflater = LayoutInflater.from(getActivity());
						}
						convertView = inflater
								.inflate(R.layout.list_item, null);
						holder = new ViewHolder();

						for (int i = 0; i < holder.textView.length; i++) {
							holder.textView[i] = (TextView) convertView
									.findViewById(textFieldIds[i]);
						}

						holder.icon1 = (ImageView) convertView
								.findViewById(R.id.ImageView01);
						convertView.setTag(holder);

					} else {
						holder = (ViewHolder) convertView.getTag();
					}

					final WindEntry windEntry = entries.get(position);

					for (int i = 0; i < holder.textView.length; i++) {
						String text;
						if (windEntry.getValue(i) != null) {
							if ((i == 2) || (i == 4)) {
								final double speed = WindInfoHandler
										.convertSpeed(Double
												.parseDouble(windEntry
														.getValue(i).replace(
																",", ".")),
												conversionIx);
								text = decimalFormat.format(speed).replace(".",
										",")
										+ " " + speedText[conversionIx];

							} else {
								text = windEntry.getValue(i);
							}
							text += " " + WindInfoHandler.postfixs[i];
						} else {
							text = "unknown";
						}
						holder.textView[i].setText(text);
					}

					holder.icon1.setImageResource(WindInfoHandler
							.getIconBasedInSpeedAndDegree(Double
									.parseDouble(windEntry.getValue(4).replace(
											",", ".")), Integer
									.parseInt(windEntry.getValue(3).replace(
											",", ".")),
									WindInfoHandler.minDegrees[activeSite],
									WindInfoHandler.maxDegrees[activeSite]));

					// speedcolor
					holder.textView[4].setTextColor(WindInfoHandler
							.getSpeedColor(Double.parseDouble(windEntry
									.getValue(4).replace(",", "."))));

					holder.textView[2].setTextColor(WindInfoHandler
							.getSpeedColor(Double.parseDouble(windEntry
									.getValue(2).replace(",", "."))));

					// if (position % 2 == 0) {
					// convertView.setBackgroundColor(R.color.oddlist);
					// } else {
					// convertView.setBackgroundColor(R.color.evenlist);
					// }

					return convertView;
				}

				@Override
				public Object getItem(final int position) {
					return entries.get(position);
				}
			};

			setListAdapter(adapter);
			
			speakit();
		}
	}
}
