package com.github.flotskiy.search.engine.dataholders;

import com.github.flotskiy.search.engine.indexing.SiteLemmaPair;
import com.github.flotskiy.search.engine.model.Lemma;
import com.github.flotskiy.search.engine.model.Page;
import com.github.flotskiy.search.engine.indexing.TempIndex;
import com.github.flotskiy.search.engine.model.Site;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class CollectionsHolder {
    private final List<Site> siteList = new ArrayList<>();
    private final ConcurrentHashMap<String, String> webpagesPathMap = new ConcurrentHashMap<>();
    private final Set<String> webpagesPath = webpagesPathMap.keySet("default");
    private final ConcurrentHashMap<SiteLemmaPair, Integer> siteLemmaMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Float> selectorsAndWeight = new ConcurrentHashMap<>();
    private final List<Page> pageList = Collections.synchronizedList(new ArrayList<>());
    private final List<TempIndex> tempIndexList = Collections.synchronizedList(new ArrayList<>());
    private final List<Lemma> lemmaList = new ArrayList<>();
}
