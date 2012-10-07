package at.the.gogo.parkoid.activities;

import android.os.Bundle;
import android.widget.TextView;
import at.the.gogo.parkoid.fragments.ParkuhrFragment;
import at.the.gogo.parkoid.util.CoreInfoHolder;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ParkuhrActivity extends SherlockFragmentActivity {

    TextView map;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (CoreInfoHolder.getInstance().getContext() == null) {
            CoreInfoHolder.getInstance().setContext(this);
        }

        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new ParkuhrFragment()).commit();
        }

        // setContentView(R.layout.parkuhr);
        //
        // map = (TextView) findViewById(R.id.showmap);
        //
        // map.setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(final View v) {
        // showMap();
        // }
        // });
    }

}
