package com.github.flotskiy.search.engine.indexing;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.model.Status;
import com.github.flotskiy.search.engine.util.StringHelper;
import com.github.flotskiy.search.engine.util.YmlConfigGetter;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

public class PageCrawlerStarter {

    private static final String INTERRUPTED_BY_USER_MESSAGE = "Indexing stopped by user";

    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    private final Map<String, String> SOURCES_MAP = YmlConfigGetter.getSites();

    private final CollectionsHolder collectionsHolder;
    private final RepoFiller repoFiller;
    private final CollFiller collFiller;

    public PageCrawlerStarter(CollectionsHolder collectionsHolder, RepoFiller repoFiller, CollFiller collFiller) {
        this.collectionsHolder = collectionsHolder;
        this.repoFiller = repoFiller;
        this.collFiller = collFiller;
    }

    public void startCrawling() {
        if (SOURCES_MAP.size() < 1) {
            System.out.println("No sites specified!");
        }

        long start = System.currentTimeMillis();
        collFiller.fillInSelectorsAndWeigh();
        collFiller.fillInSiteList(SOURCES_MAP);
        repoFiller.fillInFields();

        for (Site site : new ArrayList<>(collectionsHolder.getSiteList())) {
            String homePage = makeActionsBeforeForkJoinPoolStarted(site);
            if (isStopped.get()) {
                fixErrorAndClearCollections(site);
                return;
            }
            try {
                PageCrawler pageCrawler = new PageCrawler(homePage, site, collFiller);
                forkJoinPool.invoke(pageCrawler);
                if (isStopped.get()) {
                    fixErrorAndClearCollections(site);
                    return;
                }
                completeActionsAfterForkJoinPoolFinished(site);
            } catch (CancellationException ce) {
                System.out.println("CancellationException in PageCrawlerStarter");
                fixErrorAndClearCollections(site);
                return;
            }
            if (isStopped.get()) {
                fixErrorAndClearCollections(site);
                return;
            }
        }
        isStopped.set(true);
        System.out.println("Duration of processing: " + (System.currentTimeMillis() - start) / 1000 + " s");
    }

    public void stopIndexing() {
        isStopped.set(true);
        forkJoinPool.shutdownNow();
    }

    public void fixErrorAndClearCollections(Site site) {
        repoFiller.setFailedStatus(site, INTERRUPTED_BY_USER_MESSAGE);
        collFiller.clearCollections();
        collFiller.clearSelectorsAndWeightCollection();
    }

    private String makeActionsBeforeForkJoinPoolStarted(Site site){
        repoFiller.setSiteStatus(site, Status.INDEXING);
        repoFiller.deletePreviouslyIndexedSiteByName(site.getName(), site.getId());
        repoFiller.saveSite(site);
        return StringHelper.getHomePage(site.getUrl());
    }

    private void completeActionsAfterForkJoinPoolFinished(Site site){
        repoFiller.fillInPages();
        repoFiller.fillInLemmas();
        repoFiller.fillInSearchIndex();
        if (isStopped.get()) {
            return;
        }
        repoFiller.setSiteStatus(site, Status.INDEXED);
        collFiller.clearCollections();
    }

    public boolean isStopped() {
        return isStopped.get();
    }
}