package main.com.github.flotskiy.search.engine.search;

import main.com.github.flotskiy.search.engine.dataholders.RepoInfoExtractor;
import main.com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import main.com.github.flotskiy.search.engine.lemmatizer.Lemmatizer;
import main.com.github.flotskiy.search.engine.model.Lemma;
import main.com.github.flotskiy.search.engine.model.Page;
import main.com.github.flotskiy.search.engine.util.JsoupHelper;
import main.com.github.flotskiy.search.engine.util.StringHelper;
import org.jsoup.nodes.Document;
import org.springframework.data.util.Streamable;

import java.util.*;
import java.util.stream.Collectors;

public class QueryHandler {

    public static List<SearchResultPage> getSearchResult(RepositoriesHolder holder) {
        List<Lemma> lemmasQueryList = getSortedLemmasQueryListWithFrequencyLessThan95(holder);
        List<Integer> lemmasIdList = lemmasQueryList.stream().map(Lemma::getId).collect(Collectors.toList());
        Set<Page> pages = getPagesSet(holder, lemmasQueryList);

        System.out.println("\nretainAll for sets testing");
        pages.forEach(p ->System.out.println(p.getId() + " - " + p.getPath()));

        List<SearchResultPage> searchResultPageList = new ArrayList<>();

        String uri = "";
        String title = "";
        String snippet = "";
        float relevance = 0f;
        Document document = null;

        for (Page page : pages) {
            uri = page.getPath();

            document = JsoupHelper.getDocument(page.getContent());
            title = JsoupHelper.getTitle(document);

            relevance = holder.getIndexRepository().getTotalLemmasRankForPage(page.getId(), lemmasIdList);

            // todo: see document.getElementsContainingOwnText()

            SearchResultPage searchResultPage = new SearchResultPage(uri, title, snippet, relevance);
            searchResultPageList.add(searchResultPage);
        }
        searchResultPageList.sort((o1, o2) -> Float.compare(o2.getRelevance(), o1.getRelevance()));
        searchResultPageList.forEach(System.out::println);
        convertAbsoluteRelevanceToRelative(searchResultPageList);
        searchResultPageList.forEach(System.out::println);
        return searchResultPageList;
    }

    public static Set<Page> getPagesSet(RepositoriesHolder holder, List<Lemma> lemmasQueryList) {
        if (lemmasQueryList.size() < 1) {
            return Collections.EMPTY_SET;
        }

        int lemmaId = lemmasQueryList.get(0).getId();
        Set<Page> pagesResultSet = new HashSet<>();
        Set<Page> pagesTempSet = new HashSet<>();
        Iterable<Page> pagesIterable = holder.getPageRepository().getPagesByLemmaId(lemmaId);
        for (Page page : pagesIterable) {
            System.out.println(page.getPath() + " - " + page.getId());
        }
        pagesIterable.forEach(pagesResultSet::add);

        for (int i = 1; i < lemmasQueryList.size(); i++) {
            pagesIterable = holder.getPageRepository().getPagesByLemmaId(lemmasQueryList.get(i).getId());
            pagesIterable.forEach(pagesTempSet::add);
            pagesResultSet.retainAll(pagesTempSet);
        }
        return pagesResultSet;
    }

    public static List<Lemma> getSortedLemmasQueryListWithFrequencyLessThan95(RepositoriesHolder holder) {
        String query = StringHelper.getInputString();
        Set<String> queryWordsSet = Lemmatizer.getLemmasCountMap(query).keySet();

        Iterable<Lemma> frequentlyOccurringLemmasIterable =
                RepoInfoExtractor.getLemmasWithOccurrenceFrequencyPerCentMoreThan95(holder);
        Set<Lemma> frequentlyOccurringLemmasSet = Streamable.of(frequentlyOccurringLemmasIterable).toSet();

        Iterable<Lemma> queryLemmasIterable = RepoInfoExtractor.getLemmasFromQueryWords(holder, queryWordsSet);
        Set<Lemma> queryLemmasSet = Streamable.of(queryLemmasIterable).toSet();
        Set<Lemma> modifiableTempSet = new HashSet<>(queryLemmasSet);
        modifiableTempSet.removeAll(frequentlyOccurringLemmasSet);

        List<Lemma> lemmasList = new ArrayList<>(modifiableTempSet);
        lemmasList.sort((l1, l2) -> l1.getFrequency() < l2.getFrequency() ? -1 : 1);

        for (Lemma lemma : lemmasList) {
            System.out.println(lemma.getFrequency() + " - " + lemma.getLemma());
        }

        return lemmasList;
    }

    private static void convertAbsoluteRelevanceToRelative(List<SearchResultPage> searchResultPageList) {
        System.out.println("convertAbsoluteRelevanceToRelative");
        float maxRelevanceValue = searchResultPageList.get(0).getRelevance();
        for (SearchResultPage result : searchResultPageList) {
            result.setRelevance(result.getRelevance() / maxRelevanceValue);
        }
    }
}
