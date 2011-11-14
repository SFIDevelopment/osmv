package at.the.gogo.parkoid.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.activities.ViewPagerIndicator;

import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
import com.viewpagerindicator.TitleProvider;

public class PagerFragment extends Fragment {

    private MyPagerAdapter mPagerAdapter;
    private ViewPager      mViewPager;
//    ViewPagerIndicator     mIndicator;
    TitlePageIndicator     mIndicator;

    public static PagerFragment newInstance() {
        final PagerFragment fragment = new PagerFragment();
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.pager, container, false);

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) view.findViewById(R.id.viewflipper);
        mViewPager.setAdapter(mPagerAdapter);

        final int lastpageViewed = sharedPreferences.getInt("PageInFlipper", 0);

        mViewPager.setCurrentItem(lastpageViewed);


        // 
		TitlePageIndicator indicator = (TitlePageIndicator)view.findViewById(R.id.indicator);
		indicator.setViewPager(mViewPager);
		indicator.setFooterIndicatorStyle(IndicatorStyle.Underline);
	       

		// other indicator impl !!
        // Find the indicator from the layout
//        mIndicator = (ViewPagerIndicator) view.findViewById(R.id.indicator);

        // Set the indicator as the pageChangeListener
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrolled(final int position,
                    final float positionOffset, final int positionOffsetPixels) {
                mIndicator.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);

            }

            private int lastPositionWorkaround = -1; // TODO: we get called
                                                     // twice ... why plz fix

            @Override
            public void onPageSelected(final int position) {

                if (position != lastPositionWorkaround) {
                    final PageChangeNotifyer oldPage = FragmentFactory.pages[mIndicator
                            .getCurrentItem()];

                    if (oldPage != null) {
                        oldPage.pageGetsDeactivated();
                    }

                    final PageChangeNotifyer newPage = FragmentFactory.pages[position];
                    if (newPage != null) {
                        newPage.pageGetsActivated();
                    }
                    lastPositionWorkaround = position;
                }
                mIndicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                mIndicator.onPageScrollStateChanged(state);

            }
        });

        // Initialize the OTHER indicator. We need some information here:
        // * What page do we start on.
        // * How many pages are there in total
        // * A callback to get page titles
//        mIndicator.init(0, mPagerAdapter.getCount(), mPagerAdapter);
//        final Resources res = getResources();
//        final Drawable prev = res.getDrawable(R.drawable.indicator_prev_arrow);
//        final Drawable next = res.getDrawable(R.drawable.indicator_next_arrow);
//        mIndicator.setFocusedTextColor(new int[] { 0xFF, 0xAF, 0x3F });
//        mIndicator.setUnfocusedTextColor(new int[] { 0x00, 0x00, 0x00 });
//
//        // Set images for previous and next arrows.
//        mIndicator.setArrows(prev, next);
//
//        mIndicator.setOnClickListener(new OnIndicatorClickListener());
//
        return view;
    }

    @Override
    public void onPause() {
        final SharedPreferences uiState = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = uiState.edit();
        editor.putInt("PageInFlipper", mViewPager.getCurrentItem());

        super.onPause();
    }

    public int getCurrentPageId() {
        return mIndicator.getCurrentItem();
    }

    public class MyPagerAdapter extends FragmentPagerAdapter implements
            ViewPagerIndicator.PageInfoProvider,TitleProvider {

        public MyPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return FragmentFactory.FRAGMENT_PAGES_TAB.length;
        }

        @Override
        public Fragment getItem(final int position) {
            return FragmentFactory.getFragmentTabPage(position);
        }

        @Override
        public String getTitle(final int pos) {
            return getActivity().getResources().getStringArray(
                    R.array.page_titles)[pos];

        }
    }

    /**
     * for access from outside
     */
    public void gotoPage(final int pageId) {
        mViewPager.setCurrentItem(pageId);
    }

    class OnIndicatorClickListener implements
            ViewPagerIndicator.OnClickListener {
        @Override
        public void onCurrentClicked(final View v) {
        }

        @Override
        public void onNextClicked(final View v) {
            mViewPager.setCurrentItem(Math.min(mPagerAdapter.getCount() - 1,
                    mIndicator.getCurrentItem() + 1));
        }

        @Override
        public void onPreviousClicked(final View v) {
            mViewPager.setCurrentItem(Math.max(0,
                    mIndicator.getCurrentItem() - 1));
        }

    }

}
