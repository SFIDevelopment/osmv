package at.the.gogo.parkoid.fragments;

import android.support.v4.app.Fragment;
import at.the.gogo.parkoid.util.db.DBConstants;

public class FragmentFactory {

    final public static int   FRAG_ID_OVERVIEW   = 0;
    final public static int   FRAG_ID_MAP        = 1;
    final public static int   FRAG_ID_ADDR       = 2;
    final public static int   FRAG_ID_SMSOUT     = 4;
    final public static int   FRAG_ID_SMSIN      = 5;
    final public static int   FRAG_ID_CAR        = 3;

    final public static int   FRAG_ID_PAGER      = 99;

    final public static int[] FRAGMENT_PAGES_TAB = {
            FragmentFactory.FRAG_ID_OVERVIEW, FragmentFactory.FRAG_ID_MAP,
            FragmentFactory.FRAG_ID_CAR, FragmentFactory.FRAG_ID_SMSOUT,
            FragmentFactory.FRAG_ID_SMSIN       };

    // FRAG_ID_ADDR,

    public static Fragment getFragmentTabPage(final int pageId) {
        return getFragmentPage(FragmentFactory.FRAGMENT_PAGES_TAB[pageId]);
    }

    public static Fragment getFragmentPage(final int pageId) {
        Fragment fragment = null;
        switch (pageId) {
            case FRAG_ID_OVERVIEW: {
                fragment = OverviewFragment.newInstance();
                break;
            }
            case FRAG_ID_MAP: {
                fragment = MapFragment.newInstance();
                break;
            }
            case FRAG_ID_CAR: {
                fragment = CarListFragment.newInstance();
                break;
            }
            case FRAG_ID_ADDR: {
                fragment = AddressListFragment.newInstance();
                break;
            }
            case FRAG_ID_PAGER: {
                fragment = PagerFragment.newInstance();
                break;
            }
            case FRAG_ID_SMSOUT: {
                fragment = SMSListFragment.newInstance(DBConstants.TABLE_SMS);
                break;
            }
            case FRAG_ID_SMSIN: {
                fragment = SMSListFragment.newInstance(DBConstants.TABLE_SMSR);
                break;
            }

        }
        return fragment;
    }

}
