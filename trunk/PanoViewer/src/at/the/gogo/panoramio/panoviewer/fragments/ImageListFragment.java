package at.the.gogo.panoramio.panoviewer.fragments;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import at.the.gogo.panoramio.panoviewer.ImageAdapter;
import at.the.gogo.panoramio.panoviewer.ImageManager;
import at.the.gogo.panoramio.panoviewer.R;
import at.the.gogo.panoramio.panoviewer.R.layout;

public class ImageListFragment extends ListFragment {

	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private Callbacks mCallbacks = sDummyCallbacks;
	private int mActivatedPosition = ListView.INVALID_POSITION;

	public interface Callbacks {

		public void onItemSelected(int position, long id);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(final int position, final long id) {
		}
	};

	/**
	 * Observer used to turn the progress indicator off when the
	 * {@link ImageManager} is done downloading.
	 */
	private final DataSetObserver mObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			if (!mImageManager.isLoading()) {
				getActivity().getWindow().setFeatureInt(
						Window.FEATURE_INDETERMINATE_PROGRESS,
						Window.PROGRESS_VISIBILITY_OFF);
			}
		}

		@Override
		public void onInvalidated() {
		}
	};

	ImageManager mImageManager;

	public ImageListFragment() {
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mImageManager = ImageManager.getInstance(getActivity());
		
		if (mImageManager.isLoading()) {
			getActivity().getWindow().setFeatureInt(
					Window.FEATURE_INDETERMINATE_PROGRESS,
					Window.PROGRESS_VISIBILITY_ON);
			mImageManager.addObserver(mObserver);
		}
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
		else
		{
			final ListView listView = getListView();
			final LayoutInflater inflater = getLayoutInflater(savedInstanceState);
			final View footer = inflater.inflate(R.layout.list_footer, listView,
					false);

			
			getListView().addFooterView(footer, null, false);
			
			// Theme.Light sets a background on our list.
			 listView.setBackgroundDrawable(null);

			
			
		}
		setListAdapter(new ImageAdapter(getActivity()));
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(final ListView listView, final View view,
			final int position, final long id) {
		super.onListItemClick(listView, view, position, id);
		mCallbacks.onItemSelected(position, id);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != AdapterView.INVALID_POSITION) {
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	public void setActivateOnItemClick(final boolean activateOnItemClick) {
		getListView().setChoiceMode(
				activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE
						: AbsListView.CHOICE_MODE_NONE);
	}

	public void setActivatedPosition(final int position) {
		if (position == AdapterView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}
		mActivatedPosition = position;
	}
}
