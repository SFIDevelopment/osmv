package at.the.gogo.panoramio.panoviewer;

import java.util.regex.Pattern;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageDetailFragment extends Fragment {

	PanoramioItem mItem;

	private Handler mHandler;

	private ImageView mImage;

	private TextView mTitle;

	private TextView mOwner;
	private TextView mOwnerUrl;

	private View mContent;

	public ImageDetailFragment() {

	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler();

		Bundle arguments = getArguments();

		mItem = arguments.getParcelable(ImageManager.PANORAMIO_ITEM_EXTRA);

		// getActivity().getWindow().setFeatureInt(
		// Window.FEATURE_INDETERMINATE_PROGRESS,
		// Window.PROGRESS_VISIBILITY_ON);

	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.view_image, container,
				false);

		mContent = rootView.findViewById(R.id.content);
		mImage = (ImageView) rootView.findViewById(R.id.image);
		mTitle = (TextView) rootView.findViewById(R.id.title);
		mOwner = (TextView) rootView.findViewById(R.id.owner);
		mOwnerUrl = (TextView) rootView.findViewById(R.id.ownerUrl);
		mContent.setVisibility(View.GONE);

		mImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity();
				DownloadManager dm = (DownloadManager) getActivity()
						.getSystemService(Context.DOWNLOAD_SERVICE);

				String uri = mItem.getThumbUrl();
				uri = uri.replace("thumbnail", "medium"); // original ?

				Request request = new Request(Uri.parse(uri));
				long enqueue = dm.enqueue(request);

				Toast.makeText(getActivity(), "Url: " + uri, Toast.LENGTH_LONG)
						.show();
			}
		});

		new LoadThread().start();

		return rootView;
	}

	private void setAsLink(TextView view, String url){
        Pattern pattern = Pattern.compile(url);
        Linkify.addLinks(view, pattern, "http://");
        view.setText(Html.fromHtml("<a href='<"+url+"'>"+url+"</a>"));
    }

	/**
	 * Utility to load a larger version of the image in a separate thread.
	 * 
	 */
	private class LoadThread extends Thread {

		public LoadThread() {
		}

		@Override
		public void run() {
			try {
				String uri = mItem.getThumbUrl();
				uri = uri.replace("thumbnail", "medium"); // original ?
				final Bitmap b = BitmapUtils.loadBitmap(uri);
				mHandler.post(new Runnable() {
					@Override
					public void run() {

						mImage.setImageBitmap(b);
						mTitle.setText(mItem.getTitle());
						mOwner.setText(mItem.getOwner());
						setAsLink(mOwnerUrl,mItem.getOwnerUrl());
						mContent.setVisibility(View.VISIBLE);

						// getActivity().getWindow().setFeatureInt(
						// Window.FEATURE_INDETERMINATE_PROGRESS,
						// Window.PROGRESS_VISIBILITY_OFF);
					}
				});
			} catch (final Exception e) {
				Log.e("ImageLoader", e.toString());
			}
		}
	}
}
