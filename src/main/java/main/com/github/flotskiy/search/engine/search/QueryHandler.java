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

    public List<Object[]> getPagesJoinedIndexList() {
        String query = StringHelper.getInputString();
        Set<String> queryWordsSet = Lemmatizer.getLemmasCountMap(query).keySet();

        List<Lemma> lemmasQueryList = getLemmasQueryList(queryWordsSet);
        for (Lemma lemma : lemmasQueryList) {
            System.out.println(lemma.getFrequency() + " - " + lemma.getLemma());
        }

        if (lemmasQueryList.size() < 1) {
            return Collections.EMPTY_LIST;
        }

        int firstLemmaId = lemmasQueryList.get(0).getId();
//        List<Page> pagesList = new ArrayList<>();
//        Iterable<Page> pagesIterable = holder.getPageRepository().getPagesByFirstLemmaId(firstLemmaId);
//        for (Page page : pagesIterable) {
//            System.out.println(page.getPath() + " - " + page.getId());
//        }
//        pagesIterable.forEach(pagesList::add);

        Iterable<Object[]> pagesJoinedWithIndexIterable =
                holder.getPageRepository().getPagesJoinedWithIndex(firstLemmaId);

        for (Object[] object : pagesJoinedWithIndexIterable) {
            System.out.println(object[0] + " - " + object[1] + " - " + object[2].toString().length() +
                    " - " + object[3] + " - " + object[4] + " - " + object[5] + " - " + object[6]);
        }

        List<Object[]> pagesJoinedWithIndexList =
                new ArrayList<>((Collection<? extends Object[]>) pagesJoinedWithIndexIterable);

        if (lemmasQueryList.size() == 1) {
            return pagesJoinedWithIndexList;
        }

//        for (int i = 0; i < lemmasQueryList.size() - 1; i++) {
//            int lemmaId = lemmasQueryList.get(i).getId();
//            for (Object[] objects : new ArrayList<>(pagesJoinedWithIndexList)) {
//                boolean contains = false;
//                if (objects[0]);
//                if (contains) {
//                    pagesJoinedWithIndexList.remove(objects);
//                }
//            }
//        }



        // todo:


        return null;
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
