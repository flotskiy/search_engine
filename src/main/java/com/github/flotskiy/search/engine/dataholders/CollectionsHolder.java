package com.github.flotskiy.search.engine.dataholders;

import com.github.flotskiy.search.engine.model.Lemma;
import com.github.flotskiy.search.engine.model.Page;
import com.github.flotskiy.search.engine.indexing.TempIndex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionsHolder {
    private static final ConcurrentHashMap<String, String> WEBPAGES_PATH_MAP = new ConcurrentHashMap<>();
    private static final Set<String> WEBPAGES_PATH = WEBPAGES_PATH_MAP.keySet("default");
    private static final ConcurrentHashMap<String, Integer> LEMMAS_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Float> SELECTORS_AND_WEIGHT = new ConcurrentHashMap<>();
    private static final List<Page> PAGES_LIST = Collections.synchronizedList(new ArrayList<>());
    private static final List<TempIndex> TEMP_INDEXES_LIST = Collections.synchronizedList(new ArrayList<>());
    private static final List<Lemma> LEMMAS_LIST = new ArrayList<>();

    public static Set<String> getWebpagesPath() {
        return WEBPAGES_PATH;
    }

    public static ConcurrentHashMap<String, Integer> getLemmasMap() {
        return LEMMAS_MAP;
    }

    public static ConcurrentHashMap<String, Float> getSelectorsAndWeight() {
        return SELECTORS_AND_WEIGHT;
    }

    public static List<Page> getPagesList() {
        return PAGES_LIST;
    }

    public static List<TempIndex> getTempIndexesList() {
        return TEMP_INDEXES_LIST;
    }

    public static List<Lemma> getLemmasList() {
        return LEMMAS_LIST;
    }
}
