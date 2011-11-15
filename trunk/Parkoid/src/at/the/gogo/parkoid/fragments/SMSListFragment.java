package at.the.gogo.parkoid.fragments;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuInflater;
import android.support.v4.view.MenuItem;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.Sms;
import at.the.gogo.parkoid.util.CoreInfoHolder;

public class SMSListFragment extends ListFragment implements PageChangeNotifyer {

	int mPositionChecked = 0;
	int mPositionShown = -1;
	Sms smsSelected;
	String tableName;

	private boolean isInitialized = false;

	public static SMSListFragment newInstance(final String tableName) {
		final SMSListFragment f = new SMSListFragment();
		f.tableName = tableName;

		return f;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		restoreSavedState(savedInstanceState);

		final View view = inflater.inflate(R.layout.headerlist, null);

		final ImageView icon = (ImageView) view.findViewById(R.id.parkButton);
		final TextView header = (TextView) view
				.findViewById(R.id.currentAddress);

		icon.setImageResource(R.drawable.sms_in);
		icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				getActivity().openOptionsMenu();
			}
		});

		header.setText(R.string.page_title_sms);
		header.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				getActivity().openOptionsMenu();
			}
		});

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu,
			android.view.MenuInflater inflater) {
		inflater.inflate(R.menu.sms_option_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	private void restoreSavedState(final Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mPositionChecked = savedInstanceState.getInt("curChoiceList", 0);
			mPositionShown = savedInstanceState.getInt("shownChoiceList", -1);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {

		final int id = (int) ((AdapterView.AdapterContextMenuInfo) menuInfo).id;
		smsSelected = CoreInfoHolder.getInstance().getDbManager()
				.getSMS(id, tableName);

		menu.setHeaderTitle("an/von " + smsSelected.getName());

		menu.add(0, R.id.menu_deletesms, 0, getText(R.string.menu_deletesms));

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
		case R.id.deleteallsms: {
			deleteAllSMS();
			break;
		}
		}

		return true;
	}

	@Override
	public boolean onContextItemSelected(final android.view.MenuItem item) {

		boolean result = false;
		switch (item.getItemId()) {
		case R.id.menu_deletesms: {
			deleteSMS(smsSelected.getId());
			result = true;
			break;
		}
		}
		return result;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		restoreSavedState(savedInstanceState);

		if (!isInitialized) {

			// NOOP for now
			isInitialized = true;
		}
		registerForContextMenu(getListView());

		getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
	}

	@Override
	public void onResume() {
		resume();
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("curChoiceList", mPositionChecked);
		outState.putInt("shownChoiceList", mPositionShown);
	}

	public void refreshData(final boolean forceRefresh) {
		// refresh = forceRefresh;
		fillData();
	}

	public void fillData() {

		final GetDataTask asyncTask = new GetDataTask();
		asyncTask.execute((Void) null);
	}

	// final static String[] COLUMNS = { "name", "text", "date" };
	// final static int[] FIELDS = { R.id.smsName, R.id.smsText, R.id.smsDate };

	public class GetDataTask extends AsyncTask<Void, Void, List<Sms>> {

		@Override
		protected List<Sms> doInBackground(final Void... params) {

			// Cursor cursor = CoreInfoHolder.getInstance()
			// .getDbManager(getActivity()).getDatabase()
			// .getSMSListCursor(tableName);
			// getActivity().startManagingCursor(cursor);
			// return cursor;

			final List<Sms> smsList = CoreInfoHolder.getInstance()
					.getDbManager().getSMSList(tableName);

			return smsList;

		}

		@Override
		protected void onPostExecute(final List<Sms> entries) {

			// if (cursor != null) {
			// setListAdapter(new SimpleCursorAdapter(getActivity(),
			// R.layout.sms_item, cursor, COLUMNS, FIELDS));
			// }

			final ListAdapter adapter = new BaseAdapter() {

				private LayoutInflater inflater = null;

				@Override
				public int getCount() {
					return (entries != null) ? entries.size() : 0;
				}

				@Override
				public long getItemId(final int position) {
					return position;
				}

				@Override
				public View getView(final int position, View convertView,
						final ViewGroup parent) {

					ViewHolder holder = null;

					if (convertView == null) {
						if (inflater == null) {
							inflater = LayoutInflater.from(getActivity());
						}
						convertView = inflater.inflate(R.layout.sms_item, null);

						holder = new ViewHolder();

						holder.textView1 = (TextView) convertView
								.findViewById(R.id.smsName);
						holder.textView2 = (TextView) convertView
								.findViewById(R.id.smsText);
						holder.textView3 = (TextView) convertView
								.findViewById(R.id.smsDate);

						holder.icon1 = (ImageView) convertView
								.findViewById(R.id.imageView1);

						convertView.setTag(holder);

					} else {
						holder = (ViewHolder) convertView.getTag();
					}

					final Sms sms = entries.get(position);

					final String date = DateUtils.formatDateTime(getActivity(),
							sms.getDate().getTime(),
							DateUtils.FORMAT_SHOW_WEEKDAY
									| DateUtils.FORMAT_SHOW_DATE
									| DateUtils.FORMAT_SHOW_TIME
									| DateUtils.FORMAT_24HOUR);

					holder.textView1.setText(sms.getName());
					holder.textView2.setText(sms.getText());
					holder.textView3.setText(date);

					return convertView;
				}

				@Override
				public Object getItem(final int position) {
					return entries.get(position);
				}
			};
			setListAdapter(adapter);
		}
	}

	public static class ViewHolder {
		TextView textView1;
		TextView textView2;
		TextView textView3;
		ImageView icon1;
		ImageView icon2;
	}

	private void deleteSMS(final int id) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		builder.setMessage(R.string.warning_delete_sms)
				.setCancelable(false)
				.setPositiveButton(R.string.dialogYES,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {

								// CoreInfoHolder.getInstance()
								// .getDbManager(getActivity()).beginTransaction();

								CoreInfoHolder
										.getInstance()
										.getDbManager()
										.deleteSMS(smsSelected.getId(),
												tableName);
								// CoreInfoHolder.getInstance()
								// .getDbManager(getActivity()).commitTransaction();

								dialog.dismiss();
								fillData();
							}
						})
				.setNegativeButton(R.string.dialogNO,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();

	}

	private void deleteAllSMS() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		builder.setMessage(R.string.warning_delete_all_sms)
				.setCancelable(false)
				.setPositiveButton(R.string.dialogYES,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {

								CoreInfoHolder.getInstance().getDbManager()
										.deleteAllSMS(tableName);

								dialog.dismiss();
								fillData();
							}
						})
				.setNegativeButton(R.string.dialogNO,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();

	}

	private void pause() {
		// NOOP for now
	}

	private void resume() {
		fillData();
	}

	@Override
	public void pageGetsActivated() {
		resume();

	}

	@Override
	public void pageGetsDeactivated() {
		pause();
	}

}
