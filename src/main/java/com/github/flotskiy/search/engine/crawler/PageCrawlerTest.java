package com.github.flotskiy.search.engine.crawler;

import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.indexing.CollFiller;
import com.github.flotskiy.search.engine.indexing.RepoFiller;
import com.github.flotskiy.search.engine.util.YmlConfig;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;

public class PageCrawlerTest {
    private static final String SOURCE = YmlConfig.getSites().get("nizhny800");
//    private static final Map<String, String> SOURCES_MAP = YmlConfig.getSites();

    public static void testCrawler(RepositoriesHolder repositoriesHolder) throws IOException {
        CollFiller.fillInSelectorsAndWeigh(repositoriesHolder);
        URL url = new URL(SOURCE);
        String homePage = url.getProtocol() + "://" + url.getHost() + "/";
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new PageCrawler(homePage, repositoriesHolder));

        RepoFiller.fillInPages(repositoriesHolder);
        RepoFiller.fillInLemmas(repositoriesHolder);
        RepoFiller.fillInSearchIndex(repositoriesHolder);
    }
}
