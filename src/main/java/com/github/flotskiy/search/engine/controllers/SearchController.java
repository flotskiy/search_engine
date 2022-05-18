package com.github.flotskiy.search.engine.controllers;

import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.search.QueryHolder;
import com.github.flotskiy.search.engine.model.search.SearchResultTrue;
import com.github.flotskiy.search.engine.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

@Controller
public class SearchController {

    private final RepositoriesHolder repositoriesHolder;

    @Autowired
    public SearchController(RepositoriesHolder repositoriesHolder) {
        this.repositoriesHolder = repositoriesHolder;
    }

    @GetMapping("/search")
    public ResponseEntity<Object> startIndexing(
            @RequestParam(name="query", required = false) String query,
            @RequestParam(name="site", required = false) String site,
            @RequestParam(name="offset", required = false) Integer offset,
            @RequestParam(name="limit", required = false) Integer limit
    ) {
        HashMap<String, Object> searchResultFalse = new HashMap<>();
        searchResultFalse.put("result", false);
        if (SearchService.hasIndexingAndFailedSites(repositoriesHolder, site)) {
            searchResultFalse.put("error", "Дождитель успешного окончания индексации");
            return ResponseEntity.status(403).body(searchResultFalse);
        }
        if (SearchService.isQueryExists(query)) {
            QueryHolder queryHolder = new QueryHolder(query, site, offset, limit);
            SearchResultTrue searchResultTrue = SearchService.getSearchResult(
                    repositoriesHolder,
                    queryHolder
            );
            return ResponseEntity.ok().body(searchResultTrue);
        }
        searchResultFalse.put("error", "Задан пустой поисковый запрос");
        return ResponseEntity.badRequest().body(searchResultFalse);
    }
}
