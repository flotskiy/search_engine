package main.com.github.flotskiy.search.engine.indexing;

import main.com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import main.com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import main.com.github.flotskiy.search.engine.lemmatizer.Lemmatizer;
import main.com.github.flotskiy.search.engine.model.Field;
import main.com.github.flotskiy.search.engine.model.Page;
import main.com.github.flotskiy.search.engine.util.TempIndex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollFiller {

    public static void fillInSelectorsAndWeigh(
            CollectionsHolder collectionsHolder,
            RepositoriesHolder repositoriesHolder
    ) {
        Iterable<Field> fieldIterable = repositoriesHolder.getFieldRepository().findAll();
        for (Field field : fieldIterable) {
            collectionsHolder.getSelectorsAndWeight().put(field.getSelector(), field.getWeight());
        }
    }

    public static void fillInLemmasMapAndTempIndexesList(CollectionsHolder holder, int code, String html, Page page) {
        if (code != 200) {
            return;
        }

        Document htmlDocument = Jsoup.parse(html);

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
            holder.getLemmasMap()
                    .put(lemma, holder.getLemmasMap().getOrDefault(lemma, 0) + 1);
            float lemmaRank =
                    CollFiller.calculateLemmaRank(holder, lemma, titleLemmasCount, bodyLemmasCount);
            holder.getTempIndexesList().add(new TempIndex(page, lemma, lemmaRank));
        }
    }

    public static boolean isPageAdded(CollectionsHolder holder, String pagePath) {
        pagePath += pagePath.endsWith("/") ? "" : "/";
        if (!holder.getWebpagesPath().contains(pagePath)) {
            holder.getWebpagesPath().add(pagePath);
            return false;
        }
        System.out.println("\tPage was added before: " + pagePath);
        return true;
    }

    public static void addPageToPagesList(CollectionsHolder holder, Page page) {
        holder.getPagesList().add(page);
    }

    public static float calculateLemmaRank(
            CollectionsHolder holder,
            String lemma,
            Map<String, Integer> titleLemmasCount,
            Map<String, Integer> bodyLemmasCount
            ) {
        return titleLemmasCount.getOrDefault(lemma, 0) *
                holder.getSelectorsAndWeight().get("title") +
                bodyLemmasCount.getOrDefault(lemma, 0) *
                holder.getSelectorsAndWeight().get("body");
    }
}
