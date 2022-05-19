package com.github.flotskiy.search.engine.service.search;

import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.search.SearchResultPage;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.model.search.*;
import com.github.flotskiy.search.engine.util.StringHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchService {

    public static boolean isQueryExists(String query) {
        return StringHelper.isStringExists(query);
    }

    public static SearchResultTrue getSearchResult(RepositoriesHolder repositoriesHolder, QueryHolder queryHolder) {
        List<SearchResultPage> searchResultPageList = QueryHandler.getSearchResult(repositoriesHolder, queryHolder);

        SearchResultTrue searchResultTrue = new SearchResultTrue();
        int noOfPagesResult = searchResultPageList.size();
        searchResultTrue.setCount(noOfPagesResult);

        int dataArrayStartIndex = queryHolder.getOffset();
        int dataArrayEndIndex = Math.min(noOfPagesResult, dataArrayStartIndex + queryHolder.getLimit());
        int dataValueSize;
        if (noOfPagesResult < dataArrayStartIndex) {
            dataValueSize = 0;
        } else {
            dataValueSize = dataArrayEndIndex - dataArrayStartIndex;
        }
        List<SearchResultPage> dataValue = new ArrayList<>();
        for (int i = dataArrayStartIndex; i < dataArrayStartIndex + dataValueSize; i++) {
            dataValue.add(searchResultPageList.get(i));
        }
        searchResultTrue.setData(dataValue);
        return searchResultTrue;
    }

    public static boolean hasIndexingAndFailedSites(RepositoriesHolder repositoriesHolder, String siteName) {
        Iterable<Site> siteIterable = repositoriesHolder.getIndexingAndFailedSites();
        if (siteName == null || siteName.isEmpty()) {
            return siteIterable.iterator().hasNext();
        }
        for (Site site : siteIterable) {
            if (site.getUrl().equals(siteName)) {
                return true;
            }
        }
        return false;
    }
}
