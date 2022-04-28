package main.com.github.flotskiy.search.engine.indexing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import main.com.github.flotskiy.search.engine.model.Page;

@Getter
@Setter
@AllArgsConstructor
public class TempIndex {
    private Page page;
    private String lemma;
    private float lemmaRank;
}
