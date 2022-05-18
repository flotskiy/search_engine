package com.github.flotskiy.search.engine.service.search;

import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.Page;
import com.github.flotskiy.search.engine.model.SearchResultPage;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.model.search.QueryHolder;
import com.github.flotskiy.search.engine.util.JsoupHelper;
import com.github.flotskiy.search.engine.util.StringHelper;
import com.github.flotskiy.search.engine.service.lemmatizer.Lemmatizer;
import com.github.flotskiy.search.engine.model.Lemma;
import org.jsoup.nodes.Document;

import java.util.*;
import java.util.stream.Collectors;

public class QueryHandler {

    public static List<SearchResultPage> getSearchResult(RepositoriesHolder repositoriesHolder, QueryHolder queryHolder) {
        List<Lemma> lemmasQueryList =
                getSortedLemmasQueryListWithFrequencyLessThan95(repositoriesHolder, queryHolder.getQuery());
        if (lemmasQueryList.size() < 1) {
            return leaveSearchResultMethodAndReturnEmptyList();
        }

        List<Integer> lemmasIdList = lemmasQueryList.stream().map(Lemma::getId).collect(Collectors.toList());
        List<String> lemmasStringList = lemmasQueryList.stream().map(Lemma::getLemma).collect(Collectors.toList());

        int siteId = -1;
        String siteUrl = queryHolder.getSite();
        Iterable<Site> siteIterable = repositoriesHolder.getSiteRepository().getAllSites();
        for (Site site : siteIterable) {
            if (site.getUrl().equals(siteUrl)) {
                siteId = site.getId();
            }
        }

        Set<Page> pages = getPagesSet(lemmasQueryList, repositoriesHolder, siteId);
        if (pages.size() < 1) {
            return leaveSearchResultMethodAndReturnEmptyList();
        }

        List<SearchResultPage> searchResultPageList = new ArrayList<>();
        String site, siteName, uri, title, snippet;
        float relevance;
        Document document;
        for (Page page : pages) {
            Site tempSite = page.getSiteId();
            site = StringHelper.cutSlash(tempSite.getUrl());
            siteName = tempSite.getName();
            uri = page.getPath();
            document = JsoupHelper.getDocument(page.getContent());
            title = JsoupHelper.getTitle(document);
            snippet = getSnippet(document, lemmasStringList);
            relevance = repositoriesHolder.getIndexRepository().getTotalLemmasRankForPage(page.getId(), lemmasIdList);

            SearchResultPage searchResultPage = new SearchResultPage(site, siteName, uri, title, snippet, relevance);
            searchResultPageList.add(searchResultPage);
        }
        searchResultPageList
                .sort(Comparator.comparing(SearchResultPage::getRelevance).thenComparing(SearchResultPage::getTitle));
        convertAbsoluteRelevanceToRelative(searchResultPageList);
        return searchResultPageList;
    }

    public static Set<Page> getPagesSet(List<Lemma> lemmasQueryList, RepositoriesHolder repositoriesHolder, int siteId) {
        int firstLemmaId = lemmasQueryList.get(0).getId();
        Set<Page> pagesResultSet = new HashSet<>();
        Set<Page> pagesTempSet = new HashSet<>();
        Iterable<Page> pagesIterable = repositoriesHolder.getPagesByLemmaAndSiteId(firstLemmaId, siteId);
        pagesIterable.forEach(pagesResultSet::add);

        for (int i = 1; i < lemmasQueryList.size(); i++) {
            pagesTempSet.clear();
            pagesIterable = repositoriesHolder.getPagesByLemmaAndSiteId(lemmasQueryList.get(i).getId(), siteId);
            pagesIterable.forEach(pagesTempSet::add);
            pagesResultSet.retainAll(pagesTempSet);
        }
        return pagesResultSet;
    }

    public static List<Lemma> getSortedLemmasQueryListWithFrequencyLessThan95(
            RepositoriesHolder repositoriesHolder, String query
            ) {
        if (query == null || query.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        Set<String> queryWordsSet = Lemmatizer.getLemmasCountMap(query).keySet();

        Iterable<Lemma> queryLemmasIterable =
                repositoriesHolder.getLemmasWithQueryWordsAndWithOccurrenceFrequencyPerCentLessThan95(queryWordsSet);

        List<Lemma> lemmasList = new ArrayList<>();
        queryLemmasIterable.forEach(lemmasList::add);
        lemmasList.sort((l1, l2) -> l1.getFrequency() < l2.getFrequency() ? -1 : 1);
        return lemmasList;
    }

    private static void convertAbsoluteRelevanceToRelative(List<SearchResultPage> searchResultPageList) {
        float maxRelevanceValue = searchResultPageList.get(0).getRelevance();
        for (SearchResultPage result : searchResultPageList) {
            result.setRelevance(result.getRelevance() / maxRelevanceValue);
        }
    }

    private static String getSnippet(Document document, List<String> queryList) {
        String documentText = document.text();
        List<String> textList = new ArrayList<>(Arrays.asList(documentText.split("\\s+")));
        List<String> textListLemmatized = Lemmatizer.getLemmatizedList(textList);

        Map<Integer, String> textMapLemmatized =
                textListLemmatized.stream().collect(HashMap::new, (map, s) -> map.put(map.size(), s), Map::putAll);
        Map<Integer, String> filteredMap = textMapLemmatized.entrySet().stream()
                .filter(e -> {
                    for (String queryWord : queryList) {
                        if (queryWord.equals(e.getValue())) {
                            return true;
                        }
                    }
                    return false;
                }).collect(HashMap::new, (map, e) -> map.put(e.getKey(), e.getValue()), Map::putAll);
        List<Integer> lemmasPositions = new ArrayList<>(filteredMap.keySet());
        lemmasPositions.sort(Integer::compareTo);

        return StringHelper.buildSnippet(textList, lemmasPositions);
    }

    private static List<SearchResultPage> leaveSearchResultMethodAndReturnEmptyList() {
        System.out.println("Nothing found!");
        return Collections.EMPTY_LIST;
    }
}
