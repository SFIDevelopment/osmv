package org.outlander.activities;

import org.outlander.fragments.CategoryListFragment;
import org.outlander.utils.Ut;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class TrackCategoryListActivity extends FragmentActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Ut.isMultiPane(this)) {
            finish();
        }
        else {
            final CategoryListFragment fragment = new CategoryListFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }
}
