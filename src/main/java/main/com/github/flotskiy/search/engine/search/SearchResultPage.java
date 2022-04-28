package main.com.github.flotskiy.search.engine.search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchResultPage {
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
}
