package com.github.flotskiy.search.engine.model.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class QueryHolder {
    private String query;
    private String site;
    private int offset;
    private int limit;
}
