package main.com.github.flotskiy.search.engine.util;

import lombok.Getter;
import lombok.Setter;
import main.com.github.flotskiy.search.engine.model.Page;

@Getter
@Setter
public class TempIndex {
    private Page page;
    private String lemma;
    private float lemmaRank;

    public TempIndex(Page page, String lemma, float lemmaRank) {
        this.page = page;
        this.lemma = lemma;
        this.lemmaRank = lemmaRank;
    }
}
