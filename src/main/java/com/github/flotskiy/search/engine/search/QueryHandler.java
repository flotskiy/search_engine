package com.github.flotskiy.search.engine.search;

import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.Page;
import com.github.flotskiy.search.engine.model.SearchResultPage;
import com.github.flotskiy.search.engine.util.JsoupHelper;
import com.github.flotskiy.search.engine.util.StringHelper;
import com.github.flotskiy.search.engine.lemmatizer.Lemmatizer;
import com.github.flotskiy.search.engine.model.Lemma;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class QueryHandler {

    private final RepositoriesHolder repositoriesHolder;

    @Autowired
    public QueryHandler(RepositoriesHolder repositoriesHolder) {
        this.repositoriesHolder = repositoriesHolder;
    }

    public List<SearchResultPage> getSearchResult() {
        List<Lemma> lemmasQueryList = getSortedLemmasQueryListWithFrequencyLessThan95();
        List<Integer> lemmasIdList = lemmasQueryList.stream().map(Lemma::getId).collect(Collectors.toList());
        List<String> lemmasStringList = lemmasQueryList.stream().map(Lemma::getLemma).collect(Collectors.toList());
        Set<Page> pages = getPagesSet(lemmasQueryList);
        if (pages.isEmpty()) {
            System.out.println("Nothing found!");
            return Collections.EMPTY_LIST;
        }

        List<SearchResultPage> searchResultPageList = new ArrayList<>();
        String uri, title, snippet;
        float relevance;
        Document document;

        for (Page page : pages) {
            uri = page.getPath();
            document = JsoupHelper.getDocument(page.getContent());
            title = JsoupHelper.getTitle(document);
            snippet = getSnippet(document, lemmasStringList);
            relevance = repositoriesHolder.getIndexRepository().getTotalLemmasRankForPage(page.getId(), lemmasIdList);

            SearchResultPage searchResultPage = new SearchResultPage(uri, title, snippet, relevance);
            searchResultPageList.add(searchResultPage);
        }
        searchResultPageList.sort((o1, o2) -> Float.compare(o2.getRelevance(), o1.getRelevance()));
        convertAbsoluteRelevanceToRelative(searchResultPageList);
        return searchResultPageList;
    }

    public Set<Page> getPagesSet(List<Lemma> lemmasQueryList) {
        if (lemmasQueryList.size() < 1) {
            return Collections.EMPTY_SET;
        }

        int lemmaId = lemmasQueryList.get(0).getId();
        Set<Page> pagesResultSet = new HashSet<>();
        Set<Page> pagesTempSet = new HashSet<>();
        Iterable<Page> pagesIterable = repositoriesHolder.getPageRepository().getPagesByLemmaId(lemmaId);
        pagesIterable.forEach(pagesResultSet::add);

        for (int i = 1; i < lemmasQueryList.size(); i++) {
            pagesTempSet.clear();
            pagesIterable = repositoriesHolder.getPageRepository().getPagesByLemmaId(lemmasQueryList.get(i).getId());
            pagesIterable.forEach(pagesTempSet::add);
            pagesResultSet.retainAll(pagesTempSet);
        }
        return pagesResultSet;
    }

    public List<Lemma> getSortedLemmasQueryListWithFrequencyLessThan95() {
        String query = StringHelper.getInputString();
        Set<String> queryWordsSet = Lemmatizer.getLemmasCountMap(query).keySet();

        Iterable<Lemma> frequentlyOccurringLemmasIterable =
                repositoriesHolder.getLemmasWithOccurrenceFrequencyPerCentMoreThan95();
        Set<Lemma> frequentlyOccurringLemmasSet = Streamable.of(frequentlyOccurringLemmasIterable).toSet();

        Iterable<Lemma> queryLemmasIterable = repositoriesHolder.getLemmasFromQueryWords(queryWordsSet);
        Set<Lemma> queryLemmasSet = Streamable.of(queryLemmasIterable).toSet();
        Set<Lemma> modifiableTempSet = new HashSet<>(queryLemmasSet);
        modifiableTempSet.removeAll(frequentlyOccurringLemmasSet);

        List<Lemma> lemmasList = new ArrayList<>(modifiableTempSet);
        lemmasList.sort((l1, l2) -> l1.getFrequency() < l2.getFrequency() ? -1 : 1);

        return lemmasList;
    }

    private void convertAbsoluteRelevanceToRelative(List<SearchResultPage> searchResultPageList) {
        float maxRelevanceValue = searchResultPageList.get(0).getRelevance();
        for (SearchResultPage result : searchResultPageList) {
            result.setRelevance(result.getRelevance() / maxRelevanceValue);
        }
    }

    private String getSnippet(Document document, List<String> queryList) {
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
}
