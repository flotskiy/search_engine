package com.github.flotskiy.search.engine.indexing;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.Field;
import com.github.flotskiy.search.engine.model.Index;
import com.github.flotskiy.search.engine.model.Lemma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoFiller {

    public static void fillInFields(RepositoriesHolder repositoriesHolder) {
        repositoriesHolder.getFieldRepository().save(new Field("title", "title", 1.0f));
        repositoriesHolder.getFieldRepository().save(new Field("body", "body", 0.8f));
    }

    public static void fillInPages(RepositoriesHolder repositoriesHolder) {
        repositoriesHolder.getPageRepository().saveAll(CollectionsHolder.getPagesList());
    }

    public static void fillInLemmas(RepositoriesHolder repositoriesHolder) {
        System.out.println("Размер lemmasMap - " + CollectionsHolder.getLemmasMap().size());
        for (String key : CollectionsHolder.getLemmasMap().keySet()) {
            Lemma lemma = new Lemma(key, CollectionsHolder.getLemmasMap().get(key));
            CollectionsHolder.getLemmasList().add(lemma);
        }
        repositoriesHolder.getLemmaRepository().saveAll(CollectionsHolder.getLemmasList());
    }

    public static void fillInSearchIndex(RepositoriesHolder repositoriesHolder) {
        Iterable<Lemma> lemmaIterable = repositoriesHolder.getLemmaRepository().findAll();
        Map<String, Lemma> lemmasMapFromDB = new HashMap<>();
        for (Lemma lemma : lemmaIterable) {
            lemmasMapFromDB.put(lemma.getLemma(), lemma);
        }

        List<Index> indexList = new ArrayList<>();
        for (TempIndex tempIndex : CollectionsHolder.getTempIndexesList()) {
            Lemma lemma = lemmasMapFromDB.get(tempIndex.getLemma());
            indexList.add(new Index(tempIndex.getPage(), lemma, tempIndex.getLemmaRank()));
        }

        System.out.println("tempIndexList.size() - " + CollectionsHolder.getTempIndexesList().size());
        System.out.println("indexList.size() - " + indexList.size());
        repositoriesHolder.getIndexRepository().saveAll(indexList);
    }
}
