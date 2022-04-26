package main.com.github.flotskiy.search.engine.dataholders;

import main.com.github.flotskiy.search.engine.model.Lemma;

import java.util.Set;

public class RepoInfoExtractor {

    public static Iterable<Lemma> getLemmasWithOccurrenceFrequencyPerCentMoreThan95(RepositoriesHolder holder) {
        return holder.getLemmaRepository().getLemmasWithOccurrenceFrequencyPerCentMoreThan95();
    }

    public static Iterable<Lemma> getLemmasFromQueryWords(RepositoriesHolder holder, Set<String> queryWords) {
        return holder.getLemmaRepository().getLemmasWithQueryWords(queryWords);
    }

}
