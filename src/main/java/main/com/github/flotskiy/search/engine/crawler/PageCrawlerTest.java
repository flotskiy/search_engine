package main.com.github.flotskiy.search.engine.crawler;

import main.com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import main.com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import main.com.github.flotskiy.search.engine.indexing.CollFiller;
import main.com.github.flotskiy.search.engine.indexing.RepoFiller;
import main.com.github.flotskiy.search.engine.util.StringHelper;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;

public class PageCrawlerTest {
    private static final String SOURCE = StringHelper.getPath();
    private static final CollectionsHolder COLLECTIONS_HOLDER = new CollectionsHolder();

    public static void testCrawler(RepositoriesHolder repositoriesHolder) throws IOException {
        CollFiller.fillInSelectorsAndWeigh(COLLECTIONS_HOLDER, repositoriesHolder);
        URL url = new URL(SOURCE);
        String homePage = url.getProtocol() + "://" + url.getHost() + "/";
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new PageCrawler(COLLECTIONS_HOLDER, homePage, repositoriesHolder));

        RepoFiller.fillInPagesTable(COLLECTIONS_HOLDER, repositoriesHolder);
        RepoFiller.fillInLemmasTable(COLLECTIONS_HOLDER, repositoriesHolder);
        RepoFiller.fillInSearchIndexTable(COLLECTIONS_HOLDER, repositoriesHolder);
    }
}
