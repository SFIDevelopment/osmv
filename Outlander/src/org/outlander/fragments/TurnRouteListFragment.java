package org.outlander.fragments;

import org.andnav.osm.util.GeoPoint;
import org.geonames.CloudmadeRequests;
import org.outlander.R;
import org.outlander.fragments.WikipediaSearchResultFragment.ViewHolder;
import org.outlander.model.TurnPoint;
import org.outlander.model.TurnRoute;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class TurnRouteListFragment extends SherlockListFragment {

    TextView               nrOfEntries;
    int                    mPositionChecked;
    int                    mPositionShown;

    GeoPoint               mTarget;
    GeoPoint               mRecentGeoPoint;
    private ProgressDialog dlgWait;

    public static TurnRouteListFragment newInstance(final GeoPoint target) {
        final TurnRouteListFragment f = new TurnRouteListFragment(target);

        return f;
    }

    public TurnRouteListFragment(final GeoPoint target) {
        super();
        mTarget = target;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        restoreSavedState(savedInstanceState);

        final View view = inflater.inflate(R.layout.headerlist, null);

        final ImageView icon = (ImageView) view.findViewById(R.id.menuButton);
        final TextView header = (TextView) view.findViewById(R.id.caption1);
        final Button btnMenu = (Button) view.findViewById(R.id.button_menu);
        btnMenu.setVisibility(View.GONE);

        icon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                getActivity().openOptionsMenu();
            }
        });

        header.setText(R.string.tab_turnroute);

        nrOfEntries = (TextView) view.findViewById(R.id.caption2);
        nrOfEntries.setText(R.string.EntriesInCategory);

        header.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                getActivity().openOptionsMenu();
            }
        });

        return view;

    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curChoiceTurnList", mPositionChecked);
        outState.putInt("shownChoiceTurnList", mPositionShown);
    }

    private void restoreSavedState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPositionChecked = savedInstanceState.getInt("curChoiceTurnList", 0);
            mPositionShown = savedInstanceState.getInt("shownChoiceTurnList", -1);
        }
    }

    @Override
    public void onResume() {

        resume();

        super.onResume();
    }

    private void resume() {
        fillData();
    }

    public void fillData() {

        // only refresh if point has changed
        if ((mTarget != null) && (mRecentGeoPoint != CoreInfoHandler.getInstance().getCurrentLocationAsGeoPoint())) {
            mRecentGeoPoint = CoreInfoHandler.getInstance().getCurrentLocationAsGeoPoint();

            final GetData asyncTask = new GetData();
            dlgWait = Ut.ShowWaitDialog(getActivity(), 0);
            asyncTask.execute(mRecentGeoPoint);
        }
    }

    public class GetData extends AsyncTask<GeoPoint, Void, TurnRoute> {

        @Override
        protected TurnRoute doInBackground(final GeoPoint... params) {

            final GeoPoint source = CoreInfoHandler.getInstance().getCurrentLocationAsGeoPoint();

            final GeoPoint target = mTarget;

            final TurnRoute turnroute = CloudmadeRequests.getRoutingInfo(source.getLatitude(), source.getLongitude(), target.getLatitude(),
                    target.getLongitude(), true);

            // convert to our internal route formatting
            // and save to db for visualisation
            if (turnroute != null) {
                CoreInfoHandler.getInstance().getDBManager(null).updateRoute(turnroute.getAsRoute(), true);
            }

            return turnroute;
        }

        @Override
        protected void onPostExecute(final TurnRoute turnroute) {

            if (nrOfEntries != null) {
                final String newHeaderDescr = getString(R.string.EntriesInCategory) + ((turnroute != null) ? turnroute.getTurnpoints().size() : 0);
                nrOfEntries.setText(newHeaderDescr);
            }

            final ListAdapter adapter = new BaseAdapter() {

                private LayoutInflater inflater = null;

                @Override
                public int getCount() {
                    return ((turnroute != null) ? turnroute.getTurnpoints().size() : 0);
                }

                @Override
                public long getItemId(final int position) {
                    return position;
                }

                @Override
                public View getView(final int position, View convertView, final ViewGroup parent) {

                    ViewHolder holder = null;

                    if (convertView == null) {
                        if (inflater == null) {
                            inflater = LayoutInflater.from(getActivity());
                        }
                        convertView = inflater.inflate(R.layout.navi_list_item, null);

                        holder = new ViewHolder();

                        holder.textView1 = (TextView) convertView.findViewById(android.R.id.text1);
                        holder.textView2 = (TextView) convertView.findViewById(android.R.id.text2);
                        holder.textView3 = (TextView) convertView.findViewById(R.id.text3);

                        holder.icon1 = (ImageView) convertView.findViewById(R.id.ImageView1);

                        convertView.setTag(holder);

                    }
                    else {
                        holder = (ViewHolder) convertView.getTag();
                    }

                    final TurnPoint turnPoint = turnroute.getTurnpoints().get(position);

                    if (turnPoint.getTurnType() != null) {
                        if (turnPoint.getTurnType().equals("C")) {
                            holder.icon1.setImageResource(R.drawable.c);
                        }
                        else if (turnPoint.getTurnType().equals("TL")) {
                            holder.icon1.setImageResource(R.drawable.tl);
                        }
                        else if (turnPoint.getTurnType().equals("TR")) {
                            holder.icon1.setImageResource(R.drawable.tr);
                        }
                        else if (turnPoint.getTurnType().equals("TSLL")) {
                            holder.icon1.setImageResource(R.drawable.tsll);
                        }
                        else if (turnPoint.getTurnType().equals("TSHL")) {
                            holder.icon1.setImageResource(R.drawable.tshl);
                        }
                        else if (turnPoint.getTurnType().equals("TSLR")) {
                            holder.icon1.setImageResource(R.drawable.tslr);
                        }
                        else if (turnPoint.getTurnType().equals("TSHR")) {
                            holder.icon1.setImageResource(R.drawable.tshr);
                        }
                        else if (turnPoint.getTurnType().equals("TU")) {
                            holder.icon1.setImageResource(R.drawable.tu);
                        }
                    }
                    else {
                        holder.icon1.setImageResource(R.drawable.c);
                    }

                    holder.textView1.setText(turnPoint.getDescription());
                    holder.textView2.setText(turnPoint.getLength_caption() + " " + turnPoint.getEarth_direction());
                    holder.textView3.setText(turnPoint.getLength() + " " + turnPoint.getAzimuth() + "°");

                    return convertView;
                }

                @Override
                public Object getItem(final int position) {
                    return turnroute.getTurnpoints().get(position);
                }

            };

            setListAdapter(adapter);

            if (dlgWait != null) {
                dlgWait.dismiss();
            }
        }
    }

}
