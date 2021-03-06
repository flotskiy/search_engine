package com.github.flotskiy.search.engine.model.search;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchResultTrue {
    private boolean result = true;
    private int count;
    private List<SearchResultPage> data;
}
