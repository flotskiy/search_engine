package com.github.flotskiy.search.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SiteLemmaPair {
    private String lemma;
    private Site site;
}
