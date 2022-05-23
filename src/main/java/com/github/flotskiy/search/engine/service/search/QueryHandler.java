package com.github.flotskiy.search.engine.service.search;

import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.Page;
import com.github.flotskiy.search.engine.model.search.SearchResultPage;
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

    public static List<SearchResultPage> getSearchResult(RepositoriesHolder repoHolder, QueryHolder queryHolder) {
        Iterable<Site> siteIterable = repoHolder.findAllSites();
        List<Lemma> lemmasQueryList = getSortedLemmasQueryList(repoHolder, queryHolder.getQuery());
        Set<Integer> removedSiteIdSet =
                cleanLemmasQueryListFromFrequentLemmas(lemmasQueryList, repoHolder, siteIterable);
        if (lemmasQueryList.size() < 1) {
            return leaveSearchResultMethodAndReturnEmptyList();
        }
        int siteId = -1;
        String siteUrl = queryHolder.getSite();
        for (Site site : siteIterable) {
            if (site.getUrl().equals(siteUrl)) {
                siteId = site.getId();
            }
        }
        Set<Page> pages = getPagesSet(lemmasQueryList, repoHolder, siteId);
        pages.removeIf(page -> removedSiteIdSet.contains(page.getSiteId().getId()));
        if (pages.size() < 1) {
            return leaveSearchResultMethodAndReturnEmptyList();
        }
        List<Integer> lemmasIdList = lemmasQueryList.stream().map(Lemma::getId).collect(Collectors.toList());
        List<String> lemmasStringList = lemmasQueryList.stream().map(Lemma::getLemma).collect(Collectors.toList());
        Map<List<Integer>, List<String>> map = new HashMap<>();
        map.put(lemmasIdList, lemmasStringList);
        List<SearchResultPage> searchResultPageList = new ArrayList<>();
        for (Page page : pages) {
            addNewSearchResult(searchResultPageList, page, repoHolder, map);
        }
        searchResultPageList.sort(Comparator.comparing(SearchResultPage::getRelevance).reversed()
                .thenComparing(SearchResultPage::getTitle));
        convertAbsoluteRelevanceToRelative(searchResultPageList);
        return searchResultPageList;
    }

    private static void addNewSearchResult(
            List<SearchResultPage> listToAdd,
            Page page,
            RepositoriesHolder repoHolder,
            Map<List<Integer>, List<String>> map
    ) {
        Site tempSite = page.getSiteId();
        String site = StringHelper.cutSlash(tempSite.getUrl());
        String siteName = tempSite.getName();
        String uri = page.getPath();

        Document document = JsoupHelper.getDocument(page.getContent());
        String title = JsoupHelper.getTitle(document);

        Map.Entry<List<Integer>, List<String>> entry = map.entrySet().stream().findFirst().get();
        List<Integer> lemmasIdList = entry.getKey();
        List<String> lemmasStringList = entry.getValue();

        String snippet = getSnippet(document, lemmasStringList);
        float relevance = repoHolder.getTotalLemmasRankForPage(page.getId(), lemmasIdList);

        SearchResultPage searchResultPage = new SearchResultPage(site, siteName, uri, title, snippet, relevance);
        listToAdd.add(searchResultPage);
    }

    private static Set<Page> getPagesSet(
            List<Lemma> lemmasQueryList, RepositoriesHolder repoHolder, int siteIdFromQueryHolder
    ) {
        String firstLemma = lemmasQueryList.get(0).getLemma();
        Set<Page> pagesResultSet = new HashSet<>();
        Set<Page> pagesTempSet = new HashSet<>();
        Iterable<Page> pagesIterable = repoHolder.getPagesByLemmaAndSiteId(firstLemma, siteIdFromQueryHolder);
        pagesIterable.forEach(pagesResultSet::add);

        for (int i = 1; i < lemmasQueryList.size(); i++) {
            pagesTempSet.clear();
            pagesIterable = repoHolder.getPagesByLemmaAndSiteId(
                    lemmasQueryList.get(i).getLemma(), siteIdFromQueryHolder
            );
            pagesIterable.forEach(pagesTempSet::add);
            pagesResultSet.retainAll(pagesTempSet);
        }
        return pagesResultSet;
    }

    private static List<Lemma> getSortedLemmasQueryList(RepositoriesHolder repoHolder, String query) {
        if (query == null || query.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        Set<String> queryWordsSet = Lemmatizer.getLemmasCountMap(query).keySet();
        Iterable<Lemma> queryLemmasIterable = repoHolder.getLemmasWithQueryWords(queryWordsSet);
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

    private static Set<Integer> cleanLemmasQueryListFromFrequentLemmas(
            List<Lemma> lemmaList, RepositoriesHolder repoHolder, Iterable<Site> siteIterable
    ) {
        Map<Integer, Float> siteIdAnd95perCentOfAllPages = new HashMap<>();
        for (Site site : siteIterable) {
            int id = site.getId();
            float occurrenceOf95perCent = repoHolder.get95perCentPagesCount(id);
            siteIdAnd95perCentOfAllPages.put(id, occurrenceOf95perCent);
        }

        Set<Integer> removedSiteIdSet = new HashSet<>();
        for (Lemma lemma : new ArrayList<>(lemmaList)) {
            if (lemma.getFrequency() > siteIdAnd95perCentOfAllPages.get(lemma.getSiteId().getId())) {
                removedSiteIdSet.add(lemma.getSiteId().getId());
                lemmaList.remove(lemma);
            }
        }
        return removedSiteIdSet;
    }
}
