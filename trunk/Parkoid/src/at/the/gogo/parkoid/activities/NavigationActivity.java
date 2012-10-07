package at.the.gogo.parkoid.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.Route;
import at.the.gogo.parkoid.models.TurnPoint;
import at.the.gogo.parkoid.util.webservices.CloudmadeRequests;

import com.actionbarsherlock.app.SherlockListActivity;

public class NavigationActivity extends SherlockListActivity {

    double   sourceLat, sourceLon, destLat, destLon;
    TextView info;
    TextView caption;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            sourceLat = savedInstanceState.getDouble("sourceLat");
            sourceLon = savedInstanceState.getDouble("sourceLon");
            destLat = savedInstanceState.getDouble("destLat");
            destLon = savedInstanceState.getDouble("destLon");
        }

        setContentView(R.layout.headerlist);

        info = (TextView) findViewById(R.id.currentAddress);
        caption = (TextView) findViewById(R.id.locationCaption);

        if ((sourceLat != 0.0) && (sourceLon != 0.0) && (destLat != 0.0)
                && (destLon != 0.0)) {
            fillData();
        }
    }

    public void fillData() {
        final GetDataTask asyncTask = new GetDataTask();
        asyncTask.execute((Void) null);
    }

    public class GetDataTask extends AsyncTask<Void, Void, Route> {

        @Override
        protected Route doInBackground(final Void... params) {

            final Route route = CloudmadeRequests.getRoutingInfo(sourceLat,
                    sourceLon, destLat, destLon, true);
            return route;
        }

        @Override
        protected void onPostExecute(final Route route) {

            // final String text = CoreInfoHolder.getInstance().getContext()
            // .getText(R.string.nr_of_entries).toString();
            // nrOfEntries.setText(text + " " + entries.size());

            final ListAdapter adapter = new BaseAdapter() {

                private LayoutInflater inflater = null;

                @Override
                public int getCount() {
                    return (route != null) ? route.getTurnpoints().size() : 0;
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
                            inflater = getLayoutInflater(); // LayoutInflater.from(getActivity());
                        }
                        convertView = inflater
                                .inflate(R.layout.navi_item, null);

                        holder = new ViewHolder();

                        holder.textView1 = (TextView) convertView
                                .findViewById(R.id.AddressLine1);
                        holder.textView2 = (TextView) convertView
                                .findViewById(R.id.AddressLine2);
                        holder.textView3 = (TextView) convertView
                                .findViewById(R.id.AddressLine3);

                        holder.icon1 = (ImageView) convertView
                                .findViewById(R.id.imageView1);

                        convertView.setTag(holder);

                    } else {
                        holder = (ViewHolder) convertView.getTag();
                    }

                    final TurnPoint turnpoint = route.getTurnpoints().get(
                            position);

                    if (turnpoint.getTurnType().equals("C")) {
                        holder.icon1.setImageResource(R.drawable.c);
                    } else if (turnpoint.getTurnType().equals("TL")) {
                        holder.icon1.setImageResource(R.drawable.tl);
                    } else if (turnpoint.getTurnType().equals("TR")) {
                        holder.icon1.setImageResource(R.drawable.tr);
                    } else if (turnpoint.getTurnType().equals("TSLL")) {
                        holder.icon1.setImageResource(R.drawable.tsll);
                    } else if (turnpoint.getTurnType().equals("TSHL")) {
                        holder.icon1.setImageResource(R.drawable.tshl);
                    } else if (turnpoint.getTurnType().equals("TSLR")) {
                        holder.icon1.setImageResource(R.drawable.tslr);
                    } else if (turnpoint.getTurnType().equals("TSHR")) {
                        holder.icon1.setImageResource(R.drawable.tshr);
                    } else if (turnpoint.getTurnType().equals("TU")) {
                        holder.icon1.setImageResource(R.drawable.tu);
                    }

                    holder.textView1.setText(turnpoint.getDescription());
                    holder.textView2.setText(turnpoint.getLength_caption()
                            + " " + turnpoint.getEarth_direction());
                    holder.textView3.setText(turnpoint.getLength() + " "
                            + turnpoint.getAzimuth() + "°");

                    // final Sms sms = entries.get(position);
                    //
                    // final String date =
                    // DateUtils.formatDateTime(getActivity(),
                    // sms.getDate().getTime(),
                    // DateUtils.FORMAT_SHOW_WEEKDAY
                    // | DateUtils.FORMAT_SHOW_DATE
                    // | DateUtils.FORMAT_SHOW_TIME
                    // | DateUtils.FORMAT_24HOUR);
                    //
                    // holder.textView1.setText(sms.getName());
                    // holder.textView2.setText(sms.getText());
                    // holder.textView3.setText(date);

                    return convertView;
                }

                @Override
                public Object getItem(final int position) {
                    return route.getTurnpoints().get(position);
                }
            };
            setListAdapter(adapter);

        }
    }

    public static class ViewHolder {
        TextView  textView1;
        TextView  textView2;
        TextView  textView3;
        ImageView icon1;
        ImageView icon2;
    }

}
