package org.outlander.views;

import java.text.DateFormat;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.geonames.Timezone;
import org.geonames.WeatherObservation;
import org.geonames.WebService;
import org.outlander.R;
import org.outlander.model.PanoramioItem;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;
import org.outlander.utils.img.RoundedCornerImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LocationInfoView {

    private GeoPoint mRecentGeoPoint;

    // ListView wikiList;

    private View     view;
    // private Gallery gallery;
    private Context  context;
    private TextView textView[];

    public LocationInfoView() {

    }

    public View getView() {
        return view;
    }

    public void setGeoPoint(final GeoPoint geoPoint) {
        mRecentGeoPoint = geoPoint;
    }

    public View createView(final Context context, final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.context = context;

        view = inflater.inflate(R.layout.locationinfo, container, false);

        textView = new TextView[8];

        inflateField(view, R.id.coordinates, 0, R.string.field_title_1);
        inflateField(view, R.id.address, 1, R.string.field_title_2);
        inflateField(view, R.id.sunrise, 2, R.string.field_title_3);
        inflateField(view, R.id.sunset, 3, R.string.field_title_4);
        inflateField(view, R.id.localtime, 4, R.string.field_title_5);
        inflateField(view, R.id.temperature, 5, R.string.field_title_6);
        inflateField(view, R.id.windspeed, 6, R.string.field_title_7);
        inflateField(view, R.id.metar, 7, R.string.field_title_8);

        return view;
    }

    private void inflateField(final View view, final int id, final int valueIx, final int captionId) {

        final LinearLayout ll = (LinearLayout) view.findViewById(id);

        final TextView caption = (TextView) ll.findViewById(R.id.textView1);
        caption.setText(captionId);
        textView[valueIx] = (TextView) ll.findViewById(R.id.textView2);
    }

    public void fillData() {

        if ((mRecentGeoPoint == null) || (mRecentGeoPoint != CoreInfoHandler.getInstance().getCurrentSearchPoint())) {
            mRecentGeoPoint = CoreInfoHandler.getInstance().getCurrentSearchPoint();

            if (mRecentGeoPoint != null) {
                // get address
                final GetAddressData adrTask = new GetAddressData();
                adrTask.execute(mRecentGeoPoint);

                // get timezone
                final GetTimezoneData tzTask = new GetTimezoneData();
                tzTask.execute(mRecentGeoPoint);

                // get weather
                final GetWeatherData weatherTask = new GetWeatherData();
                weatherTask.execute(mRecentGeoPoint);

                // get images
                final GetPanoramioData imgTask = new GetPanoramioData();
                imgTask.execute(mRecentGeoPoint);

                // get wikipedia
                // final GetWikiData wikiTask = new GetWikiData();
                // wikiTask.execute(mRecentGeoPoint);
            }
        }
    }

    public class GetAddressData extends AsyncTask<GeoPoint, Void, Address> {

        GeoPoint gp;

        @Override
        protected Address doInBackground(final GeoPoint... params) {

            gp = params[0];
            final Address addr = Ut.getRawAddressFromYahoo(context, params[0].getLatitude(), params[0].getLongitude());

            return addr;
        }

        @Override
        protected void onPostExecute(final Address address) {

            final int coordFormt = CoreInfoHandler.getInstance().getCoordFormatId();

            textView[0].setText(GeoMathUtil.formatGeoPoint(gp, coordFormt));

            if (address != null) {

                textView[1].setText(address.getAddressLine(0) + " " + address.getAddressLine(1));
            }

        }
    }

    public class GetTimezoneData extends AsyncTask<GeoPoint, Void, Timezone> {

        @Override
        protected Timezone doInBackground(final GeoPoint... params) {
            Timezone timezone = null;

            try {
                timezone = WebService.timezone(params[0].getLatitude(), params[0].getLongitude());
            }
            catch (final Exception x) {
                Ut.d("Timezone request failed with : " + x.toString());
            }
            return timezone;
        }

        @Override
        protected void onPostExecute(final Timezone timezone) {

            // set fields

            if (timezone != null) {
                final DateFormat df = DateFormat.getDateTimeInstance();

                textView[2].setText(timezone.getSunrise() != null ? df.format(timezone.getSunrise()) : "unknown");

                textView[3].setText(timezone.getSunset() != null ? df.format(timezone.getSunset()) : "unknown");

                textView[4].setText(timezone.getSunset() != null ? df.format(timezone.getTime()) : "unknown");

            }
            // super.onPostExecute(result);
        }
    }

    public class GetWeatherData extends AsyncTask<GeoPoint, Void, WeatherObservation> {

        @Override
        protected WeatherObservation doInBackground(final GeoPoint... params) {
            WeatherObservation weather = null;

            try {
                weather = WebService.findNearByWeather(params[0].getLatitude(), params[0].getLongitude());
            }
            catch (final Exception x) {
                Ut.d("Weather request failed with : " + x.toString());
            }
            return weather;
        }

        @Override
        protected void onPostExecute(final WeatherObservation weather) {

            // set fields

            if (weather != null) {
                TextView textView = (TextView) getView().findViewById(R.id.temperature);
                textView.setText(weather.getTemperature() + "°C");

                textView = (TextView) getView().findViewById(R.id.windspeed);
                textView.setText(weather.getWindSpeed() + " knots");

                textView = (TextView) getView().findViewById(R.id.metar);
                textView.setText(weather.getObservation());

                // textView = (TextView) getView().findViewById(R.id.localtime);
                // textView.setText(df.format(timezone.getTime()));

            }
            // super.onPostExecute(result);
        }
    }

    public class GetPanoramioData extends AsyncTask<GeoPoint, Void, List<PanoramioItem>> {

        @Override
        protected List<PanoramioItem> doInBackground(final GeoPoint... params) {
            List<PanoramioItem> images = null;

            try {

                images = WebService.getImages(params[0].getLatitude(), params[0].getLongitude(), 10);

                CoreInfoHandler.getInstance().setPanoramioItems(images);

            }
            catch (final Exception x) {
                Ut.d("Timezone request failed with : " + x.toString());
            }
            return images;
        }

        @Override
        protected void onPostExecute(final List<PanoramioItem> images) {

            // set fields

            if (images != null) {

                ((Gallery) getView().findViewById(R.id.gallery)).setAdapter(new BaseAdapter() {

                    @Override
                    public int getCount() {

                        return images.size();
                    }

                    @Override
                    public Object getItem(final int position) {

                        return images.get(position).getImage();
                    }

                    @Override
                    public long getItemId(final int position) {

                        return position;
                    }

                    @Override
                    public View getView(final int position, final View convertView, final ViewGroup parent) {

                        final ImageView i = new RoundedCornerImageView(parent.getContext());

                        // disable hardware accelleration
                        i.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                        i.setImageBitmap((Bitmap) getItem(position));
                        /*
                         * Image should be scaled as width/height are set.
                         */
                        i.setScaleType(ImageView.ScaleType.FIT_XY);
                        /* Set the Width/Height of the ImageView. */
                        i.setLayoutParams(new Gallery.LayoutParams(40, 40));

                        return i;
                    }

                });

            }
        }
    }

    /*
     * public class GetWikiData extends AsyncTask<GeoPoint, Void,
     * List<WikipediaArticle>> { private List<WikipediaArticle> mWeblinks;
     * @Override protected List<WikipediaArticle> doInBackground( final
     * GeoPoint... params) { mWeblinks = searchForArticles(params[0]); return
     * mWeblinks; }
     * @Override protected void onPostExecute(final List<WikipediaArticle>
     * entries) { // if (nrOfEntries != null) { // final String newHeaderDescr =
     * // getString(R.string.EntriesInCategory) // + ((entries != null) ?
     * entries.size() : 0); // nrOfEntries.setText(newHeaderDescr); // } final
     * ListAdapter adapter = new BaseAdapter() { private LayoutInflater inflater
     * = null;
     * @Override public int getCount() { return ((entries != null) ?
     * entries.size() : 0); }
     * @Override public long getItemId(final int position) { return position; }
     * @Override public View getView(final int position, View convertView, final
     * ViewGroup parent) { ViewHolder holder = null; if (convertView == null) {
     * if (inflater == null) { inflater = LayoutInflater.from(getActivity()); }
     * convertView = inflater .inflate(R.layout.list_item, null); holder = new
     * ViewHolder(); holder.textView1 = (TextView) convertView
     * .findViewById(android.R.id.text1); holder.textView2 = (TextView)
     * convertView .findViewById(android.R.id.text2); holder.icon1 = (ImageView)
     * convertView .findViewById(R.id.ImageView01); holder.icon2 = (ImageView)
     * convertView .findViewById(R.id.ImageView02); holder.checkbox = (CheckBox)
     * convertView .findViewById(R.id.checkBox1); convertView.setTag(holder); }
     * else { holder = (ViewHolder) convertView.getTag(); }
     * holder.textView1.setText(entries.get(position).getTitle());
     * holder.textView2 .setText(entries.get(position).getSummary());
     * holder.icon2.setImageResource(R.drawable.wikipedia);
     * holder.icon1.setVisibility(View.GONE);
     * holder.checkbox.setVisibility(View.GONE); return convertView; }
     * @Override public Object getItem(final int position) { return
     * entries.get(position); } }; wikiList.setAdapter(adapter); } }
     */

    // private List<WikipediaArticle> searchForArticles(final GeoPoint geoPoint)
    // {
    // List<WikipediaArticle> weblinks = null;
    // try {
    // if (Ut.isInternetConnectionAvailable(context)) {
    // final String language = PreferenceManager
    // .getDefaultSharedPreferences(context).getString(
    // "pref_googlelanguagecode", "en");
    // WebService.setUserName(WebService.USERNAME);
    // weblinks = WebService.findNearbyWikipedia(
    // geoPoint.getLatitude(), geoPoint.getLongitude(), 2,
    // language, 10);
    //
    // }
    // } catch (final Exception e) {
    // Ut.dd("wikipedia" + e.toString());
    // }
    //
    // return weblinks;
    // }

    // private void openURL(final WikipediaArticle wpa) {
    // if (wpa.getWikipediaUrl() != null) {
    // try {
    // final Intent viewIntent = new Intent(
    // "android.intent.action.VIEW", Uri.parse(wpa
    // .getWikipediaUrl()));
    // startActivity(viewIntent);
    // } catch (final Exception x) {
    // Toast.makeText(getActivity(),
    // getString(R.string.url_open_problem), Toast.LENGTH_LONG)
    // .show();
    // }
    // } else {
    // Toast.makeText(getActivity(), getString(R.string.url_missing),
    // Toast.LENGTH_LONG).show();
    // }
    // }

    public static class ViewHolder {

        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
        public TextView groupHeader;
        ImageView       icon1;
        ImageView       icon2;
        CheckBox        checkbox;
    }

}
