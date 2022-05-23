package com.github.flotskiy.search.engine.service.indexing.one;

import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.service.indexing.all.CollFiller;
import com.github.flotskiy.search.engine.model.*;
import com.github.flotskiy.search.engine.util.JsoupHelper;
import com.github.flotskiy.search.engine.util.StringHelper;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SinglePageCrawler {

    private final RepositoriesHolder repositoriesHolder;
    private final CollFiller collFiller;

    @Autowired
    public SinglePageCrawler(RepositoriesHolder repositoriesHolder, CollFiller collFiller) {
        this.repositoriesHolder = repositoriesHolder;
        this.collFiller = collFiller;
    }

    public boolean addOrUpdateSinglePage(String pagePathFromUser) {
        try {
            if (!StringHelper.isHrefToPage(pagePathFromUser)) {
                return false;
            }
            String startPage = StringHelper.getStartPage(pagePathFromUser);
            Connection connection = JsoupHelper.getConnection(pagePathFromUser);
            Connection.Response response = connection.execute();
            int httpStatusCode = response.statusCode();
            Site siteToAddPage = getSiteToAddPage(startPage);
            if (siteToAddPage == null || httpStatusCode == 404) {
                return false;
            }
            String pagePath = StringHelper.cutProtocolAndHost(pagePathFromUser, startPage);
            Page page = repositoriesHolder.getPageByPath(pagePath);
            if (page != null) {
                repositoriesHolder.deletePage(page);
            }
            if (httpStatusCode != 200) {
                repositoriesHolder.savePage(new Page(pagePath, httpStatusCode, "", siteToAddPage));
                return true;
            }
            Document document = connection.get();
            String html = document.outerHtml();
            page = new Page(pagePath, httpStatusCode, html, siteToAddPage);
            repositoriesHolder.savePage(page);
            collFiller.fillInLemmasAndIndexForSinglePageCrawler(html, page, siteToAddPage);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Site getSiteToAddPage(String startPage) throws NullPointerException {
        Iterable<Site> allSitesIterable = repositoriesHolder.findAllSites();
        for (Site site : allSitesIterable) {
            if (site.getUrl().equals(startPage + "/")) {
                return site;
            }
        }
        return null;
    }
}
