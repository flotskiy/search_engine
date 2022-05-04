package com.github.flotskiy.search.engine.crawler;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.indexing.CollFiller;
import com.github.flotskiy.search.engine.model.Page;
import com.github.flotskiy.search.engine.util.StringHelper;
import com.github.flotskiy.search.engine.util.YmlConfig;
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

    private final CollectionsHolder collHolder;
    private final String pagePath;
    private final RepositoriesHolder repoHolder;

    public PageCrawler(CollectionsHolder collHolder, String pagePath, RepositoriesHolder repoHolder) {
        this.collHolder = collHolder;
        this.pagePath = pagePath;
        this.repoHolder = repoHolder;
    }

    @Override
    protected void compute() {
        List<PageCrawler> forkJoinPoolPagesList = new ArrayList<>();
        try {
            Thread.sleep(500);
            Connection connection = Jsoup.connect(pagePath)
                    .userAgent(YmlConfig.getConnectUseragent())
//                    .userAgent("FlotskiySearchBot")
                    .referrer(YmlConfig.getConnectReferrer())
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
                    if (StringHelper.isHrefValid(collHolder, homePage, href)) {
                        System.out.println("Added to set: " + href);
                        PageCrawler pageCrawler = new PageCrawler(collHolder, href, repoHolder);
                        forkJoinPoolPagesList.add(pageCrawler);
                        pageCrawler.fork();
                    }
                }
            }

            String pathToSave = StringHelper.cutProtocolAndHost(pagePath, homePage);
            Page page = new Page(pathToSave, httpStatusCode, html);
            CollFiller.addPageToPagesList(collHolder, page);
            CollFiller.fillInLemmasMapAndTempIndexesList(collHolder, httpStatusCode, html, page);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        for (PageCrawler pageCrawler : forkJoinPoolPagesList) {
            pageCrawler.join();
        }
    }
}
