package com.github.flotskiy.search.engine.service.indexing.all;

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
        repositoriesHolder.truncateFields();
        repositoriesHolder.saveNewField(new Field("title", "title", 1.0f));
        repositoriesHolder.saveNewField(new Field("body", "body", 0.8f));
    }

    public void saveSite(Site site) {
        repositoriesHolder.saveNewSite(site);
    }

    public void fillInPages() {
        repositoriesHolder.saveAllPages(collectionsHolder.getPageList());
        collectionsHolder.getPageList().clear();
    }

    public void fillInLemmas() {
        System.out.println("lemmasMap size - " + collectionsHolder.getSiteLemmaMap().size());
        for (SiteLemmaPair pair : collectionsHolder.getSiteLemmaMap().keySet()) {
            Lemma lemma = new Lemma(pair.getLemma(), collectionsHolder.getSiteLemmaMap().get(pair), pair.getSite());
            collectionsHolder.getLemmaList().add(lemma);
        }
        repositoriesHolder.saveAllLemmas(collectionsHolder.getLemmaList());
        collectionsHolder.getSiteLemmaMap().clear();
        collectionsHolder.getLemmaList().clear();
    }

    public void fillInSearchIndex() {
        Iterable<Lemma> lemmaIterable = repositoriesHolder.findAllLemmas();
        Map<String, Lemma> lemmasMapFromDB = new HashMap<>();
        for (Lemma lemma : lemmaIterable) {
            lemmasMapFromDB.put(lemma.getLemma(), lemma);
        }

        List<Index> indexList = new ArrayList<>();
        for (TempIndex tempIndex : collectionsHolder.getTempIndexList()) {
            Lemma lemma = lemmasMapFromDB.get(tempIndex.getLemma());
            indexList.add(new Index(tempIndex.getPage(), lemma, tempIndex.getLemmaRank()));
        }

        System.out.println("indexList.size() - " + indexList.size());
        repositoriesHolder.saveAllIndexes(indexList);
        collectionsHolder.getTempIndexList().clear();
    }

    public void deletePreviouslyIndexedSiteByName(String siteName, int siteId) {
        repositoriesHolder.deletePreviouslyIndexedSiteByName(siteName, siteId);
    }

    public void setSiteStatus(Site site, Status status) {
        repositoriesHolder.setSiteStatus(site.getId(), status);
    }

    public void setFailedStatus(Site site, String error) {
        repositoriesHolder.setFailedStatus(site.getId(), error);
    }
}
