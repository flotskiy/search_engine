package com.github.flotskiy.search.engine.indexing;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.util.StringHelper;
import com.github.flotskiy.search.engine.util.YmlConfigGetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@Component
public class PageCrawlerStarter {
    private static final Map<String, String> SOURCES_MAP = YmlConfigGetter.getSites();
    private final CollectionsHolder collectionsHolder;
    private final RepoFiller repoFiller;
    private final CollFiller collFiller;
    private final StringHelper stringHelper;

    @Autowired
    public PageCrawlerStarter(
            CollectionsHolder collectionsHolder,
            RepoFiller repoFiller,
            CollFiller collFiller,
            StringHelper stringHelper
    ) {
        this.collectionsHolder = collectionsHolder;
        this.repoFiller = repoFiller;
        this.collFiller = collFiller;
        this.stringHelper = stringHelper;
    }

    public void makeCrawling(RepositoriesHolder repositoriesHolder) {
        collFiller.fillInSelectorsAndWeigh(repositoriesHolder);
        collFiller.fillInSiteList(SOURCES_MAP);
        repoFiller.fillInSites();

        ForkJoinPool forkJoinPool = new ForkJoinPool();

        if (collectionsHolder.getSiteList().isEmpty()) {
            System.out.println("No sites specified!");
        } else {
            for (Site site : collectionsHolder.getSiteList()) {
                repoFiller.deletePreviouslyIndexedSiteByName(site.getName(), site.getId());
                String homePage = stringHelper.getHomePage(site.getUrl());
                PageCrawler pageCrawler = new PageCrawler(homePage, site, collFiller, stringHelper);
                forkJoinPool.invoke(pageCrawler);
                repoFiller.fillInPages();
                repoFiller.fillInLemmas();
                repoFiller.fillInSearchIndex();
                repoFiller.markSiteAsIndexed(site);
            }
        }
    }
}
