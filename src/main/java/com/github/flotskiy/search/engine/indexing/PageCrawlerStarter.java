package com.github.flotskiy.search.engine.indexing;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.model.Status;
import com.github.flotskiy.search.engine.util.StringHelper;
import com.github.flotskiy.search.engine.util.YmlConfigGetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@Component
public class PageCrawlerStarter {

    private static final Map<String, String> SOURCES_MAP = YmlConfigGetter.getSites();

    private final CollectionsHolder collectionsHolder;
    private final RepoFiller repoFiller;
    private final CollFiller collFiller;

    @Autowired
    public PageCrawlerStarter(
            CollectionsHolder collectionsHolder,
            RepoFiller repoFiller,
            CollFiller collFiller
    ) {
        this.collectionsHolder = collectionsHolder;
        this.repoFiller = repoFiller;
        this.collFiller = collFiller;
    }

    public void startCrawling(RepositoriesHolder repositoriesHolder) {
        long start = System.currentTimeMillis();
        collFiller.fillInSelectorsAndWeigh(repositoriesHolder);
        collFiller.fillInSiteList(SOURCES_MAP);
        repoFiller.fillInFields();

        ForkJoinPool forkJoinPool = new ForkJoinPool();

        if (SOURCES_MAP.size() < 1) {
            System.out.println("No sites specified!");
        } else {
            for (Site site : new ArrayList<>(collectionsHolder.getSiteList())) {
                String homePage = makeActionsBeforeForkJoinPoolStarted(site);
                PageCrawler pageCrawler = new PageCrawler(homePage, site, collFiller);
                forkJoinPool.invoke(pageCrawler);
                completeActionsAfterForkJoinPoolFinished(site);
            }
        }
        System.out.println("Duration of processing: " + (System.currentTimeMillis() - start) / 1000 + " s");
    }

    private String makeActionsBeforeForkJoinPoolStarted(Site site) {
        repoFiller.changeSiteStatus(site, Status.INDEXING);
        repoFiller.deletePreviouslyIndexedSiteByName(site.getName(), site.getId());
        repoFiller.saveSite(site);
        return StringHelper.getHomePage(site.getUrl());
    }

    private void completeActionsAfterForkJoinPoolFinished(Site site) {
        repoFiller.fillInPages();
        repoFiller.fillInLemmas();
        repoFiller.fillInSearchIndex();
        repoFiller.changeSiteStatus(site, Status.INDEXED);
        collFiller.clearCollections();
    }
}
