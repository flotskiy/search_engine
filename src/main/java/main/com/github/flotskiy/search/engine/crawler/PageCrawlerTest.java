package main.com.github.flotskiy.search.engine.crawler;

import main.com.github.flotskiy.search.engine.repositories.PageRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;

public class PageCrawlerTest {
    private static final BufferedReader BUFFERED_READER = new BufferedReader(new InputStreamReader(System.in));
    private static final String SOURCE = getPath();
    private static final Set<String> WEBPAGES_PATH = new TreeSet<>();

    public static void testCrawler(PageRepository pageRepository) throws IOException {
        URL url = new URL(SOURCE);
        String homePage = url.getProtocol() + "://" + url.getHost() + "/";
        new ForkJoinPool().invoke(new PageCrawler(WEBPAGES_PATH, homePage, pageRepository));
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
