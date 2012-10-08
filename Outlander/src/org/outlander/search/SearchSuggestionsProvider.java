package org.outlander.search;

import android.content.SearchRecentSuggestionsProvider;

public class SearchSuggestionsProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "org.outlander.search.SearchSuggestionsProvider";
    public final static int    MODE      = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES;

    public SearchSuggestionsProvider() {
        super();
        setupSuggestions(SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);
    }
}
