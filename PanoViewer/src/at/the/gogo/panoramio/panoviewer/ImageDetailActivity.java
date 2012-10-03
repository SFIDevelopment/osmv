package at.the.gogo.panoramio.panoviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class ImageDetailActivity extends FragmentActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_detail);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		final Intent i = getIntent();

		final ImageDetailFragment fragment = new ImageDetailFragment();

		fragment.setArguments(i.getExtras());

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.image_detail_container, fragment).commit();
		
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpTo(this, new Intent(this,
					PanoViewerActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
