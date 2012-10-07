package org.outlander.fragments;

import org.outlander.instruments.artificialhorizon.HorizontFragment;
import org.outlander.instruments.compass.CompassFragment;
import org.outlander.utils.CoreInfoHandler;

import android.support.v4.app.Fragment;

public class FragmentFactory {

    final public static int   FRAG_ID_POILIST    = 0;
    final public static int   FRAG_ID_ROUTELIST  = 1;
    final public static int   FRAG_ID_TRACKLIST  = 2;
    final public static int   FRAG_ID_COMPASS    = 4;
    final public static int   FRAG_ID_HORIZON    = 5;
    final public static int   FRAG_ID_NAVIGATION = 6;
    final public static int   FRAG_ID_MAP        = 7;
    final public static int   FRAG_ID_WIKI       = 8;
    final public static int   FRAG_ID_CAT        = 9;
    final public static int   FRAG_ID_TOPOSEARCH = 10;

    final public static int   FRAG_ID_PAGER      = 99;

    final public static int[] FRAGMENT_PAGES     = {
            FragmentFactory.FRAG_ID_NAVIGATION,
            FragmentFactory.FRAG_ID_POILIST, FragmentFactory.FRAG_ID_ROUTELIST,
            FragmentFactory.FRAG_ID_TRACKLIST, FragmentFactory.FRAG_ID_WIKI,
            FragmentFactory.FRAG_ID_TOPOSEARCH  };

    public static int getFragmentTabPageIndexById(final int pageId) {
        int ix = -1;

        for (int i = 0; i < FRAGMENT_PAGES.length; i++) {
            if (FRAGMENT_PAGES[i] == pageId) {
                ix = i;
                break;
            }
        }
        return ix;
    }

    public static Fragment getFragmentTabPageById(final int pageId) {
        Fragment fragment = null;
        final int ix = getFragmentTabPageIndexById(pageId);

        if (ix > -1) {
            fragment = getFragmentTabPage(ix);
        }

        return fragment;
    }

    public static Fragment getFragmentTabPage(final int pageIx) {

        final Fragment fragment = getFragmentPage(CoreInfoHandler.getInstance()
                .getPageSet()[pageIx]);

        CoreInfoHandler.getInstance().setPageChangeNotifyer(pageIx,
                (PageChangeNotifyer) fragment);

        return fragment;
    }

    public static Fragment getFragmentPage(final int pageId) {
        Fragment fragment = null;
        switch (pageId) {
            case FRAG_ID_POILIST: {
                fragment = PoiListFragment.newInstance();
                break;
            }
            case FRAG_ID_CAT: {
                fragment = CategoryListFragment
                        .newInstance(CategoryListFragment.CAT_TYPE_POI);
                break;
            }
            case FRAG_ID_COMPASS: {
                fragment = CompassFragment.newInstance();
                break;
            }
            case FRAG_ID_HORIZON: {
                fragment = HorizontFragment.newInstance();
                break;
            }
            case FRAG_ID_MAP: {
                fragment = MapFragment.newInstance();
                break;
            }
            case FRAG_ID_NAVIGATION: {
                fragment = NavigationFragment.newInstance();
                break;
            }
            case FRAG_ID_PAGER: {
                fragment = PagerFragment.newInstance();
                break;
            }
            case FRAG_ID_ROUTELIST: {
                fragment = RouteListFragment.newInstance();
                break;
            }
            case FRAG_ID_TRACKLIST: {
                fragment = TrackListFragment.newInstance();
                break;
            }
            case FRAG_ID_TOPOSEARCH: {
                fragment = ToponymSearchResultFragment.newInstance(1);
                break;
            }
            case FRAG_ID_WIKI: {
                // fragment = LocationInfoFragment.newInstance();
                fragment = WikipediaSearchResultFragment.newInstance();
                break;
            }
        }
        return fragment;
    }

}
