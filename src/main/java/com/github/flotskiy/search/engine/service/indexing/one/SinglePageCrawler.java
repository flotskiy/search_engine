package com.github.flotskiy.search.engine.service.indexing.one;

import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.service.lemmatizer.Lemmatizer;
import com.github.flotskiy.search.engine.model.*;
import com.github.flotskiy.search.engine.util.JsoupHelper;
import com.github.flotskiy.search.engine.util.StringHelper;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SinglePageCrawler {

    private final RepositoriesHolder repositoriesHolder;

    @Autowired
    public SinglePageCrawler(RepositoriesHolder repositoriesHolder) {
        this.repositoriesHolder = repositoriesHolder;
    }

    public boolean addOrUpdateSinglePage(String pagePathFromUser) {
        try {
            if (!StringHelper.isHrefToPage(pagePathFromUser)) {
                return false;
            }
            URL url = new URL(pagePathFromUser);
            String homePage = url.getProtocol() + "://" + url.getHost();
            Site siteToAddPage = null;
            Iterable<Site> allSitesIterable = repositoriesHolder.getSiteRepository().getAllSites();
            for (Site site : allSitesIterable) {
                if (site.getUrl().equals(homePage + "/")) {
                    siteToAddPage = site;
                }
            }
            if (siteToAddPage == null) {
                return false;
            }
            Connection connection = JsoupHelper.getConnection(pagePathFromUser);
            Connection.Response response = connection.execute();
            int httpStatusCode = response.statusCode();
            String html = "";
            if (httpStatusCode == 404) {
                return false;
            }
            if (httpStatusCode == 200) {
                Document document = connection.get();
                html = document.outerHtml();
            }
            String pathToFindOrSave = StringHelper.cutProtocolAndHost(pagePathFromUser, homePage);
            Page page = repositoriesHolder.getPageRepository().getPageByPath(pathToFindOrSave);
            if (page != null) {
                repositoriesHolder.getPageRepository().delete(page);
            }
            page = new Page(pathToFindOrSave, httpStatusCode, html, siteToAddPage);
            repositoriesHolder.getPageRepository().save(page);
            return fillInLemmasAndIndex(httpStatusCode, html, page, siteToAddPage);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean fillInLemmasAndIndex(int code, String html, Page page, Site site) {
        if (code != 200) {
            return false;
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

        Iterable<Lemma> lemmaIterableFromDb = repositoriesHolder.getLemmaRepository().getAllLemmasFromSite(site.getId());
        Map<String, Lemma> tempMapWithLemmas = new HashMap<>();
        for (Lemma lemma : lemmaIterableFromDb) {
            for (String lemmaString : uniqueLemmasInTitleAndBody.keySet()) {
                if (lemma.getLemma().equals(lemmaString)) {
                    lemma.setFrequency(lemma.getFrequency() + 1);
                    tempMapWithLemmas.put(lemma.getLemma(), lemma);
                }
            }
        }
        repositoriesHolder.getLemmaRepository().saveAll(tempMapWithLemmas.values());

        Iterable<Field> fieldIterableFromDb = repositoriesHolder.getFieldRepository().getAllFields();
        HashMap<String, Float> selectorsAndWeightHashMap = new HashMap<>();
        for (Field field : fieldIterableFromDb) {
            selectorsAndWeightHashMap.put(field.getSelector(), field.getWeight());
        }

        List<Index> tempIndexList = new ArrayList<>();
        for (String lemmaString : uniqueLemmasInTitleAndBody.keySet()) {
            float lemmaRank =
                    calculateLemmaRank(lemmaString, titleLemmasCount, bodyLemmasCount, selectorsAndWeightHashMap);
            Lemma lemma = tempMapWithLemmas.get(lemmaString);
            tempIndexList.add(new Index(page, lemma, lemmaRank));

        }
        repositoriesHolder.getIndexRepository().saveAll(tempIndexList);
        return true;
    }

    private float calculateLemmaRank(
            String lemma,
            Map<String, Integer> titleLemmasCount,
            Map<String, Integer> bodyLemmasCount,
            Map<String, Float> selectorsAndWeight
    ) {
        return titleLemmasCount.getOrDefault(lemma, 0) * selectorsAndWeight.get("title") +
                bodyLemmasCount.getOrDefault(lemma, 0) * selectorsAndWeight.get("body");
    }
}
