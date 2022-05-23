package com.github.flotskiy.search.engine.service.indexing.all;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.*;
import com.github.flotskiy.search.engine.util.JsoupHelper;
import com.github.flotskiy.search.engine.service.lemmatizer.Lemmatizer;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CollFiller {

    private final CollectionsHolder collectionsHolder;
    private final RepositoriesHolder repositoriesHolder;

    @Autowired
    public CollFiller(CollectionsHolder collectionsHolder, RepositoriesHolder repositoriesHolder) {
        this.collectionsHolder = collectionsHolder;
        this.repositoriesHolder = repositoriesHolder;
    }

    public void setSelectorsAndWeigh() {
        ConcurrentHashMap<String, Float> tempSelectorsAndWeight = getSelectorsAndWeight();
        collectionsHolder.getSelectorsAndWeight().putAll(tempSelectorsAndWeight);
    }

    private ConcurrentHashMap<String, Float> getSelectorsAndWeight() {
        ConcurrentHashMap<String, Float> selectorsAndWeight = new ConcurrentHashMap<>();
        Iterable<Field> fieldIterable = repositoriesHolder.findAllFields();
        for (Field field : fieldIterable) {
            selectorsAndWeight.put(field.getSelector(), field.getWeight());
        }
        return selectorsAndWeight;
    }

    public void fillInSiteList(Map<String, String> SOURCES_MAP) {
        Iterable<Site> sitesInRepository = repositoriesHolder.findAllSites();
        for (Map.Entry<String, String> entry : SOURCES_MAP.entrySet()) {
            Site site = new Site(Status.INDEXING, "no errors", new Date(), entry.getValue(), entry.getKey());
            for (Site siteFromRepository : sitesInRepository) {
                if (site.getName().equals(siteFromRepository.getName())) {
                    repositoriesHolder.deletePreviouslyIndexedSiteByName(
                            siteFromRepository.getName(), siteFromRepository.getId()
                    );
                    break;
                }
            }
            collectionsHolder.getSiteList().add(site);
        }
    }

    public void fillInLemmasMapAndTempIndexList(String html, Page page, Site site) {
        List<Map<String, Integer>> mapList = getUniqueLemmasListOfMaps(html);
        for (String lemma : mapList.get(2).keySet()) {
            SiteLemmaPair siteLemmaPair = new SiteLemmaPair(lemma, site);
            collectionsHolder.getSiteLemmaMap()
                    .put(
                            siteLemmaPair,
                            collectionsHolder.getSiteLemmaMap().getOrDefault(siteLemmaPair, 0) + 1
                    );
            float lemmaRank = calculateLemmaRank(
                    lemma, mapList.get(0), mapList.get(1), collectionsHolder.getSelectorsAndWeight()
            );
            collectionsHolder.getTempIndexList().add(new TempIndex(page, lemma, lemmaRank));
        }
    }

    public void fillInLemmasAndIndexForSinglePageCrawler(String html, Page page, Site site) {
        List<Map<String, Integer>> mapList = getUniqueLemmasListOfMaps(html);
        Iterable<Lemma> lemmaIterableFromDb = repositoriesHolder.getAllLemmasFromSite(site.getId());
        Map<String, Lemma> tempMapWithLemmas = new HashMap<>();
        for (Lemma lemma : lemmaIterableFromDb) {
            for (String lemmaString : mapList.get(2).keySet()) {
                if (lemma.getLemma().equals(lemmaString)) {
                    lemma.setFrequency(lemma.getFrequency() + 1);
                    tempMapWithLemmas.put(lemma.getLemma(), lemma);
                }
            }
        }
        repositoriesHolder.saveAllLemmas(tempMapWithLemmas.values());

        ConcurrentHashMap<String, Float> selectorsAndWeight = getSelectorsAndWeight();
        List<Index> tempIndexList = new ArrayList<>();
        for (String lemmaString : mapList.get(2).keySet()) {
            float lemmaRank = calculateLemmaRank(lemmaString, mapList.get(0), mapList.get(1), selectorsAndWeight);
            Lemma lemma = tempMapWithLemmas.get(lemmaString);
            tempIndexList.add(new Index(page, lemma, lemmaRank));
        }
        repositoriesHolder.saveAllIndexes(tempIndexList);
    }

    private List<Map<String, Integer>> getUniqueLemmasListOfMaps(String html) {
        Document htmlDocument = JsoupHelper.getDocument(html);
        String title = htmlDocument.title();
        System.out.println(title);
        String bodyText = htmlDocument.body().text();

        Map<String, Integer> titleLemmasCount = Lemmatizer.getLemmasCountMap(title); // 0
        Map<String, Integer> bodyLemmasCount = Lemmatizer.getLemmasCountMap(bodyText); // 1
        Map<String, Integer> uniqueLemmasInTitleAndBody = Stream // 2
                .concat(titleLemmasCount.entrySet().stream(), bodyLemmasCount.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));

        List<Map<String, Integer>> mapList = new ArrayList<>();
        mapList.add(titleLemmasCount);
        mapList.add(bodyLemmasCount);
        mapList.add(uniqueLemmasInTitleAndBody);
        return mapList;
    }

    public boolean isPageAdded(String pagePath) {
        pagePath += pagePath.endsWith("/") ? "" : "/";
        if (!collectionsHolder.getWebpagesPath().contains(pagePath)) {
            collectionsHolder.getWebpagesPath().add(pagePath);
            return false;
        }
        return true;
    }

    public void addPageToPagesList(Page page) {
        collectionsHolder.getPageList().add(page);
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

    public void clearCollections() {
        collectionsHolder.getSiteList().clear();
        collectionsHolder.getWebpagesPath().clear();
        collectionsHolder.getSiteLemmaMap().clear();
        collectionsHolder.getPageList().clear();
        collectionsHolder.getTempIndexList().clear();
        collectionsHolder.getLemmaList().clear();
    }

    public void clearSelectorsAndWeightCollection() {
        collectionsHolder.getSelectorsAndWeight().clear();
    }
}
