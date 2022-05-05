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

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollFiller {

    public static void fillInSelectorsAndWeigh(RepositoriesHolder repositoriesHolder) {
        Iterable<Field> fieldIterable = repositoriesHolder.getFieldRepository().findAll();
        for (Field field : fieldIterable) {
            CollectionsHolder.getSelectorsAndWeight().put(field.getSelector(), field.getWeight());
        }
    }

    public static void fillInSiteList(Map<String, String> SOURCES_MAP) {
        for (Map.Entry<String, String> entry : SOURCES_MAP.entrySet()) {
            Site site = new Site(Status.INDEXING, new Date(), entry.getValue(), entry.getKey());
            CollectionsHolder.getSiteList().add(site);
        }
    }

    public static void fillInLemmasMapAndTempIndexList(int code, String html, Page page, Site site) {
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
            CollectionsHolder.getSiteLemmaMap()
                    .put(
                            siteLemmaPair,
                            CollectionsHolder.getSiteLemmaMap().getOrDefault(siteLemmaPair, 0) + 1
                    );
            float lemmaRank =
                    CollFiller.calculateLemmaRank(lemma, titleLemmasCount, bodyLemmasCount);
            CollectionsHolder.getTempIndexList().add(new TempIndex(page, lemma, lemmaRank));
        }
    }

    public static boolean isPageAdded(String pagePath) {
        pagePath += pagePath.endsWith("/") ? "" : "/";
        if (!CollectionsHolder.getWebpagesPath().contains(pagePath)) {
            CollectionsHolder.getWebpagesPath().add(pagePath);
            return false;
        }
        System.out.println("\tPage was added before: " + pagePath);
        return true;
    }

    public static void addPageToPagesList(Page page) {
        CollectionsHolder.getPageList().add(page);
    }

    public static float calculateLemmaRank(
            String lemma,
            Map<String, Integer> titleLemmasCount,
            Map<String, Integer> bodyLemmasCount
            ) {
        return titleLemmasCount.getOrDefault(lemma, 0) *
                CollectionsHolder.getSelectorsAndWeight().get("title") +
                bodyLemmasCount.getOrDefault(lemma, 0) *
                CollectionsHolder.getSelectorsAndWeight().get("body");
    }
}
