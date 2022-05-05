package com.github.flotskiy.search.engine.dataholders;

import com.github.flotskiy.search.engine.indexing.SiteLemmaPair;
import com.github.flotskiy.search.engine.model.Lemma;
import com.github.flotskiy.search.engine.model.Page;
import com.github.flotskiy.search.engine.indexing.TempIndex;
import com.github.flotskiy.search.engine.model.Site;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionsHolder {
    private static final List<Site> SITE_LIST = new ArrayList<>();
    private static final ConcurrentHashMap<String, String> WEBPAGES_PATH_MAP = new ConcurrentHashMap<>();
    private static final Set<String> WEBPAGES_PATH = WEBPAGES_PATH_MAP.keySet("default");
    private static final ConcurrentHashMap<SiteLemmaPair, Integer> SITE_LEMMA_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Float> SELECTORS_AND_WEIGHT = new ConcurrentHashMap<>();
    private static final List<Page> PAGE_LIST = Collections.synchronizedList(new ArrayList<>());
    private static final List<TempIndex> TEMP_INDEX_LIST = Collections.synchronizedList(new ArrayList<>());
    private static final List<Lemma> LEMMA_LIST = new ArrayList<>();

    public static List<Site> getSiteList() {
        return SITE_LIST;
    }

    public static Set<String> getWebpagesPath() {
        return WEBPAGES_PATH;
    }

    public static ConcurrentHashMap<SiteLemmaPair, Integer> getSiteLemmaMap() {
        return SITE_LEMMA_MAP;
    }

    public static ConcurrentHashMap<String, Float> getSelectorsAndWeight() {
        return SELECTORS_AND_WEIGHT;
    }

    public static List<Page> getPageList() {
        return PAGE_LIST;
    }

    public static List<TempIndex> getTempIndexList() {
        return TEMP_INDEX_LIST;
    }

    public static List<Lemma> getLemmaList() {
        return LEMMA_LIST;
    }
}
