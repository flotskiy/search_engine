package com.github.flotskiy.search.engine.indexing;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.Page;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.model.Status;
import com.github.flotskiy.search.engine.util.JsoupHelper;
import com.github.flotskiy.search.engine.lemmatizer.Lemmatizer;
import com.github.flotskiy.search.engine.model.Field;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CollFiller {

    private final CollectionsHolder collectionsHolder;

    @Autowired
    public CollFiller(CollectionsHolder collectionsHolder) {
        this.collectionsHolder = collectionsHolder;
    }

    public void fillInSelectorsAndWeigh(RepositoriesHolder repositoriesHolder) {
        Iterable<Field> fieldIterable = repositoriesHolder.getFieldRepository().findAll();
        for (Field field : fieldIterable) {
            collectionsHolder.getSelectorsAndWeight().put(field.getSelector(), field.getWeight());
        }
    }

    public void fillInSiteList(Map<String, String> SOURCES_MAP) {
        for (Map.Entry<String, String> entry : SOURCES_MAP.entrySet()) {
            Site site = new Site(Status.INDEXING, new Date(), entry.getValue(), entry.getKey());
            collectionsHolder.getSiteList().add(site);
        }
    }

    public void fillInLemmasMapAndTempIndexList(int code, String html, Page page, Site site) {
        if (code != 200) {
            return;
        }

        Document htmlDocument = JsoupHelper.getDocument(html);

        String title = htmlDocument.title();
        System.out.println(title);
        Map<String, Integer> titleLemmasCount = Lemmatizer.getLemmasCountMap(title);

        String bodyText = htmlDocument.body().text();
        Map<String, Integer> bodyLemmasCount = Lemmatizer.getLemmasCountMap(bodyText);

        Map<String, Integer> uniqueLemmasInTitleAndBody = Stream
                .concat(titleLemmasCount.entrySet().stream(), bodyLemmasCount.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));
        System.out.println("Все леммы: " + uniqueLemmasInTitleAndBody);

        for (String lemma : uniqueLemmasInTitleAndBody.keySet()) {
            SiteLemmaPair siteLemmaPair = new SiteLemmaPair(lemma, site);
            collectionsHolder.getSiteLemmaMap()
                    .put(
                            siteLemmaPair,
                            collectionsHolder.getSiteLemmaMap().getOrDefault(siteLemmaPair, 0) + 1
                    );
            float lemmaRank =
                    calculateLemmaRank(lemma, titleLemmasCount, bodyLemmasCount);
            collectionsHolder.getTempIndexList().add(new TempIndex(page, lemma, lemmaRank));
        }
    }

    public boolean isPageAdded(String pagePath) {
        pagePath += pagePath.endsWith("/") ? "" : "/";
        if (!collectionsHolder.getWebpagesPath().contains(pagePath)) {
            collectionsHolder.getWebpagesPath().add(pagePath);
            return false;
        }
        System.out.println("\tPage was added before: " + pagePath);
        return true;
    }

    public void addPageToPagesList(Page page) {
        collectionsHolder.getPageList().add(page);
    }

    public float calculateLemmaRank(
            String lemma,
            Map<String, Integer> titleLemmasCount,
            Map<String, Integer> bodyLemmasCount
            ) {
        return titleLemmasCount.getOrDefault(lemma, 0) *
                collectionsHolder.getSelectorsAndWeight().get("title") +
                bodyLemmasCount.getOrDefault(lemma, 0) *
                collectionsHolder.getSelectorsAndWeight().get("body");
    }
}
