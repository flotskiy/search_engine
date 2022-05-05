package com.github.flotskiy.search.engine.crawler;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.indexing.CollFiller;
import com.github.flotskiy.search.engine.indexing.RepoFiller;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.model.Status;
import com.github.flotskiy.search.engine.util.StringHelper;
import com.github.flotskiy.search.engine.util.YmlConfig;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class PageCrawlerStarter {
    private static final Map<String, String> SOURCES_MAP = YmlConfig.getSites();

    public static void testCrawler(RepositoriesHolder repositoriesHolder) throws IOException {
        CollFiller.fillInSelectorsAndWeigh(repositoriesHolder);
        CollFiller.fillInSiteList(SOURCES_MAP);

        RepoFiller.fillInSites(repositoriesHolder);

        ForkJoinPool forkJoinPool = new ForkJoinPool();

        if (CollectionsHolder.getSiteList().isEmpty()) {
            System.out.println("No sites specified!");
        } else {
            for (Site site : CollectionsHolder.getSiteList()) {
                String homePage = StringHelper.getHomePage(site.getUrl());
                PageCrawler pageCrawler = new PageCrawler(homePage, repositoriesHolder, site);
                forkJoinPool.invoke(pageCrawler);
                RepoFiller.fillInPages(repositoriesHolder);
                RepoFiller.fillInLemmas(repositoriesHolder);
                RepoFiller.fillInSearchIndex(repositoriesHolder);
                site.setStatus(Status.INDEXED);
                site.setStatusTime(new Date());
                repositoriesHolder.getSiteRepository().save(site);
            }
        }
    }
}
