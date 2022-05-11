package com.github.flotskiy.search.engine.model.statistics;

import lombok.Data;

@Data
public class Total {
    int sites;
    int pages;
    int lemmas;
    boolean isIndexing;
}
