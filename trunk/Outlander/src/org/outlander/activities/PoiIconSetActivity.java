package org.outlander.activities;

import org.outlander.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PoiIconSetActivity extends Activity {
    private final int indx[] = { R.drawable.poi, R.drawable.poiblue,
            R.drawable.poigreen, R.drawable.poiyellow,

            R.drawable.marker_climbing, R.drawable.marker_hanggliding,
            R.drawable.marker_hiking, R.drawable.marker_horseriding,
            R.drawable.marker_jogging, R.drawable.marker_kayak,
            R.drawable.marker_nordicski, R.drawable.marker_panoramic,
            R.drawable.marker_paragliding, R.drawable.marker_parking,
            R.drawable.marker_photo, R.drawable.marker_picnic,
            R.drawable.marker_restaurantgourmet, R.drawable.marker_ruins,
            R.drawable.marker_skiing, R.drawable.marker_snowshoeing,
            R.drawable.marker_tent, R.drawable.marker_tower,
            R.drawable.marker_water, R.drawable.marker_waterfall,
            R.drawable.marker_zoo

                             };
    private GridView  mGridInt;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.poiiconset);

        mGridInt = (GridView) findViewById(R.id.GridInt);
        mGridInt.setAdapter(new AppsAdapter());

        mGridInt.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1,
                    final int arg2, final long arg3) {
                // Toast.makeText(PoiIconSetActivity.this, "sel="+arg3,
                // Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK,
                        (new Intent()).putExtra("iconid", indx[arg2]));
                finish();
            }
        });
    }

    public class AppsAdapter extends BaseAdapter {
        public AppsAdapter() {
        }

        @Override
        public View getView(final int position, final View convertView,
                final ViewGroup parent) {
            ImageView i;

            if (convertView == null) {
                i = new ImageView(PoiIconSetActivity.this);
                i.setScaleType(ImageView.ScaleType.FIT_CENTER);
                i.setLayoutParams(new GridView.LayoutParams(50, 50));
            } else {
                i = (ImageView) convertView;
            }

            i.setImageResource(indx[position]);

            return i;
        }

        @Override
        public final int getCount() {
            return indx.length;
        }

        @Override
        public final Object getItem(final int position) {
            return null;
        }

        @Override
        public final long getItemId(final int position) {
            return indx[position];
        }
    }

}
