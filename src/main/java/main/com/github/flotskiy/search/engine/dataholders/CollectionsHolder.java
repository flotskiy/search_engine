package main.com.github.flotskiy.search.engine.dataholders;

import main.com.github.flotskiy.search.engine.model.Field;
import main.com.github.flotskiy.search.engine.model.Lemma;
import main.com.github.flotskiy.search.engine.repositories.FieldRepository;
import main.com.github.flotskiy.search.engine.util.TempIndex;

import java.util.*;

public class CollectionsHolder {
    private final Set<String> webpagesPath = new TreeSet<>();
    private final Map<String, Integer> lemmasMap = new HashMap<>();
    private final Map<String, Float> selectorsAndWeight = new HashMap<>();
    private final List<TempIndex> tempIndexList = new ArrayList<>();
    private final List<Lemma> lemmasList = new ArrayList<>();

    public Set<String> getWebpagesPath() {
        return webpagesPath;
    }

    public Map<String, Integer> getLemmasMap() {
        return lemmasMap;
    }

    public List<TempIndex> getTempIndexList() {
        return tempIndexList;
    }

    public Map<String, Float> getSelectorsAndWeight() {
        return selectorsAndWeight;
    }

    public List<Lemma> getLemmasList() {
        return lemmasList;
    }

    public void fillInSelectorsAndWeigh(FieldRepository fieldRepository) {
        Iterable<Field> fieldIterable = fieldRepository.findAll();
        for (Field field : fieldIterable) {
            selectorsAndWeight.put(field.getSelector(), field.getWeight());
        }
    }
}
