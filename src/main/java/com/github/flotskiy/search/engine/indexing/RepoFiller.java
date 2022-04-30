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

    public static void fillInFieldsTable(RepositoriesHolder repositoriesHolder) {
        repositoriesHolder.getFieldRepository().save(new Field("title", "title", 1.0f));
        repositoriesHolder.getFieldRepository().save(new Field("body", "body", 0.8f));
    }

    public static void fillInPagesTable(
            CollectionsHolder collectionsHolder,
            RepositoriesHolder repositoriesHolder
    ) {
        repositoriesHolder.getPageRepository().saveAll(collectionsHolder.getPagesList());
    }

    public static void fillInLemmasTable(
            CollectionsHolder collectionsHolder,
            RepositoriesHolder repositoriesHolder
    ) {
        System.out.println("Размер lemmasMap - " + collectionsHolder.getLemmasMap().size());
        for (String key : collectionsHolder.getLemmasMap().keySet()) {
            Lemma lemma = new Lemma(key, collectionsHolder.getLemmasMap().get(key));
            collectionsHolder.getLemmasList().add(lemma);
        }
        repositoriesHolder.getLemmaRepository().saveAll(collectionsHolder.getLemmasList());
    }

    public static void fillInSearchIndexTable(
            CollectionsHolder collectionsHolder,
            RepositoriesHolder repositoriesHolder
    ) {
        Iterable<Lemma> lemmaIterable = repositoriesHolder.getLemmaRepository().findAll();
        Map<String, Lemma> lemmasMapFromDB = new HashMap<>();
        for (Lemma lemma : lemmaIterable) {
            lemmasMapFromDB.put(lemma.getLemma(), lemma);
        }

        List<Index> indexList = new ArrayList<>();
        for (TempIndex tempIndex : collectionsHolder.getTempIndexesList()) {
            Lemma lemma = lemmasMapFromDB.get(tempIndex.getLemma());
            indexList.add(new Index(tempIndex.getPage(), lemma, tempIndex.getLemmaRank()));
        }

        System.out.println("tempIndexList.size() - " + collectionsHolder.getTempIndexesList().size());
        System.out.println("indexList.size() - " + indexList.size());
        repositoriesHolder.getIndexRepository().saveAll(indexList);
    }
}
