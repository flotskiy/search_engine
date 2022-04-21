package main.com.github.flotskiy.search.engine.crawler;

import main.com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import main.com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import main.com.github.flotskiy.search.engine.model.Index;
import main.com.github.flotskiy.search.engine.model.Lemma;
import main.com.github.flotskiy.search.engine.util.TempIndex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class PageCrawlerTest {
    private static final BufferedReader BUFFERED_READER = new BufferedReader(new InputStreamReader(System.in));
    private static final String SOURCE = getPath();
    private static final CollectionsHolder COLLECTIONS_HOLDER = new CollectionsHolder();

    public static void testCrawler(RepositoriesHolder repositoriesHolder) throws IOException {
        COLLECTIONS_HOLDER.fillInSelectorsAndWeigh(repositoriesHolder.getFieldRepository());
        URL url = new URL(SOURCE);
        String homePage = url.getProtocol() + "://" + url.getHost() + "/";
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new PageCrawler(COLLECTIONS_HOLDER, homePage, repositoriesHolder));

        System.out.println("Размер lemmasMap - " + COLLECTIONS_HOLDER.getLemmasMap().size());
        for (String key : COLLECTIONS_HOLDER.getLemmasMap().keySet()) {
            Lemma lemma = new Lemma(key, COLLECTIONS_HOLDER.getLemmasMap().get(key));
            COLLECTIONS_HOLDER.getLemmasList().add(lemma);
        }
        repositoriesHolder.getLemmaRepository().saveAll(COLLECTIONS_HOLDER.getLemmasList());

        List<Index> indexList = new ArrayList<>();
        for (TempIndex tempIndex : COLLECTIONS_HOLDER.getTempIndexList()) {
            int lemmaId = repositoriesHolder.getLemmaRepository().getLemmaByName(tempIndex.getLemma());
            Lemma lemma = repositoriesHolder.getLemmaRepository().findById(lemmaId).orElseThrow();
            indexList.add(new Index(tempIndex.getPage(), lemma, tempIndex.getLemmaRank()));
        }

        System.out.println("tempIndexList.size() - " + COLLECTIONS_HOLDER.getTempIndexList().size());
        System.out.println("indexList.size() - " + indexList.size());
        repositoriesHolder.getIndexRepository().saveAll(indexList);
    }

    private static String getPath() {
        String input = "";
        System.out.println("Please input site url:");
        try {
            input = BUFFERED_READER.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return input;
    }
}
