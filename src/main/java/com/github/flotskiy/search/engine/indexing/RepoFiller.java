package com.github.flotskiy.search.engine.indexing;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.*;

import java.util.*;

public class RepoFiller {

    public static void fillInFields(RepositoriesHolder repositoriesHolder) {
        repositoriesHolder.getFieldRepository().save(new Field("title", "title", 1.0f));
        repositoriesHolder.getFieldRepository().save(new Field("body", "body", 0.8f));
    }

    public static void fillInSites(RepositoriesHolder repositoriesHolder) {
        repositoriesHolder.getSiteRepository().saveAll(CollectionsHolder.getSiteList());
    }

    public static void fillInPages(RepositoriesHolder repositoriesHolder) {
        repositoriesHolder.getPageRepository().saveAll(CollectionsHolder.getPageList());
        CollectionsHolder.getPageList().clear();
    }

    public static void fillInLemmas(RepositoriesHolder repositoriesHolder) {
        System.out.println("lemmasMap size - " + CollectionsHolder.getSiteLemmaMap().size());
        for (SiteLemmaPair pair : CollectionsHolder.getSiteLemmaMap().keySet()) {
            Lemma lemma = new Lemma(pair.getLemma(), CollectionsHolder.getSiteLemmaMap().get(pair), pair.getSite());
            CollectionsHolder.getLemmaList().add(lemma);
        }
        repositoriesHolder.getLemmaRepository().saveAll(CollectionsHolder.getLemmaList());
        CollectionsHolder.getSiteLemmaMap().clear();
        CollectionsHolder.getLemmaList().clear();
    }

    public static void fillInSearchIndex(RepositoriesHolder repositoriesHolder) {
        Iterable<Lemma> lemmaIterable = repositoriesHolder.getLemmaRepository().findAll();
        Map<String, Lemma> lemmasMapFromDB = new HashMap<>();
        for (Lemma lemma : lemmaIterable) {
            lemmasMapFromDB.put(lemma.getLemma(), lemma);
        }

        List<Index> indexList = new ArrayList<>();
        for (TempIndex tempIndex : CollectionsHolder.getTempIndexList()) {
            Lemma lemma = lemmasMapFromDB.get(tempIndex.getLemma());
            indexList.add(new Index(tempIndex.getPage(), lemma, tempIndex.getLemmaRank()));
        }

        System.out.println("tempIndexList.size() - " + CollectionsHolder.getTempIndexList().size());
        System.out.println("indexList.size() - " + indexList.size());
        repositoriesHolder.getIndexRepository().saveAll(indexList);
        CollectionsHolder.getTempIndexList().clear();
    }

    public static void deletePreviouslyIndexedSiteByName(
            RepositoriesHolder repositoriesHolder, String siteName, int siteId
    ) {
        repositoriesHolder.getSiteRepository().deletePreviouslyIndexedSiteByName(siteName, siteId);
    }

    public static void markSiteAsIndexed(RepositoriesHolder repositoriesHolder, Site site) {
        site.setStatus(Status.INDEXED);
        site.setStatusTime(new Date());
        repositoriesHolder.getSiteRepository().save(site);
    }
}
