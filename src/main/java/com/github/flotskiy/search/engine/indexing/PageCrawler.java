package com.github.flotskiy.search.engine.indexing;

import com.github.flotskiy.search.engine.model.Page;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.util.StringHelper;
import com.github.flotskiy.search.engine.util.YmlConfigGetter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class PageCrawler extends RecursiveAction {

    private final String pagePath;
    private final Site site;
    private final CollFiller collFiller;

    public PageCrawler(String pagePath, Site site, CollFiller collFiller) {
        this.pagePath = pagePath;
        this.site = site;
        this.collFiller = collFiller;
    }

    @Override
    protected void compute() {
        List<PageCrawler> forkJoinPoolPagesList = new ArrayList<>();
        try {
            Thread.sleep(500);
            Connection connection = Jsoup.connect(pagePath)
                    .userAgent(YmlConfigGetter.getConnectUseragent())
//                    .userAgent("FlotskiySearchBot")
                    .referrer(YmlConfigGetter.getConnectReferrer())
                    .ignoreHttpErrors(true);

            URL url = new URL(pagePath);
            String homePage = url.getProtocol() + "://" + url.getHost();

            Connection.Response response = connection.execute();
            int httpStatusCode = response.statusCode();
            String html = "";

            if (httpStatusCode == 200) {
                Document document = connection.get();
                html = document.outerHtml();

                Elements anchors = document.select("body").select("a");
                for (Element anchor : anchors) {
                    String href = anchor.absUrl("href");
                    if (StringHelper.isHrefValid(homePage, href, collFiller)) {
                        System.out.println("Added to set: " + href);
                        PageCrawler pageCrawler = new PageCrawler(href, site, collFiller);
                        forkJoinPoolPagesList.add(pageCrawler);
                        pageCrawler.fork();
                    }
                }
            }

            String pathToSave = StringHelper.cutProtocolAndHost(pagePath, homePage);
            Page page = new Page(pathToSave, httpStatusCode, html, site);
            collFiller.addPageToPagesList(page);
            collFiller.fillInLemmasMapAndTempIndexList(httpStatusCode, html, page, site);

        } catch (InterruptedException ie) {
            System.out.println("InterruptedException in PageCrawler - Thread: " + Thread.currentThread().getName());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        for (PageCrawler pageCrawler : forkJoinPoolPagesList) {
            pageCrawler.join();
        }
    }
}