package at.the.gogo.panoramio.panoviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class ImageListActivity extends FragmentActivity implements
		ImageListFragment.Callbacks {

	private boolean mTwoPane;
	ImageManager mImageManager;

	/**
	 * The zoom level the user chose when picking the search area
	 */
	private int mZoom;

	/**
	 * The latitude of the center of the search area chosen by the user
	 */
	private int mLatitudeE6;

	/**
	 * The longitude of the center of the search area chosen by the user
	 */
	private int mLongitudeE6;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_image_list);

		mImageManager = ImageManager.getInstance(this);

		if (findViewById(R.id.image_detail_container) != null) {
			mTwoPane = true;
			((ImageListFragment) getSupportFragmentManager().findFragmentById(
					R.id.image_list)).setActivateOnItemClick(true);
		}

		final Intent i = getIntent();
		mZoom = i.getIntExtra(ImageManager.ZOOM_EXTRA, Integer.MIN_VALUE);
		mLatitudeE6 = i.getIntExtra(ImageManager.LATITUDE_E6_EXTRA,
				Integer.MIN_VALUE);
		mLongitudeE6 = i.getIntExtra(ImageManager.LONGITUDE_E6_EXTRA,
				Integer.MIN_VALUE);

		final float minLong = i.getFloatExtra(ImageManager.LONGITUDE_E6_MIN, 0);
		final float maxLong = i.getFloatExtra(ImageManager.LONGITUDE_E6_MAX, 0);

		final float minLat = i.getFloatExtra(ImageManager.LATITUDE_E6_MIN, 0);
		final float maxLat = i.getFloatExtra(ImageManager.LATITUDE_E6_MAX, 0);

		mImageManager.clear();

		// Start downloading
		mImageManager.load(minLong, maxLong, minLat, maxLat);
	}

	@Override
	public void onItemSelected(final int position, final long id) {

		final PanoramioItem item = mImageManager.get(position);

//		Toast.makeText(this, "Selected: "+position, Toast.LENGTH_LONG).show();
		
		if (mTwoPane) {
			final Bundle arguments = new Bundle();

			arguments.putParcelable(ImageManager.PANORAMIO_ITEM_EXTRA, item);
			arguments.putInt(ImageManager.ZOOM_EXTRA, mZoom);
			arguments.putDouble(ImageManager.LATITUDE_E6_EXTRA, mLatitudeE6);
			arguments.putDouble(ImageManager.LONGITUDE_E6_EXTRA, mLongitudeE6);

			final ImageDetailFragment fragment = new ImageDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.image_detail_container, fragment).commit();

		} else {
			
			final Intent i = new Intent(this, ImageDetailActivity.class);
			i.putExtra(ImageManager.PANORAMIO_ITEM_EXTRA, item);
			i.putExtra(ImageManager.ZOOM_EXTRA, mZoom);
			i.putExtra(ImageManager.LATITUDE_E6_EXTRA, mLatitudeE6);
			i.putExtra(ImageManager.LONGITUDE_E6_EXTRA, mLongitudeE6);

			startActivity(i);
		}
	}
}