package main.com.github.flotskiy.search.engine.search;

import main.com.github.flotskiy.search.engine.dataholders.RepoInfoExtractor;
import main.com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import main.com.github.flotskiy.search.engine.lemmatizer.Lemmatizer;
import main.com.github.flotskiy.search.engine.model.Lemma;
import main.com.github.flotskiy.search.engine.model.Page;
import main.com.github.flotskiy.search.engine.util.StringHelper;
import org.springframework.data.util.Streamable;

import java.util.*;

public class QueryHandler {

    private RepositoriesHolder holder;

    public QueryHandler(RepositoriesHolder holder) {
        this.holder = holder;
    }

    public Set<Page> getPagesSet() {
        String query = StringHelper.getInputString();
        Set<String> queryWordsSet = Lemmatizer.getLemmasCountMap(query).keySet();

        List<Lemma> lemmasQueryList = getLemmasQueryList(queryWordsSet);
        for (Lemma lemma : lemmasQueryList) {
            System.out.println(lemma.getFrequency() + " - " + lemma.getLemma());
        }

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

    private List<Lemma> getLemmasQueryList(Set<String> queryWordsSet) {
        Iterable<Lemma> frequentlyOccurringLemmasIterable =
                RepoInfoExtractor.getLemmasWithOccurrenceFrequencyPerCentMoreThan95(holder);
        Set<Lemma> frequentlyOccurringLemmasSet = Streamable.of(frequentlyOccurringLemmasIterable).toSet();

        Iterable<Lemma> queryLemmasIterable = RepoInfoExtractor.getLemmasFromQueryWords(holder, queryWordsSet);
        Set<Lemma> queryLemmasSet = Streamable.of(queryLemmasIterable).toSet();
        Set<Lemma> modifiableTempSet = new HashSet<>(queryLemmasSet);
        modifiableTempSet.removeAll(frequentlyOccurringLemmasSet);

        List<Lemma> lemmasList = new ArrayList<>(modifiableTempSet);
        lemmasList.sort((l1, l2) -> l1.getFrequency() < l2.getFrequency() ? -1 : 1);
        return lemmasList;
    }
}
