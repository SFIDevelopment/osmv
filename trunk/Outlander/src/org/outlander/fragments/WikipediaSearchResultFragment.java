package org.outlander.fragments;

import java.util.ArrayList;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.andnav.osm.util.GeoPoint;
import org.geonames.WebService;
import org.geonames.WikipediaArticle;
import org.outlander.R;
import org.outlander.constants.DBConstants;
import org.outlander.model.PoiPoint;
import org.outlander.overlays.PoiOverlay;
import org.outlander.overlays.SearchResultOverlay;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

public class WikipediaSearchResultFragment extends SherlockListFragment implements PageChangeNotifyer {

    boolean                        mHasDetailsFrame;
    int                            mPositionChecked = 0;
    int                            mPositionShown   = -1;
    private List<WikipediaArticle> mWeblinks;

    private GeoPoint               mRecentGeoPoint;
    private boolean                isInitialized    = false;
    private TextView               nrOfEntries;
    private QuickAction            mQuickAction;
    private ProgressDialog         dlgWait;

    public static WikipediaSearchResultFragment newInstance() {
        final WikipediaSearchResultFragment f = new WikipediaSearchResultFragment();

        return f;
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

        header.setText(R.string.tab_wiki);

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

    private void restoreSavedState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPositionChecked = savedInstanceState.getInt("curChoiceWikiList", 0);
            mPositionShown = savedInstanceState.getInt("shownChoiceWikiList", -1);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        restoreSavedState(savedInstanceState);

        if (!isInitialized) {
            mQuickAction = new QuickAction(getActivity());
            setupQuickAction(mQuickAction);
            isInitialized = true;
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

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curChoiceTopoList", mPositionChecked);
        outState.putInt("shownChoiceTopoList", mPositionShown);
    }

    // public final static String QUERYTERMKEY = "searchString";

    private List<WikipediaArticle> searchForArticles(final GeoPoint geoPoint) {
        List<WikipediaArticle> weblinks = null;
        try {
            if (Ut.isInternetConnectionAvailable(getActivity())) {
                final String language = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_googlelanguagecode", "en");
                WebService.setUserName(WebService.USERNAME);
                weblinks = WebService.findNearbyWikipedia(geoPoint.getLatitude(), geoPoint.getLongitude(), 2, language, 10);

                // clear old points
                CoreInfoHandler.getInstance().getDBManager(getActivity()).deletePoisOfCategoryWiki();

                if (weblinks != null) {

                    GeoPoint gpoint = null;
                    final List<PoiPoint> points = new ArrayList<PoiPoint>();
                    for (final WikipediaArticle article : weblinks) {
                        final PoiPoint point = new PoiPoint();

                        if (gpoint == null) {
                            gpoint = new GeoPoint((int) (article.getLatitude() * 1E6), (int) (article.getLongitude() * 1E6));
                        }
                        else {
                            gpoint.setLatitude(article.getLatitude());
                            gpoint.setLongitude(article.getLongitude());
                        }

                        point.setGeoPoint(gpoint);
                        point.setTitle(article.getTitle());
                        point.setDescr(article.getSummary());
                        point.setIconId(SearchResultOverlay.ICON_ID);
                        point.setCategoryId(DBConstants.POI_CATEGORY_WIKI);

                        CoreInfoHandler.getInstance().getDBManager(getActivity()).updatePoi(point);

                        sendMsgToOverlay(PoiOverlay.POI_REFRESH, -1);

                        // CoreInfoHandler.getInstance().getPoiOverlay().refresh();
                    }
                }
            }
        }
        catch (final Exception e) {
            Ut.dd("wikipedia" + e.toString());
        }

        return weblinks;
    }

    private void sendMsgToOverlay(String cmd, int id) {
        Intent intent = new Intent(cmd);
        if (id > -1)
            intent.putExtra(PoiOverlay.POI_ID, id);

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    public void fillData() {

        // only refresh if point has changed
        if ((CoreInfoHandler.getInstance().getCurrentSearchPoint() != null) && (mRecentGeoPoint != CoreInfoHandler.getInstance().getCurrentSearchPoint())) {
            mRecentGeoPoint = CoreInfoHandler.getInstance().getCurrentSearchPoint();

            final GetData asyncTask = new GetData();
            dlgWait = Ut.ShowWaitDialog(getActivity(), 0);
            asyncTask.execute(mRecentGeoPoint);
        }
    }

    public class GetData extends AsyncTask<GeoPoint, Void, List<WikipediaArticle>> {

        @Override
        protected List<WikipediaArticle> doInBackground(final GeoPoint... params) {

            mWeblinks = searchForArticles(params[0]);

            return mWeblinks;
        }

        @Override
        protected void onPostExecute(final List<WikipediaArticle> entries) {

            if (nrOfEntries != null) {
                final String newHeaderDescr = getString(R.string.EntriesInCategory) + ((entries != null) ? entries.size() : 0);
                nrOfEntries.setText(newHeaderDescr);
            }

            final ListAdapter adapter = new BaseAdapter() {

                private LayoutInflater inflater = null;

                @Override
                public int getCount() {
                    return ((entries != null) ? entries.size() : 0);
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
                        convertView = inflater.inflate(R.layout.list_item, null);
                        holder = new ViewHolder();
                        holder.textView1 = (TextView) convertView.findViewById(android.R.id.text1);
                        holder.textView2 = (TextView) convertView.findViewById(android.R.id.text2);
                        holder.icon1 = (ImageView) convertView.findViewById(R.id.ImageView01);
                        holder.icon2 = (ImageView) convertView.findViewById(R.id.ImageView02);

                        holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkBox1);
                        holder.checkbox.setVisibility(View.GONE);
                        convertView.setTag(holder);

                    }
                    else {
                        holder = (ViewHolder) convertView.getTag();
                    }

                    holder.textView1.setText(entries.get(position).getTitle());
                    holder.textView2.setText(entries.get(position).getSummary());
                    holder.icon2.setImageResource(R.drawable.wikipedia);

                    holder.icon1.setVisibility(View.GONE);

                    return convertView;
                }

                @Override
                public Object getItem(final int position) {
                    return entries.get(position);
                }
            };

            setListAdapter(adapter);

            if (dlgWait != null) {
                dlgWait.dismiss();
            }
        }
    }

    private void openURL(final WikipediaArticle wpa) {
        if (wpa.getWikipediaUrl() != null) {
            try {
                final Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://" + wpa.getWikipediaUrl()));
                startActivity(viewIntent);
            }
            catch (final Exception x) {
                Toast.makeText(getActivity(), getString(R.string.url_open_problem), Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getActivity(), getString(R.string.url_missing), Toast.LENGTH_LONG).show();
        }
    }

    public static class ViewHolder {

        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
        public TextView groupHeader;
        ImageView       icon1;
        ImageView       icon2;
        CheckBox        checkbox;
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {

        mPositionChecked = position; // id;
        mQuickAction.show(v);

    }

    private void setupQuickAction(final QuickAction quickAction) {

        // ------
        ActionItem item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_show));
        item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
        quickAction.addActionItem(item);

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_shareLocation));
        item.setIcon(getResources().getDrawable(R.drawable.menu_share));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_addpoi));
        item.setIcon(getResources().getDrawable(R.drawable.menu_add));
        quickAction.addActionItem(item);

        // ------

        item = new ActionItem();

        item.setTitle(getResources().getString(R.string.menu_showexternalpoi));
        item.setIcon(getResources().getDrawable(R.drawable.menu_navi));
        quickAction.addActionItem(item);

        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

            @Override
            public void onItemClick(final int pos) {
                if (pos == 0) {
                    handleContextItemSelected(R.id.menu_show);
                }
                else if (pos == 1) {
                    handleContextItemSelected(R.id.menu_shareLocation);
                }
                else if (pos == 2) {
                    handleContextItemSelected(R.id.menu_addpoi);
                }
                else if (pos == 3) {
                    handleContextItemSelected(R.id.menu_showexternalpoi);
                }
            }
        });

    }

    private void handleContextItemSelected(final int id) {

        final WikipediaArticle article = mWeblinks.get(mPositionChecked);

        switch (id) {
            case R.id.menu_show:

                if (article != null) {
                    openURL(article);
                }

                break;
            case R.id.menu_addpoi:

                addPoi(article);

                // Toast.makeText(getActivity(), R.string.NYI,
                // Toast.LENGTH_SHORT)
                // .show();

                break;

            case R.id.menu_showexternalpoi:
                try {
                    startActivity(Ut.showLocationExternal(article.getLatitude(), article.getLongitude()));
                }
                catch (final ActivityNotFoundException x) {
                    Toast.makeText(getActivity(), R.string.no_activity, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_shareLocation: {

                final Intent intent = Ut.shareLocation(article.getLatitude(), article.getLongitude(), article.getTitle() + "\n" + article.getWikipediaUrl(),
                        getActivity());

                if (intent != null) {
                    startActivity(intent);
                }
                // Toast.makeText(this, R.string.NYI, Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    private void addPoi(final WikipediaArticle article) {
        final DialogFragment newFragment = PoiDialogFragment.newInstance(-1, article.getTitle(), article.getSummary(), article.getLatitude(),
                article.getLongitude(), R.string.dialogTitlePOI);
        showDialog(newFragment);
    }

    private void showDialog(final DialogFragment dialog) {
        final FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        final Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            transaction.remove(prev);
        }

        transaction.addToBackStack(null);
        dialog.show(transaction, "dialog");
    }

    @Override
    public void pageGetsActivated() {
        resume();
    }

    @Override
    public void pageGetsDeactivated() {

    }

    @Override
    public void refresh() {
        resume();

    }

}
