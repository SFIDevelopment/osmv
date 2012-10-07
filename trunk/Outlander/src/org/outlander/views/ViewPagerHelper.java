package org.outlander.views;

import org.outlander.R;
import org.outlander.fragments.FragmentFactory;
import org.outlander.fragments.PageChangeNotifyer;
import org.outlander.utils.CoreInfoHandler;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
import com.viewpagerindicator.TitleProvider;

public class ViewPagerHelper {

    private MyPagerAdapter     mPagerAdapter;
    private ViewPager          mViewPager;
    private TitlePageIndicator mIndicator;

    public View getViewPagerView(final SherlockFragmentActivity activity) {

        CoreInfoHandler.getInstance()
                .setPageSet(FragmentFactory.FRAGMENT_PAGES);

        mPagerAdapter = new MyPagerAdapter(
                activity.getSupportFragmentManager(), activity);

        final LayoutInflater inflater = LayoutInflater.from(activity);
        final View view = inflater.inflate(R.layout.pager, null, false);

        mViewPager = (ViewPager) view.findViewById(R.id.viewflipper);

        mViewPager.setAdapter(mPagerAdapter);

        CoreInfoHandler.getInstance().setViewPager(mViewPager);

        //
        mIndicator = (TitlePageIndicator) view.findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);
        mIndicator.setFooterIndicatorStyle(IndicatorStyle.Underline);

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
                    final PageChangeNotifyer oldPage = CoreInfoHandler
                            .getInstance().getPageChangeNotifyer(
                                    mIndicator.getCurrentItem());

                    if (oldPage != null) {
                        oldPage.pageGetsDeactivated();
                    }

                    final PageChangeNotifyer newPage = CoreInfoHandler
                            .getInstance().getPageChangeNotifyer(position);
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

        return view;
    }

    public class MyPagerAdapter extends FragmentPagerAdapter implements
            TitleProvider {

        private String[] titles;

        public MyPagerAdapter(final FragmentManager fman,
                final Activity activity) {
            super(fman);
            titles = activity.getResources().getStringArray(
                    R.array.pager_titles_tab);
        }

        public MyPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return CoreInfoHandler.getInstance().getPageSet().length;
        }

        @Override
        public Fragment getItem(final int position) {
            return FragmentFactory.getFragmentTabPage(position);
        }

        @Override
        public String getTitle(final int pos) {
            return titles[pos];
        }
    }

    public class SwitchPageTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(final Integer... paramArrayOfParams) {

            return paramArrayOfParams[0];
        }

        @Override
        protected void onPostExecute(final Integer pageId) {

            CoreInfoHandler.getInstance().gotoPage(
                    FragmentFactory.getFragmentTabPageIndexById(pageId));

        }
    }

}