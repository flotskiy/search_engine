package com.github.flotskiy.search.engine.indexing;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RepoFiller {

    private final RepositoriesHolder repositoriesHolder;
    private final CollectionsHolder collectionsHolder;

    @Autowired
    public RepoFiller(RepositoriesHolder repositoriesHolder, CollectionsHolder collectionsHolder) {
        this.repositoriesHolder = repositoriesHolder;
        this.collectionsHolder = collectionsHolder;
    }

    public void fillInFields() {
        repositoriesHolder.getFieldRepository().save(new Field("title", "title", 1.0f));
        repositoriesHolder.getFieldRepository().save(new Field("body", "body", 0.8f));
    }

    public void fillInSites() {
        repositoriesHolder.getSiteRepository().saveAll(collectionsHolder.getSiteList());
    }

    public void fillInPages() {
        repositoriesHolder.getPageRepository().saveAll(collectionsHolder.getPageList());
        collectionsHolder.getPageList().clear();
    }

    public void fillInLemmas() {
        System.out.println("lemmasMap size - " + collectionsHolder.getSiteLemmaMap().size());
        for (SiteLemmaPair pair : collectionsHolder.getSiteLemmaMap().keySet()) {
            Lemma lemma = new Lemma(pair.getLemma(), collectionsHolder.getSiteLemmaMap().get(pair), pair.getSite());
            collectionsHolder.getLemmaList().add(lemma);
        }
        repositoriesHolder.getLemmaRepository().saveAll(collectionsHolder.getLemmaList());
        collectionsHolder.getSiteLemmaMap().clear();
        collectionsHolder.getLemmaList().clear();
    }

    public void fillInSearchIndex() {
        Iterable<Lemma> lemmaIterable = repositoriesHolder.getLemmaRepository().findAll();
        Map<String, Lemma> lemmasMapFromDB = new HashMap<>();
        for (Lemma lemma : lemmaIterable) {
            lemmasMapFromDB.put(lemma.getLemma(), lemma);
        }

        List<Index> indexList = new ArrayList<>();
        for (TempIndex tempIndex : collectionsHolder.getTempIndexList()) {
            Lemma lemma = lemmasMapFromDB.get(tempIndex.getLemma());
            indexList.add(new Index(tempIndex.getPage(), lemma, tempIndex.getLemmaRank()));
        }

        System.out.println("tempIndexList.size() - " + collectionsHolder.getTempIndexList().size());
        System.out.println("indexList.size() - " + indexList.size());
        repositoriesHolder.getIndexRepository().saveAll(indexList);
        collectionsHolder.getTempIndexList().clear();
    }

    public void deletePreviouslyIndexedSiteByName(String siteName, int siteId) {
        repositoriesHolder.getSiteRepository().deletePreviouslyIndexedSiteByName(siteName, siteId);
    }

    public void markSiteAsIndexed(Site site) {
        site.setStatus(Status.INDEXED);
        site.setStatusTime(new Date());
        repositoriesHolder.getSiteRepository().save(site);
    }
}
