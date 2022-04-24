package main.com.github.flotskiy.search.engine.dataholders;

import main.com.github.flotskiy.search.engine.model.Lemma;
import main.com.github.flotskiy.search.engine.model.Page;
import main.com.github.flotskiy.search.engine.util.TempIndex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionsHolder {
    private final ConcurrentHashMap<String, String> webpagesPathMap = new ConcurrentHashMap<>();
    private final Set<String> webpagesPath = webpagesPathMap.keySet("default");
    private final ConcurrentHashMap<String, Integer> lemmasMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Float> selectorsAndWeight = new ConcurrentHashMap<>();
    private final List<Page> pagesList = Collections.synchronizedList(new ArrayList<>());
    private final List<TempIndex> tempIndexesList = Collections.synchronizedList(new ArrayList<>());
    private final List<Lemma> lemmasList = new ArrayList<>();

    public Set<String> getWebpagesPath() {
        return webpagesPath;
    }

    public ConcurrentHashMap<String, Integer> getLemmasMap() {
        return lemmasMap;
    }

    public ConcurrentHashMap<String, Float> getSelectorsAndWeight() {
        return selectorsAndWeight;
    }

    public List<Page> getPagesList() {
        return pagesList;
    }

    public List<TempIndex> getTempIndexesList() {
        return tempIndexesList;
    }

    public List<Lemma> getLemmasList() {
        return lemmasList;
    }
}
