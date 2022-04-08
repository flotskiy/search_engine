package main.com.github.flotskiy.search.engine.crawler;

import main.com.github.flotskiy.search.engine.model.Page;
import org.hibernate.Session;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

public class PageCrawler extends RecursiveAction {

    private final Set<String> webpages;
    private final String pagePath;
    private final Session session;

    public PageCrawler(Set<String> webpages, String pagePath, Session session) {
        this.webpages = webpages;
        this.pagePath = pagePath;
        this.session = session;
    }

    @Override
    protected void compute() {

        List<PageCrawler> pagesList = new ArrayList<>();

        try {
            Thread.sleep(500);
            Connection connection = Jsoup.connect(pagePath)
                    .userAgent("Mozilla / 5.0 (Windows NT 10.0; Win64; x64) AppleWebKit / " +
                            "537.36 (KHTML, как Gecko) Chrome / 81.0.4044.92 Safari / 537.36")
//                    .userAgent("FlotskiySearchBot")
                    .referrer("http://www.google.com")
                    .ignoreHttpErrors(true);
//                    .ignoreContentType(true);

            Connection.Response response = connection.execute();
            int httpStatusCode = response.statusCode();
            String html = "";

            if (httpStatusCode == 200) {
                Document document = connection.get();
                html = document.outerHtml();

                Elements anchors = document.select("body").select("a");
                for (Element anchor : anchors) {
                    String href = anchor.absUrl("href");
                    if (href.startsWith(pagePath) && isHrefToPage(href) && !isPageAdded(href)) {
                        System.out.println("Added to set: " + href);
                        PageCrawler pageCrawler = new PageCrawler(webpages, href, session);
                        pagesList.add(pageCrawler);
                        pageCrawler.fork();
                    }
                }
            }

            URL url = new URL(pagePath);
            String homePage = url.getProtocol() + "://" + url.getHost();
            String pathToSave = pagePath.substring(homePage.length());
            pathToSave += pathToSave.endsWith("/") ? "" : "/";

            Page page = new Page(pathToSave, httpStatusCode, html);
            synchronized (session) {
                session.save(page);
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        for (PageCrawler pageCrawler : pagesList) {
            pageCrawler.join();
        }
    }

    private boolean isPageAdded(String pagePath) {
        synchronized (webpages) {
            if (!webpages.contains(pagePath)) {
                webpages.add(pagePath);
                return false;
            }
        }
        System.out.println("\tPage was added before: " + pagePath);
        return true;
    }

    private boolean isHrefToPage(String href) {
        if (href.matches(".*(#|\\?).*")) {
            return false;
        }
        return !href.matches(
                ".*\\.(pdf|docx?|xlsx?|pptx?|jpe?g|gif|png|mp3|aac|json|csv|exe|apk|rar|zip|xml|jar|bin|svg)/?"
        );
    }
}
