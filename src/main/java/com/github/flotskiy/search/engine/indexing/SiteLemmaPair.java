package com.github.flotskiy.search.engine.indexing;

import com.github.flotskiy.search.engine.model.Site;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SiteLemmaPair {
    private String lemma;
    private Site site;
}
