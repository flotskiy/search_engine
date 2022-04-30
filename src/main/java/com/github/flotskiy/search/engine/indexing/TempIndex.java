package com.github.flotskiy.search.engine.indexing;

import com.github.flotskiy.search.engine.model.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TempIndex {
    private Page page;
    private String lemma;
    private float lemmaRank;
}
