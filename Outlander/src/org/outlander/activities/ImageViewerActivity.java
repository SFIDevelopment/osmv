package org.outlander.activities;

import java.util.List;

import org.outlander.R;
import org.outlander.model.PanoramioItem;
import org.outlander.utils.CoreInfoHandler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher.ViewFactory;

public class ImageViewerActivity extends Activity implements ViewFactory {

    ImageSwitcher iSwitcher;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.imageviewer);

        // Bundle urlsInfo = getIntent().getExtras();
        //
        // if (urlsInfo != null) {
        // urls = urlsInfo.getStringArray("imgurls");
        // }

        iSwitcher = (ImageSwitcher) findViewById(R.id.ImageSwitcher01);
        iSwitcher.setFactory(this);
        iSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        iSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

        final Gallery gallery = (Gallery) findViewById(R.id.Gallery01);

        if (CoreInfoHandler.getInstance().getPanoramioItems() != null) {
            gallery.setAdapter(new ImageAdapter(this));
        }

        gallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1, final int index, final long arg3) {
                // TODO: optimize !!
                iSwitcher.setImageDrawable(new BitmapDrawable(getImage(index, true)));
            }
        });
    }

    private Bitmap getImage(final int index, final boolean thumbOrOri) {
        Bitmap image = null;
        final List<PanoramioItem> panoItems = CoreInfoHandler.getInstance().getPanoramioItems();

        if (panoItems != null) {
            image = (thumbOrOri ? panoItems.get(index).getImage() : panoItems.get(index).getOriginalImage());
        }
        return image;
    }

    public class ImageAdapter extends BaseAdapter {

        private final Context ctx;

        public ImageAdapter(final Context c) {
            ctx = c;
        }

        @Override
        public int getCount() {
            return CoreInfoHandler.getInstance().getPanoramioItems().size();
        }

        @Override
        public Object getItem(final int arg0) {

            return arg0;
        }

        @Override
        public long getItemId(final int arg0) {

            return arg0;
        }

        @Override
        public View getView(final int arg0, final View arg1, final ViewGroup arg2) {

            final ImageView iView = new ImageView(ctx);
            iView.setImageBitmap(getImage(arg0, false));
            iView.setScaleType(ImageView.ScaleType.FIT_XY);
            iView.setLayoutParams(new Gallery.LayoutParams(150, 150));
            return iView;
        }

    }

    @Override
    public View makeView() {
        final ImageView iView = new ImageView(this);
        iView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        iView.setBackgroundColor(0xFF000000);
        return iView;
    }

    // public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap>
    // {
    // private Context mContext; // reference to the calling Activity
    // int progress = -1;
    // Bitmap downloadedImage = null;
    //
    // DownloadImageTask(Context context) {
    // mContext = context;
    // }
    //
    // // Called from main thread to re-attach
    // protected void setContext(Context context) {
    // mContext = context;
    // if(progress >= 0) {
    // publishProgress(this.progress);
    // }
    // }
    //
    // protected void onPreExecute() {
    // progress = 0;
    // // We could do some other setup work here before doInBackground() runs
    // }
    //
    // protected Bitmap doInBackground(String... urls) {
    // Ut.d("doInBackground", "doing download of image...");
    // return downloadImage(urls);
    // }
    //
    // protected void onProgressUpdate(Integer... progress) {
    // TextView mText = (TextView)
    // ((Activity) mContext).findViewById(R.id.text);
    // mText.setText("Progress so far: " + progress[0]);
    // }
    //
    // protected void onPostExecute(Bitmap result) {
    // if(result != null) {
    // downloadedImage = result;
    // setImageInView();
    // }
    // else {
    // TextView errorMsg = (TextView)
    // ((Activity) mContext).findViewById(R.id.errorMsg);
    // errorMsg.setText("Problem downloading image. Please try later.");
    // }
    // stopSelf();
    // }
    //
    // public Bitmap downloadImage(String... urls)
    // {
    // HttpClient httpClient = CustomHttpClient.getHttpClient();
    // try {
    // HttpGet request = new HttpGet(urls[0]);
    // HttpParams params = new BasicHttpParams();
    // HttpConnectionParams.setSoTimeout(params, 60000); // 1 minute
    // request.setParams(params);
    //
    // setProgress(25);
    //
    // HttpResponse response = httpClient.execute(request);
    //
    // setProgress(50);
    //
    // sleepFor(5000); // five second sleep
    //
    // byte[] image = EntityUtils.toByteArray(response.getEntity());
    //
    // setProgress(75);
    //
    // Bitmap mBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
    //
    // setProgress(100);
    //
    // return mBitmap;
    // } catch (IOException e) {
    // // covers:
    // // ClientProtocolException
    // // ConnectTimeoutException
    // // ConnectionPoolTimeoutException
    // // SocketTimeoutException
    // e.printStackTrace();
    // }
    // return null;
    // }
    //
    // private void setProgress(int progress) {
    // this.progress = progress;
    // publishProgress(this.progress);
    // }
    //
    // protected void setImageInView() {
    // if(downloadedImage != null) {
    // ImageView mImage = (ImageView)
    // ((Activity) mContext).findViewById(R.id.image);
    // mImage.setImageBitmap(downloadedImage);
    // }
    // }
    //
    // private void sleepFor(long msecs) {
    // try {
    // Thread.sleep(msecs);
    // } catch (InterruptedException e) {
    // Ut.d("sleep: interrupted");
    // }
    // }
    // }

}
