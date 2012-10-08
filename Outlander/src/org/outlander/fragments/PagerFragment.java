package org.outlander.fragments;

import org.outlander.R;
import org.outlander.activities.ViewPagerIndicator;
import org.outlander.utils.CoreInfoHandler;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;

public class PagerFragment extends SherlockFragment {

    private MyPagerAdapter mPagerAdapter;
    private ViewPager      mViewPager;
    ViewPagerIndicator     mIndicator;

    public static PagerFragment newInstance() {
        final PagerFragment fragment = new PagerFragment();

        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.pager, container, false);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mPagerAdapter = new MyPagerAdapter(getActivity().getSupportFragmentManager()); // ABS
                                                                                       // 4.0

        mViewPager = (ViewPager) view.findViewById(R.id.viewflipper);
        mViewPager.setAdapter(mPagerAdapter);

        final int lastpageViewed = sharedPreferences.getInt("PageInFlipper", 0);

        mViewPager.setCurrentItem(lastpageViewed);

        final Button nextPage = (Button) view.findViewById(R.id.nextButton);
        nextPage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                int pagenr = mViewPager.getCurrentItem() + 1;

                if (pagenr >= mViewPager.getAdapter().getCount()) {
                    pagenr = 0;
                }
                mViewPager.setCurrentItem(pagenr);
            }
        });

        final Button prevPage = (Button) view.findViewById(R.id.prevButton);
        prevPage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                int pagenr = mViewPager.getCurrentItem() - 1;

                if (pagenr < 0) {
                    pagenr = mViewPager.getAdapter().getCount() - 1;
                }
                mViewPager.setCurrentItem(pagenr);
            }
        });

        // Find the indicator from the layout
        mIndicator = (ViewPagerIndicator) view.findViewById(R.id.indicator);

        // Set the indicator as the pageChangeListener
        mViewPager.setOnPageChangeListener(mIndicator);

        // Initialize the indicator. We need some information here:
        // * What page do we start on.
        // * How many pages are there in total
        // * A callback to get page titles
        mIndicator.init(0, mPagerAdapter.getCount(), mPagerAdapter);
        final Resources res = getResources();
        final Drawable prev = res.getDrawable(R.drawable.indicator_prev_arrow);
        final Drawable next = res.getDrawable(R.drawable.indicator_next_arrow);
        mIndicator.setFocusedTextColor(new int[] { 255, 0, 0 });

        // Set images for previous and next arrows.
        mIndicator.setArrows(prev, next);

        mIndicator.setOnClickListener(new OnIndicatorClickListener());

        return view;
    }

    @Override
    public void onPause() {
        final SharedPreferences uiState = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = uiState.edit();
        editor.putInt("PageInFlipper", mViewPager.getCurrentItem());

        super.onPause();
    }

    public int getCurrentPageId() {
        return mIndicator.getCurrentPosition();
    }

    public class MyPagerAdapter extends FragmentPagerAdapter implements ViewPagerIndicator.PageInfoProvider {

        public MyPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return CoreInfoHandler.getInstance().getPageSet().length;
        }

        @Override
        public Fragment getItem(final int position) {
            return null; // mainMapActivity
            // .getFragmentPage(CoreInfoHandler.FRAGMENT_PAGES_TAB[position]);
        }

        @Override
        public String getTitle(final int pos) {
            return null; // mainMapActivity.getResources().getStringArray(R.array.pager_titles)[pos];

        }
    }

    class OnIndicatorClickListener implements ViewPagerIndicator.OnClickListener {

        @Override
        public void onCurrentClicked(final View v) {
        }

        @Override
        public void onNextClicked(final View v) {
            mViewPager.setCurrentItem(Math.min(mPagerAdapter.getCount() - 1, mIndicator.getCurrentPosition() + 1));
        }

        @Override
        public void onPreviousClicked(final View v) {
            mViewPager.setCurrentItem(Math.max(0, mIndicator.getCurrentPosition() - 1));
        }

    }

}
