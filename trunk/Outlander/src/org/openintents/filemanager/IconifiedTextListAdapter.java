package org.openintents.filemanager;

/*
 * $Id: BulletedTextListAdapter.java 57 2007-11-21 18:31:52Z steven $
 * 
 * Copyright 2007 Steven Osborn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

/** @author Steven Osborn - http://steven.bitsetters.com */
public class IconifiedTextListAdapter extends BaseAdapter implements Filterable {

    /** Remember our context so we can use it when constructing views. */
    private final Context mContext;

    private static String lastFilter;

    class IconifiedFilter extends Filter {
        @Override
        protected FilterResults performFiltering(final CharSequence arg0) {

            IconifiedTextListAdapter.lastFilter = (arg0 != null) ? arg0
                    .toString() : null;

            final Filter.FilterResults results = new Filter.FilterResults();

            // No results yet?
            if (mOriginalItems == null) {
                results.count = 0;
                results.values = null;
                return results;
            }

            final int count = mOriginalItems.size();

            if ((arg0 == null) || (arg0.length() == 0)) {
                results.count = count;
                results.values = mOriginalItems;
                return results;
            }

            final List<IconifiedText> filteredItems = new ArrayList<IconifiedText>(
                    count);

            int outCount = 0;
            final CharSequence lowerCs = arg0.toString().toLowerCase();

            for (int x = 0; x < count; x++) {
                final IconifiedText text = mOriginalItems.get(x);

                if (text.getText().toLowerCase().contains(lowerCs)) {
                    // This one matches.
                    filteredItems.add(text);
                    outCount++;
                }
            }

            results.count = outCount;
            results.values = filteredItems;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(final CharSequence arg0,
                final FilterResults arg1) {
            mItems = (List<IconifiedText>) arg1.values;
            notifyDataSetChanged();
        }

        @SuppressWarnings("unchecked")
        List<IconifiedText> synchronousFilter(final CharSequence filter) {
            final FilterResults results = performFiltering(filter);
            return (List<IconifiedText>) (results.values);
        }
    }

    private final IconifiedFilter mFilter        = new IconifiedFilter();

    private List<IconifiedText>   mItems         = new ArrayList<IconifiedText>();
    private List<IconifiedText>   mOriginalItems = new ArrayList<IconifiedText>();

    public IconifiedTextListAdapter(final Context context) {
        mContext = context;
    }

    public void addItem(final IconifiedText it) {
        mItems.add(it);
    }

    public void setListItems(final List<IconifiedText> lit, final boolean filter) {
        mOriginalItems = lit;

        if (filter) {
            mItems = mFilter
                    .synchronousFilter(IconifiedTextListAdapter.lastFilter);
        } else {
            mItems = lit;
        }
    }

    /** @return The number of items in the */
    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(final int position) {
        return mItems.get(position);
    }

    public boolean areAllItemsSelectable() {
        return false;
    }

    /*
     * public boolean isSelectable(int position) { try{ return
     * mItems.get(position).isSelectable(); }catch (IndexOutOfBoundsException
     * aioobe){ return super.isSelectable(position); } }
     */

    /** Use the array index as a unique id. */
    @Override
    public long getItemId(final int position) {
        return position;
    }

    /**
     * @param convertView
     *            The old view to overwrite, if one is passed
     * @returns a IconifiedTextView that holds wraps around an IconifiedText
     */
    @Override
    public View getView(final int position, final View convertView,
            final ViewGroup parent) {
        IconifiedTextView btv;
        if (convertView == null) {
            btv = new IconifiedTextView(mContext, mItems.get(position));
        } else { // Reuse/Overwrite the View passed
            // We are assuming(!) that it is castable!
            btv = (IconifiedTextView) convertView;
        }
        btv.setText(mItems.get(position).getText());
        btv.setInfo(mItems.get(position).getInfo());
        return btv;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }
}